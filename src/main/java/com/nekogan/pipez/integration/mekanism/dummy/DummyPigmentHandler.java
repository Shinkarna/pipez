package com.nekogan.pipez.integration.mekanism.dummy;

import mekanism.api.Action;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.PigmentStack;
import org.jetbrains.annotations.NotNull;

public class DummyPigmentHandler implements IPigmentHandler {
    public static final DummyPigmentHandler INSTANCE = new DummyPigmentHandler();

    @Override
    public int getTanks() {
        return 0;
    }

    @Override
    public @NotNull PigmentStack getChemicalInTank(int tank) {
        return PigmentStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, @NotNull PigmentStack stack) {}

    @Override
    public long getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isValid(int tank, @NotNull PigmentStack stack) {
        return false;
    }

    @Override
    public @NotNull PigmentStack insertChemical(int tank, @NotNull PigmentStack stack, @NotNull Action action) {
        return stack;
    }

    @Override
    public @NotNull PigmentStack extractChemical(int tank, long amount, @NotNull Action action) {
        return PigmentStack.EMPTY;
    }
}
