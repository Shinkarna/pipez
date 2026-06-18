package com.nekogan.pipez.gui;

import com.nekogan.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.core.Direction;

public interface IPipeContainer {

    PipeLogicTileEntity getPipe();

    Direction getSide();

}
