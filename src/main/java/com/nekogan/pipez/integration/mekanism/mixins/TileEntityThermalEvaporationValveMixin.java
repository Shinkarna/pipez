package com.nekogan.pipez.integration.mekanism.mixins;

import com.nekogan.pipez.integration.mekanism.IValve;
import com.nekogan.pipez.integration.mekanism.dummy.Dummies;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationValve;
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

@Mixin(value = TileEntityThermalEvaporationValve.class)
public abstract class TileEntityThermalEvaporationValveMixin extends CapabilityTileEntity implements IValve {
    @Unique
    private static final Capability<?>[] pipezCe$caps = {
        ForgeCapabilities.ITEM_HANDLER, ForgeCapabilities.FLUID_HANDLER
    };

    public TileEntityThermalEvaporationValveMixin(
            TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state) {
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
