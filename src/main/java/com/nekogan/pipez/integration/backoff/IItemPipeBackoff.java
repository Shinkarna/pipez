package com.nekogan.pipez.integration.backoff;

import net.minecraft.core.Direction;

public interface IItemPipeBackoff {
    long pipezCe$getNextActiveTick(Direction side);
    void pipezCe$setNextActiveTick(Direction side, long tick);
    int pipezCe$getBackoffDelay(Direction side);
    void pipezCe$setBackoffDelay(Direction side, int delay);
}
