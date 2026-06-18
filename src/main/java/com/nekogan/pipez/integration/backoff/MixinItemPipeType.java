package com.nekogan.pipez.integration.backoff;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.nekogan.pipez.Main;
import com.nekogan.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.nekogan.pipez.blocks.tileentity.PipeTileEntity;
import com.nekogan.pipez.blocks.tileentity.types.ItemPipeType;
import net.minecraft.core.Direction;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ItemPipeType.class, remap = false)
public class MixinItemPipeType {

    // --- insertEqually ---

    @Inject(method = "insertEqually", at = @At("HEAD"), cancellable = true, remap = false)
    public void startEqually(PipeLogicTileEntity tileEntity, Direction side,
                             List<PipeTileEntity.Connection> connections, IItemHandler itemHandler,
                             CallbackInfo ci) {
        if (pipezCe$shouldSuppress(tileEntity, side)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "insertEqually", at = @At("HEAD"), argsOnly = true, remap = false)
    public IItemHandler wrapHandlerEqually(IItemHandler handler,
                                           @Share("tracker") LocalRef<TrackingItemHandler> tracked) {
        TrackingItemHandler tracker = new TrackingItemHandler(handler);
        tracked.set(tracker);
        return tracker;
    }

    @Inject(method = "insertEqually", at = @At("RETURN"), remap = false)
    public void endEqually(PipeLogicTileEntity tileEntity, Direction side,
                           List<PipeTileEntity.Connection> connections, IItemHandler itemHandler,
                           CallbackInfo ci,
                           @Share("tracker") LocalRef<TrackingItemHandler> tracked) {
        pipezCe$applyBackoff(tileEntity, side, tracked.get().didExtract());
    }

    // --- insertOrdered ---

    @Inject(method = "insertOrdered", at = @At("HEAD"), cancellable = true, remap = false)
    public void startOrdered(PipeLogicTileEntity tileEntity, Direction side,
                             List<PipeTileEntity.Connection> connections, IItemHandler itemHandler,
                             CallbackInfo ci) {
        if (pipezCe$shouldSuppress(tileEntity, side)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "insertOrdered", at = @At("HEAD"), argsOnly = true, remap = false)
    public IItemHandler wrapHandlerOrdered(IItemHandler handler,
                                           @Share("tracker") LocalRef<TrackingItemHandler> tracked) {
        TrackingItemHandler tracker = new TrackingItemHandler(handler);
        tracked.set(tracker);
        return tracker;
    }

    @Inject(method = "insertOrdered", at = @At("RETURN"), remap = false)
    public void endOrdered(PipeLogicTileEntity tileEntity, Direction side,
                           List<PipeTileEntity.Connection> connections, IItemHandler itemHandler,
                           CallbackInfo ci,
                           @Share("tracker") LocalRef<TrackingItemHandler> tracked) {
        pipezCe$applyBackoff(tileEntity, side, tracked.get().didExtract());
    }

    // --- Helper methods ---

    @Unique
    private static boolean pipezCe$shouldSuppress(PipeLogicTileEntity tileEntity, Direction side) {
        if (!Main.SERVER_CONFIG.itemPipeBackoffEnabled.get()) return false;
        if (!(tileEntity instanceof IItemPipeBackoff backoff)) return false;
        return backoff.pipezCe$getNextActiveTick(side) > tileEntity.getLevel().getGameTime();
    }

    @Unique
    private static void pipezCe$applyBackoff(PipeLogicTileEntity tileEntity, Direction side, boolean didExtract) {
        if (!Main.SERVER_CONFIG.itemPipeBackoffEnabled.get()) return;
        if (!(tileEntity instanceof IItemPipeBackoff backoff)) return;

        long now = tileEntity.getLevel().getGameTime();
        int currentDelay = backoff.pipezCe$getBackoffDelay(side);

        if (didExtract) {
            // Success: halve the delay
            int newDelay = currentDelay >> 1;
            backoff.pipezCe$setBackoffDelay(side, newDelay);
            backoff.pipezCe$setNextActiveTick(side, now + (newDelay / 2) + 1);
        } else {
            // Failure: exponential backoff
            int baseBackoff = Main.SERVER_CONFIG.itemPipeBaseBackoffTicks.get();
            int maxBackoff = Main.SERVER_CONFIG.itemPipeMaxBackoffTicks.get();
            if (currentDelay == 0) {
                backoff.pipezCe$setBackoffDelay(side, baseBackoff);
                backoff.pipezCe$setNextActiveTick(side, now + baseBackoff);
            } else {
                int newDelay = Math.min(currentDelay << 1, maxBackoff);
                backoff.pipezCe$setBackoffDelay(side, newDelay);
                backoff.pipezCe$setNextActiveTick(side, now + newDelay / 2);
            }
        }
    }
}
