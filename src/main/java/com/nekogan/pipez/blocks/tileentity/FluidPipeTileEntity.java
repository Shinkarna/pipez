package com.nekogan.pipez.blocks.tileentity;

import com.nekogan.pipez.blocks.tileentity.types.FluidPipeType;
import com.nekogan.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FluidPipeTileEntity extends PipeLogicTileEntity {

    public FluidPipeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.FLUID_PIPE.get(), new PipeType[]{FluidPipeType.INSTANCE}, pos, state);
    }

}
