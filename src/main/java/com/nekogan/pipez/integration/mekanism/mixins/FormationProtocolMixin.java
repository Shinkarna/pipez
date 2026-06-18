package com.nekogan.pipez.integration.mekanism.mixins;

import com.nekogan.pipez.integration.mekanism.IValve;
import com.llamalad7.mixinextras.sugar.Local;
import mekanism.common.lib.multiblock.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FormationProtocol.class, remap = false)
public abstract class FormationProtocolMixin<T extends MultiblockData> {
    @Shadow
    @Final
    private IMultiblock<T> pointer;

    /**
     * Update pipez when a multi-block is assembled.
     */
    @Inject(method = "doUpdate", at = @At("RETURN"))
    public void doUpdate(
            CallbackInfoReturnable<FormationProtocol.FormationResult> cir,
            @Local(name = "structureFound") T structureFound) {
        var ret = cir.getReturnValue();
        if (ret != FormationProtocol.FormationResult.SUCCESS || structureFound == null) return;

        var level = pointer.getTileWorld();
        if (level == null
                || level.isClientSide
                || structureFound.locations == null
                || structureFound.locations.isEmpty()) return;

        structureFound.locations.stream()
                .map(level::getBlockEntity)
                .filter(IValve.class::isInstance)
                .map(IValve.class::cast)
                .forEach(v -> v.pipezCe$updatePipezCache((BlockEntity) v, Direction.values()));
    }
}
