package com.nekogan.pipez.integration.mekanism.dummy;

import mekanism.api.Action;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.InfusionStack;
import org.jetbrains.annotations.NotNull;

public class DummyInfusionHandler implements IInfusionHandler {
    public static final DummyInfusionHandler INSTANCE = new DummyInfusionHandler();

    @Override
    public int getTanks() {
        return 0;
    }

    @Override
    public @NotNull InfusionStack getChemicalInTank(int tank) {
        return InfusionStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, @NotNull InfusionStack stack) {}

    @Override
    public long getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isValid(int tank, @NotNull InfusionStack stack) {
        return false;
    }

    @Override
    public @NotNull InfusionStack insertChemical(int tank, @NotNull InfusionStack stack, @NotNull Action action) {
        return stack;
    }

    @Override
    public @NotNull InfusionStack extractChemical(int tank, long amount, @NotNull Action action) {
        return InfusionStack.EMPTY;
    }
}
