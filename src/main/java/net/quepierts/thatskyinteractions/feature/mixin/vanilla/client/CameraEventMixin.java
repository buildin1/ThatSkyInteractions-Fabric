package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * ViewportEvent.ComputeCameraAngles 与 CalculateDetachedCameraDistanceEvent 的事件源。
 */
@Mixin(Camera.class)
public abstract class CameraEventMixin {

    @WrapOperation(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setRotation(FF)V"
            )
    )
    private void tsi$computeCameraAngles(
            Camera instance,
            float yRot,
            float xRot,
            Operation<Void> original
    ) {
        var event = new ViewportEvent.ComputeCameraAngles(instance, yRot, xRot, 0.0F);
        NeoForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            original.call(instance, event.getYaw(), event.getPitch());
        }
    }

    @WrapOperation(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"
            )
    )
    private float tsi$calculateDetachedDistance(
            Camera instance,
            float distance,
            Operation<Float> original
    ) {
        float result = original.call(instance, distance);
        var event = new CalculateDetachedCameraDistanceEvent(instance, result);
        NeoForge.EVENT_BUS.post(event);
        return (float) event.getDistance();
    }
}
