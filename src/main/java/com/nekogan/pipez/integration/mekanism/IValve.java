package com.nekogan.pipez.integration.mekanism;

import com.nekogan.pipez.blocks.PipeBlock;
import com.nekogan.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.nekogan.pipez.blocks.tileentity.PipeTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IValve {
    /**
     * Update pipez cache and reconnect.
     * @param self valve block entity
     * @param sides valid sides
     */
    default void pipezCe$updatePipezCache(BlockEntity self, Direction[] sides) {
        var level = self.getLevel();
        if (level == null || level.isClientSide) return;
        for (Direction side : sides) {
            var pipezSide = side.getOpposite();
            var pipePos = self.getBlockPos().relative(side);
            if (level.getBlockState(pipePos).getBlock() instanceof PipeBlock) {
                PipeTileEntity.markPipesDirty(level, pipePos);
            }

            var be = level.getBlockEntity(pipePos);

            if (be instanceof PipeLogicTileEntity pipez && pipez.isExtracting(pipezSide)) {
                pipez.setExtracting(pipezSide, true);
            }
        }
    }
}
