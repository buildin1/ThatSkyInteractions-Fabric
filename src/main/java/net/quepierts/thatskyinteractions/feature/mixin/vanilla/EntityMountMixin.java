package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.quepierts.thatskyinteractions.feature.bond.PlayerBondSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMountMixin {

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("HEAD"), cancellable = true)
    private void tsi$onStartRiding(Entity vehicle, boolean force, boolean suppressCallbacks, CallbackInfoReturnable<Boolean> cir) {
        var event = new EntityMountEvent((Entity) (Object) this, vehicle, true);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "stopRiding", at = @At("HEAD"), cancellable = true)
    private void tsi$onStopRiding(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.getVehicle() == null) {
            return;
        }
        var event = new EntityMountEvent(self, self.getVehicle(), false);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    /** 背起状态下乘客的下车位置（原版在 dismountVehicle 内调用本方法，此处直接覆写结果） */
    @Inject(method = "getDismountLocationForPassenger", at = @At("HEAD"), cancellable = true)
    private void tsi$getDismountLocation(LivingEntity passenger, CallbackInfoReturnable<Vec3> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof ServerPlayer player) {
            final var attachment = PlayerBondSystem.getAttachment(player);
            if (attachment.getCarry().getCarried() == passenger) {
                final var yRot = player.getYRot() + 90.0F;
                final float f = Mth.cos(yRot * Mth.DEG_TO_RAD);
                final float f1 = Mth.sin(yRot * Mth.DEG_TO_RAD);
                cir.setReturnValue(player.position().add(f, 0, f1));
            }
        }
    }
}
