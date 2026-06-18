package com.nekogan.pipez.blocks.tileentity;

import com.nekogan.pipez.blocks.tileentity.types.ItemPipeType;
import com.nekogan.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ItemPipeTileEntity extends PipeLogicTileEntity {

    public ItemPipeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.ITEM_PIPE.get(), new PipeType[]{ItemPipeType.INSTANCE}, pos, state);
    }
}
