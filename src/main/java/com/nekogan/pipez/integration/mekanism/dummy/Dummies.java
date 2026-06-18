package com.nekogan.pipez.integration.mekanism.dummy;

import com.nekogan.pipez.utils.DummyFluidHandler;
import com.nekogan.pipez.utils.DummyItemHandler;
import java.util.HashMap;
import java.util.Map;
import mekanism.common.capabilities.Capabilities;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class Dummies {
    public static Map<Capability<?>, Object> MAP = new HashMap<>();

    static {
        MAP.put(ForgeCapabilities.ITEM_HANDLER, DummyItemHandler.INSTANCE);
        MAP.put(ForgeCapabilities.FLUID_HANDLER, DummyFluidHandler.INSTANCE);
        MAP.put(ForgeCapabilities.ENERGY, DummyEnergyStorage.INSTANCE);
        MAP.put(Capabilities.GAS_HANDLER, DummyGasHandler.INSTANCE);
        MAP.put(Capabilities.INFUSION_HANDLER, DummyInfusionHandler.INSTANCE);
        MAP.put(Capabilities.PIGMENT_HANDLER, DummyPigmentHandler.INSTANCE);
        MAP.put(Capabilities.SLURRY_HANDLER, DummySlurryHandler.INSTANCE);
    }
}
