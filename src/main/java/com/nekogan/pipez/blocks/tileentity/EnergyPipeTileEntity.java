package com.nekogan.pipez.blocks.tileentity;

import com.nekogan.pipez.blocks.tileentity.types.EnergyPipeType;
import com.nekogan.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class EnergyPipeTileEntity extends PipeLogicTileEntity {

    public EnergyPipeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.ENERGY_PIPE.get(), new PipeType[]{EnergyPipeType.INSTANCE}, pos, state);
    }

}
