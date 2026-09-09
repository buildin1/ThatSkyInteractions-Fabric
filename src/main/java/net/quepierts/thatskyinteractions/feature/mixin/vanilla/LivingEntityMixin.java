package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.quepierts.thatskyinteractions.feature.bond.PlayerBondSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(
            method = "stopRiding",
            at = @At("TAIL")
    )
    private void tsi$onStopRiding(
            final CallbackInfo ci
    ) {

        if ((Object) this instanceof ServerPlayer self) {
            PlayerBondSystem.unRide(self);
            self.refreshDimensions();
        }

    }
}
