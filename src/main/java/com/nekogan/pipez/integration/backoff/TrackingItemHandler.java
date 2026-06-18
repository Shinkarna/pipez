package com.nekogan.pipez.integration.backoff;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class TrackingItemHandler implements IItemHandler {
    private final IItemHandler delegate;
    private boolean didExtract;

    public TrackingItemHandler(IItemHandler delegate) {
        this.delegate = delegate;
        this.didExtract = false;
    }

    public boolean didExtract() {
        return didExtract;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return delegate.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack result = delegate.extractItem(slot, amount, simulate);
        if (!simulate && !result.isEmpty()) {
            this.didExtract = true;
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return delegate.isItemValid(slot, stack);
    }
}
