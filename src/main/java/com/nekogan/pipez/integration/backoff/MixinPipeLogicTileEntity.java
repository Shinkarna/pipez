package com.nekogan.pipez.integration.backoff;

import com.nekogan.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = PipeLogicTileEntity.class, remap = false)
public class MixinPipeLogicTileEntity implements IItemPipeBackoff {

    @Unique
    private final long[] pipezCe$nextActiveTick = new long[Direction.values().length];

    @Unique
    private final int[] pipezCe$backoffDelay = new int[Direction.values().length];

    @Override
    public long pipezCe$getNextActiveTick(Direction side) {
        return pipezCe$nextActiveTick[side.get3DDataValue()];
    }

    @Override
    public void pipezCe$setNextActiveTick(Direction side, long tick) {
        pipezCe$nextActiveTick[side.get3DDataValue()] = tick;
    }

    @Override
    public int pipezCe$getBackoffDelay(Direction side) {
        return pipezCe$backoffDelay[side.get3DDataValue()];
    }

    @Override
    public void pipezCe$setBackoffDelay(Direction side, int delay) {
        pipezCe$backoffDelay[side.get3DDataValue()] = delay;
    }
}
