package com.nekogan.pipez.blocks.tileentity.types;

import com.nekogan.pipez.Filter;
import com.nekogan.pipez.FluidFilter;
import com.nekogan.pipez.Main;
import com.nekogan.pipez.Upgrade;
import com.nekogan.pipez.blocks.ModBlocks;
import com.nekogan.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.nekogan.pipez.blocks.tileentity.PipeTileEntity;
import com.nekogan.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

public class FluidPipeType extends PipeType<Fluid> {

    public static final FluidPipeType INSTANCE = new FluidPipeType();

    @Override
    public String getKey() {
        return "Fluid";
    }

    @Override
    public Capability<?> getCapability() {
        return ForgeCapabilities.FLUID_HANDLER;
    }

    @Override
    public Filter<Fluid> createFilter() {
        return new FluidFilter();
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez_ce.fluid";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.FLUID_PIPE.get());
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return Component.translatable("tooltip.pipez_ce.rate.fluid", getRate(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {
        for (Direction side : Direction.values()) {
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
            IFluidHandler fluidHandler = PipeLogicTileEntity.getFluidHandler(tileEntity.getLevel(), extractingConnection.getPos(), extractingConnection.getDirection());
            if (fluidHandler == null) {
                continue;
            }

            List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

            if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, fluidHandler);
            } else {
                insertOrdered(tileEntity, side, connections, fluidHandler);
            }
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IFluidHandler fluidHandler) {
        if (connections.isEmpty()) {
            return;
        }
        int completeAmount = getRate(tileEntity, side);
        int mbToTransfer = completeAmount;
        boolean[] connectionsFull = new boolean[connections.size()];
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        while (mbToTransfer > 0 && hasNotInserted(connectionsFull)) {
            PipeTileEntity.Connection connection = connections.get(p);
            IFluidHandler destination = PipeLogicTileEntity.getFluidHandler(tileEntity.getLevel(), connection.getPos(), connection.getDirection());
            boolean hasInserted = false;
            if (destination != null && !connectionsFull[p]) {
                for (int j = 0; j < fluidHandler.getTanks(); j++) {
                    FluidStack fluidInTank = fluidHandler.getFluidInTank(j);
                    FluidStack simulatedExtract = fluidHandler.drain(new FluidStack(fluidInTank.getFluid(), Math.min(Math.max(completeAmount / getConnectionsNotFullCount(connectionsFull), 1), mbToTransfer), fluidInTank.getTag()), IFluidHandler.FluidAction.SIMULATE);
                    if (simulatedExtract.isEmpty()) {
                        continue;
                    }
                    if (canInsert(connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                        continue;
                    }
                    FluidStack stack = FluidUtil.tryFluidTransfer(destination, fluidHandler, simulatedExtract, true);
                    if (stack.getAmount() > 0) {
                        mbToTransfer -= stack.getAmount();
                        hasInserted = true;
                        break;
                    }
                }
            }
            if (!hasInserted) {
                connectionsFull[p] = true;
            }
            p = (p + 1) % connections.size();
        }

        tileEntity.setRoundRobinIndex(side, this, p);
    }

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IFluidHandler fluidHandler) {
        int mbToTransfer = getRate(tileEntity, side);

        HashSet<Fluid> nonFittingFluids = new HashSet<>();

        connectionLoop:
        for (PipeTileEntity.Connection connection : connections) {
            nonFittingFluids.clear();
            IFluidHandler destination = PipeLogicTileEntity.getFluidHandler(tileEntity.getLevel(), connection.getPos(), connection.getDirection());
            if (destination == null) {
                continue;
            }

            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                if (mbToTransfer <= 0) {
                    break connectionLoop;
                }
                FluidStack fluidInTank = fluidHandler.getFluidInTank(i);
                if (nonFittingFluids.contains(fluidInTank.getFluid())) {
                    continue;
                }
                FluidStack simulatedExtract = fluidHandler.drain(new FluidStack(fluidInTank.getFluid(), mbToTransfer, fluidInTank.getTag()), IFluidHandler.FluidAction.SIMULATE);
                if (simulatedExtract.isEmpty()) {
                    continue;
                }
                if (canInsert(connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                    continue;
                }
                FluidStack stack = FluidUtil.tryFluidTransfer(destination, fluidHandler, simulatedExtract, true);
                if (stack.getAmount() <= 0) {
                    nonFittingFluids.add(fluidInTank.getFluid());
                }
                mbToTransfer -= stack.getAmount();
            }
        }
    }

    private boolean canInsert(PipeTileEntity.Connection connection, FluidStack stack, List<Filter<?>> filters) {
        boolean hasNonInvert = false;
        for (Filter<?> f : filters) {
            Filter<Fluid> filter = (Filter<Fluid>) f;
            if (!matchesConnection(connection, filter)) {
                continue;
            }
            if (filter.isInvert()) {
                if (matches(filter, stack)) {
                    return false;
                }
            } else {
                hasNonInvert = true;
                if (matches(filter, stack)) {
                    return true;
                }
            }
        }
        return !hasNonInvert;
    }

    private boolean matches(Filter<Fluid> filter, FluidStack stack) {
        CompoundTag metadata = filter.getMetadata();
        if (metadata == null) {
            return filter.getTag() == null || filter.getTag().contains(stack.getFluid());
        }
        if (filter.isExactMetadata()) {
            if (deepExactCompare(metadata, stack.getTag())) {
                return filter.getTag() == null || filter.getTag().contains(stack.getFluid());
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
            return filter.getTag() == null || filter.getTag().contains(stack.getFluid());
        }
    }

    private boolean hasNotInserted(boolean[] inventoriesFull) {
        for (boolean b : inventoriesFull) {
            if (!b) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return Main.SERVER_CONFIG.fluidPipeAmount.get();
        }
        switch (upgrade) {
            case BASIC:
                return Main.SERVER_CONFIG.fluidPipeAmountBasic.get();
            case IMPROVED:
                return Main.SERVER_CONFIG.fluidPipeAmountImproved.get();
            case ADVANCED:
                return Main.SERVER_CONFIG.fluidPipeAmountAdvanced.get();
            case ULTIMATE:
                return Main.SERVER_CONFIG.fluidPipeAmountUltimate.get();
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }

}
