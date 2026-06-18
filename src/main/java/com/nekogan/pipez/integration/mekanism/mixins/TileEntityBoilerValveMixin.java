package com.nekogan.pipez.integration.mekanism.mixins;

import com.nekogan.pipez.integration.mekanism.IValve;
import com.nekogan.pipez.integration.mekanism.dummy.Dummies;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.tile.multiblock.TileEntityBoilerValve;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TileEntityBoilerValve.class)
public abstract class TileEntityBoilerValveMixin extends CapabilityTileEntity implements IValve {
    @Unique
    private static final Capability<?>[] pipezCe$caps = {
        ForgeCapabilities.FLUID_HANDLER, Capabilities.GAS_HANDLER
    };

    public TileEntityBoilerValveMixin(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        var cap = super.getCapability(capability, side);
        if (!cap.isPresent() && ArrayUtils.contains(pipezCe$caps, capability)) {
            return LazyOptional.of(() -> (T) Dummies.MAP.get(capability));
        }
        return cap;
    }
}
