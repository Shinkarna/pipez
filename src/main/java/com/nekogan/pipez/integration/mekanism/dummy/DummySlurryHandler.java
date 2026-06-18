package com.nekogan.pipez.integration.mekanism.dummy;

import mekanism.api.Action;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.SlurryStack;
import org.jetbrains.annotations.NotNull;

public class DummySlurryHandler implements ISlurryHandler {
    public static final DummySlurryHandler INSTANCE = new DummySlurryHandler();

    @Override
    public int getTanks() {
        return 0;
    }

    @Override
    public @NotNull SlurryStack getChemicalInTank(int tank) {
        return SlurryStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, @NotNull SlurryStack stack) {}

    @Override
    public long getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isValid(int tank, @NotNull SlurryStack stack) {
        return false;
    }

    @Override
    public @NotNull SlurryStack insertChemical(int tank, @NotNull SlurryStack stack, @NotNull Action action) {
        return stack;
    }

    @Override
    public @NotNull SlurryStack extractChemical(int tank, long amount, @NotNull Action action) {
        return SlurryStack.EMPTY;
    }
}
