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
 *
 * <p>1.20.1 的相机装配方法叫 {@code Camera#setup(BlockGetter, Entity, boolean, boolean, float)}
 * （26.x 是 {@code alignWithEntity(float)}），且 {@code getMaxZoom} 收发都是 double。
 *
 * <p>{@code setup} 里会调用 {@code setRotation} 最多三次（基础视角、第三人称镜像、睡眠朝向），
 * 这里只包裹第一次（{@code ordinal = 0}），即"计算相机角度"那一次，保证事件每帧只触发一次；
 * 之后的镜像/睡眠修正属于原版行为，不应再走一遍监听器。
 */
@Mixin(Camera.class)
public abstract class CameraEventMixin {

    @WrapOperation(
            method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setRotation(FF)V",
                    ordinal = 0
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
            method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"
            )
    )
    private double tsi$calculateDetachedDistance(
            Camera instance,
            double distance,
            Operation<Double> original
    ) {
        double result = original.call(instance, distance);
        var event = new CalculateDetachedCameraDistanceEvent(instance, (float) result);
        NeoForge.EVENT_BUS.post(event);
        return event.getDistance();
    }
}
