package com.nekogan.pipez.blocks.tileentity;

import com.nekogan.pipez.blocks.tileentity.types.*;
import com.nekogan.pipez.utils.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class UniversalPipeTileEntity extends PipeLogicTileEntity {

    public UniversalPipeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.UNIVERSAL_PIPE.get(), MekanismUtils.isMekanismInstalled() ? new PipeType[]{ItemPipeType.INSTANCE, FluidPipeType.INSTANCE, EnergyPipeType.INSTANCE, GasPipeType.INSTANCE} : new PipeType[]{ItemPipeType.INSTANCE, FluidPipeType.INSTANCE, EnergyPipeType.INSTANCE}, pos, state);
    }

}
