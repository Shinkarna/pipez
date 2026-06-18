package com.nekogan.pipez.integration.mekanism.dummy;

import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import org.jetbrains.annotations.NotNull;

public class DummyGasHandler implements IGasHandler {
    public static final DummyGasHandler INSTANCE = new DummyGasHandler();

    @Override
    public int getTanks() {
        return 0;
    }

    @Override
    public @NotNull GasStack getChemicalInTank(int tank) {
        return GasStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, @NotNull GasStack stack) {}

    @Override
    public long getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isValid(int tank, @NotNull GasStack stack) {
        return false;
    }

    @Override
    public @NotNull GasStack insertChemical(int tank, @NotNull GasStack stack, @NotNull Action action) {
        return stack;
    }

    @Override
    public @NotNull GasStack extractChemical(int tank, long amount, @NotNull Action action) {
        return GasStack.EMPTY;
    }
}
