package com.nekogan.pipez.blocks.tileentity.types;

import de.maxhenkel.corelib.item.ItemUtils;
import com.nekogan.pipez.Filter;
import com.nekogan.pipez.ItemFilter;
import com.nekogan.pipez.Main;
import com.nekogan.pipez.Upgrade;
import com.nekogan.pipez.blocks.ModBlocks;
import com.nekogan.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.nekogan.pipez.blocks.tileentity.PipeTileEntity;
import com.nekogan.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemPipeType extends PipeType<Item> {

    public static final ItemPipeType INSTANCE = new ItemPipeType();

    @Override
    public String getKey() {
        return "Item";
    }

    @Override
    public Capability<?> getCapability() {
        return ForgeCapabilities.ITEM_HANDLER;
    }

    @Override
    public Filter<Item> createFilter() {
        return new ItemFilter();
    }

    @Override
    public UpgradeTileEntity.Distribution getDefaultDistribution() {
        return UpgradeTileEntity.Distribution.NEAREST;
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez_ce.item";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.ITEM_PIPE.get());
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return Component.translatable("tooltip.pipez_ce.rate.item", getRate(upgrade), getSpeed(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {
        long gameTime = tileEntity.getLevel().getGameTime();
        for (Direction side : Direction.values()) {
            if (gameTime % getSpeed(tileEntity, side) != 0) {
                continue;
            }
            if (!tileEntity.isExtracting(side)) {
                continue;
            }
            if (!tileEntity.shouldWork(side, this)) {
                continue;
            }
            PipeTileEntity.Connection extractingConnection = tileEntity.getExtractingConnection(side);
            if (extractingConnection == null) {
                continue;
            }
            IItemHandler itemHandler = PipeLogicTileEntity.getItemHandler(tileEntity.getLevel(), extractingConnection.getPos(), extractingConnection.getDirection());
            if (itemHandler == null) {
                continue;
            }

            List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

            if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, itemHandler);
            } else {
                insertOrdered(tileEntity, side, connections, itemHandler);
            }
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IItemHandler itemHandler) {
        if (connections.isEmpty()) {
            return;
        }
        int itemsToTransfer = getRate(tileEntity, side);
        int totalSlots = itemHandler.getSlots();
        if (totalSlots <= 0) return;

        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        int startSlot = tileEntity.getLastExtractSlot(side, this) % totalSlots;
        List<Filter<?>> filters = tileEntity.getFilters(side, this);
        boolean isBlacklist = tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST);

        // Pre-fetch all destination handlers (null = unreachable, skip)
        IItemHandler[] destinations = new IItemHandler[connections.size()];
        for (int i = 0; i < connections.size(); i++) {
            destinations[i] = PipeLogicTileEntity.getItemHandler(
                    tileEntity.getLevel(), connections.get(i).getPos(), connections.get(i).getDirection());
        }

        int scanned = 0;
        while (scanned < totalSlots && itemsToTransfer > 0) {
            // Batch simulate extract from current slot
            ItemStack batchExtract = itemHandler.extractItem(startSlot, itemsToTransfer, true);
            if (batchExtract.isEmpty()) {
                startSlot = (startSlot + 1) % totalSlots;
                scanned++;
                continue;
            }

            int available = batchExtract.getCount();
            if (available <= 0) {
                startSlot = (startSlot + 1) % totalSlots;
                scanned++;
                continue;
            }

            // Find compatible destinations (filter pass + not full, in round-robin order starting from p)
            int activeCount = 0;
            for (int i = 0; i < connections.size(); i++) {
                int idx = (p + i) % connections.size();
                IItemHandler dest = destinations[idx];
                if (dest != null && !isFull(dest)) {
                    ItemStack check = batchExtract.copy();
                    check.setCount(1);
                    if (!(canInsert(tileEntity, side, connections.get(idx), check) == isBlacklist)) {
                        activeCount++;
                    }
                }
            }

            if (activeCount == 0) {
                // No destination accepts this item type — skip slot
                startSlot = (startSlot + 1) % totalSlots;
                scanned++;
                continue;
            }

            // Batch distribute: perDest = ceil(available / activeCount), waterfall remainder
            int perDest = Math.max(1, available / activeCount);
            int remainder = available % activeCount;
            int distributed = 0;

            for (int i = 0; i < connections.size() && distributed < available; i++) {
                int idx = (p + i) % connections.size();
                IItemHandler dest = destinations[idx];
                if (dest == null || isFull(dest)) continue;

                ItemStack check = batchExtract.copy();
                check.setCount(1);
                if (canInsert(tileEntity, side, connections.get(idx), check) == isBlacklist) continue;

                int toInsert = perDest + (remainder > 0 ? 1 : 0);
                if (remainder > 0) remainder--;
                if (toInsert <= 0) continue;

                ItemStack insertStack = batchExtract.copy();
                insertStack.setCount(toInsert);
                ItemStack leftover = ItemHandlerHelper.insertItem(dest, insertStack, false);
                int inserted = toInsert - leftover.getCount();
                if (inserted > 0) {
                    distributed += inserted;
                }
            }

            // Waterfall: redistribute any undistributed remainder
            if (distributed < available) {
                int remaining = available - distributed;
                for (int i = 0; i < connections.size() && remaining > 0; i++) {
                    int idx = (p + i) % connections.size();
                    IItemHandler dest = destinations[idx];
                    if (dest == null || isFull(dest)) continue;
                    ItemStack check = batchExtract.copy();
                    check.setCount(1);
                    if (canInsert(tileEntity, side, connections.get(idx), check) == isBlacklist) continue;

                    ItemStack insertStack = batchExtract.copy();
                    insertStack.setCount(Math.min(remaining, insertStack.getMaxStackSize()));
                    ItemStack leftover = ItemHandlerHelper.insertItem(dest, insertStack, false);
                    int inserted = Math.min(remaining, insertStack.getCount()) - leftover.getCount();
                    if (inserted > 0) {
                        distributed += inserted;
                        remaining -= inserted;
                    }
                }
            }

            // Commit extraction
            if (distributed > 0) {
                itemHandler.extractItem(startSlot, distributed, false);
                itemsToTransfer -= distributed;
            }

            tileEntity.setLastExtractSlot(side, this, startSlot);
            startSlot = (startSlot + 1) % totalSlots;
            scanned++;
            p = (p + 1) % connections.size();
        }

        tileEntity.setRoundRobinIndex(side, this, p);
    }

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IItemHandler itemHandler) {
        int itemsToTransfer = getRate(tileEntity, side);
        int totalSlots = itemHandler.getSlots();
        if (totalSlots <= 0) return;

        ArrayList<ItemStack> nonFittingItems = new ArrayList<>();
        int startSlot = tileEntity.getLastExtractSlot(side, this) % totalSlots;
        int lastSuccessfulSlot = startSlot;

        connectionLoop:
        for (PipeTileEntity.Connection connection : connections) {
            nonFittingItems.clear();
            IItemHandler destination = PipeLogicTileEntity.getItemHandler(tileEntity.getLevel(), connection.getPos(), connection.getDirection());
            if (destination == null) {
                continue;
            }
            if (isFull(destination)) {
                continue;
            }
            int slot = startSlot;
            do {
                if (itemsToTransfer <= 0) {
                    break connectionLoop;
                }
                ItemStack simulatedExtract = itemHandler.extractItem(slot, itemsToTransfer, true);
                if (simulatedExtract.isEmpty()) {
                    slot = (slot + 1) % totalSlots;
                    continue;
                }
                if (nonFittingItems.stream().anyMatch(stack -> ItemUtils.isStackable(stack, simulatedExtract))) {
                    slot = (slot + 1) % totalSlots;
                    continue;
                }
                if (canInsert(tileEntity, side, connection, simulatedExtract) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                    slot = (slot + 1) % totalSlots;
                    continue;
                }
                ItemStack stack = ItemHandlerHelper.insertItem(destination, simulatedExtract, false);
                int insertedAmount = simulatedExtract.getCount() - stack.getCount();
                if (insertedAmount <= 0) {
                    nonFittingItems.add(simulatedExtract);
                }
                itemsToTransfer -= insertedAmount;
                itemHandler.extractItem(slot, insertedAmount, false);
                if (insertedAmount > 0) {
                    lastSuccessfulSlot = slot;
                }
                slot = (slot + 1) % totalSlots;
            } while (slot != startSlot);
        }

        tileEntity.setLastExtractSlot(side, this, lastSuccessfulSlot);
    }

    private boolean isFull(IItemHandler itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            if (stackInSlot.getCount() < itemHandler.getSlotLimit(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean canInsert(PipeLogicTileEntity tileEntity, Direction side, PipeTileEntity.Connection connection, ItemStack stack) {
        List<Filter<?>> filters = tileEntity.getFilters(side, this);

        // Fast path: cache by item type for repeat transfers
        Integer itemKey = Item.getId(stack.getItem());
        Map<Integer, Boolean> cache = tileEntity.getFilterMatchCache(side);
        Boolean cached = cache.get(itemKey);
        if (cached != null) {
            return cached;
        }

        boolean hasNonInvert = false;
        for (Filter<?> f : filters) {
            Filter<Item> filter = (Filter<Item>) f;
            if (!matchesConnection(connection, filter)) {
                continue;
            }
            if (filter.isInvert()) {
                if (matches(filter, stack)) {
                    cache.put(itemKey, false);
                    return false;
                }
            } else {
                hasNonInvert = true;
                if (matches(filter, stack)) {
                    cache.put(itemKey, true);
                    return true;
                }
            }
        }
        boolean result = !hasNonInvert;
        cache.put(itemKey, result);
        return result;
    }

    private boolean matches(Filter<Item> filter, ItemStack stack) {
        CompoundTag metadata = filter.getMetadata();
        if (metadata == null) {
            return filter.getTag() == null || filter.getTag().contains(stack.getItem());
        }
        if (filter.isExactMetadata()) {
            if (deepExactCompare(metadata, stack.getTag())) {
                return filter.getTag() == null || filter.getTag().contains(stack.getItem());
            } else {
                return false;
            }
        } else {
            CompoundTag stackNBT = stack.getTag();
            if (stackNBT == null) {
                return metadata.size() <= 0;
            }
            if (!deepFuzzyCompare(metadata, stackNBT)) {
                return false;
            }
            return filter.getTag() == null || filter.getTag().contains(stack.getItem());
        }
    }

    public int getSpeed(PipeLogicTileEntity tileEntity, Direction direction) {
        return getSpeed(tileEntity.getUpgrade(direction));
    }

    public int getSpeed(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return Main.SERVER_CONFIG.itemPipeSpeed.get();
        }
        switch (upgrade) {
            case BASIC:
                return Main.SERVER_CONFIG.itemPipeSpeedBasic.get();
            case IMPROVED:
                return Main.SERVER_CONFIG.itemPipeSpeedImproved.get();
            case ADVANCED:
                return Main.SERVER_CONFIG.itemPipeSpeedAdvanced.get();
            case ULTIMATE:
                return Main.SERVER_CONFIG.itemPipeSpeedUltimate.get();
            case INFINITY:
            default:
                return 1;
        }
    }

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return Main.SERVER_CONFIG.itemPipeAmount.get();
        }
        switch (upgrade) {
            case BASIC:
                return Main.SERVER_CONFIG.itemPipeAmountBasic.get();
            case IMPROVED:
                return Main.SERVER_CONFIG.itemPipeAmountImproved.get();
            case ADVANCED:
                return Main.SERVER_CONFIG.itemPipeAmountAdvanced.get();
            case ULTIMATE:
                return Main.SERVER_CONFIG.itemPipeAmountUltimate.get();
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }
}
