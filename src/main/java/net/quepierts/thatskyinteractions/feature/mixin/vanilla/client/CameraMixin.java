package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.quepierts.thatskyinteractions.feature.animation.PlayerAnimationSystem;
import net.quepierts.thatskyinteractions.feature.client.animation.PlayerAnimationHook;
import net.quepierts.thatskyinteractions.feature.client.control.ClientCameraSystem;
import net.quepierts.thatskyinteractions.feature.client.control.event.CameraAlignEvent;
import net.quepierts.thatskyinteractions.feature.client.control.event.ComputeCameraPositionEvent;
import org.joml.*;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.20.1 的相机装配方法是 {@code setup(BlockGetter, Entity, boolean, boolean, float)}，
 * 26.x 是 {@code alignWithEntity(float)}；{@code getMaxZoom} 在 1.20.1 收发都是 double。
 * 另外 1.20.1 的 Camera 没有 {@code matrixPropertiesDirty}，也没有静态的
 * {@code FORWARDS/UP/LEFT} 常量——方向向量在 {@code setRotation} 里就地重算。
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private @Nullable Entity entity;

    @Shadow
    protected abstract void setPosition(final double x, final double y, final double z);

    @Shadow
    private Vec3 position;

    @Shadow
    protected abstract void setRotation(final float yRot, final float xRot);

    @Shadow
    private float xRot;

    @Shadow
    private float yRot;

    @Shadow
    @org.spongepowered.asm.mixin.Final
    private Quaternionf rotation;

    @Shadow
    @org.spongepowered.asm.mixin.Final
    private Vector3f forwards;

    @Shadow
    @org.spongepowered.asm.mixin.Final
    private Vector3f up;

    @Shadow
    @org.spongepowered.asm.mixin.Final
    private Vector3f left;

    @Unique
    private float tsi$roll;

    /**
     * NeoForge 行为：两参 setRotation 会把 roll 重置为 0（其两参版本是
     * {@code setRotation(yRot, xRot, 0F)} 的包装）。缺少这一步会导致动画结束后
     * roll 残留并在下一次动作上累加，表现为视角持续倾斜。
     */
    @Inject(method = "setRotation(FF)V", at = @At("TAIL"))
    private void tsi$resetRoll(final float yRot, final float xRot, final CallbackInfo ci) {
        this.tsi$roll = 0.0F;
    }

    /**
     * NeoForge 的三参 setRotation 等价实现：在原版两参基础上叠加 roll，
     * 并按 1.20.1 的方式就地重算方向向量（否则视角/剔除会出现错位）。
     */
    @Unique
    private void tsi$setRotation(final float yRot, final float xRot, final float roll) {
        this.setRotation(yRot, xRot);
        this.tsi$roll = roll;
        if (roll != 0.0F) {
            this.rotation.rotateZ(-roll * net.minecraft.util.Mth.DEG_TO_RAD);
            this.forwards.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
            this.up.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
            this.left.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
        }
    }

    @Inject(
            method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            at = @At("TAIL")
    )
    private void a4j$animate(
            final BlockGetter   level,
            final Entity        cameraEntity,
            final boolean       detached,
            final boolean       mirrored,
            final float         partialTicks,
            final CallbackInfo  ci
    ) {
        if (!(this.entity instanceof Player avatar)) {
            return;
        }

        final var data          = PlayerAnimationSystem.getAnimationData(avatar);
        final var controller    = data.getController();

        final var position = new Vector3d(this.position.x(), this.position.y(), this.position.z());
        final var rotation = new Vector3f(this.xRot, this.yRot, this.tsi$roll);
        final var modified = PlayerAnimationHook.onSetupCameraAnimation(
                controller,
                (Camera) (Object) this,
                position,
                rotation,
                partialTicks
        );

        if (modified) {
            this.setPosition(position.x(), position.y(), position.z());
            this.tsi$setRotation(rotation.y(), rotation.x(), rotation.z());
        }
    }

    @Inject(
            method = "getMaxZoom(D)D",
            at = @At("RETURN")
    )
    private void a4j$getMaxZoom(
            final double                            distance,
            final CallbackInfoReturnable<Double>    cir
    ) {
        ClientCameraSystem.updateMaxZoom((float) cir.getReturnValueD());
    }

    @WrapOperation(
            method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"
            )
    )
    private void a4j$setPosition(
            final Camera            instance,
            final double            x,
            final double            y,
            final double            z,
            final Operation<Void>   original
    ) {

        final var event = new ComputeCameraPositionEvent(this.entity, x, y, z);
        NeoForge.EVENT_BUS.post(event);
        original.call(instance, event.getX(), event.getY(), event.getZ());

    }

    @Inject(
            method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            at = @At("HEAD")
    )
    private void a4j$alignWithEntity(
            final BlockGetter   level,
            final Entity        cameraEntity,
            final boolean       detached,
            final boolean       mirrored,
            final float         partialTicks,
            final CallbackInfo  ci
    ) {
        // HEAD 处 this.entity 还没赋值（1.20.1 在 setup 内部才写），用形参判断
        if (cameraEntity != Minecraft.getInstance().player) {
            return;
        }

        final var event = new CameraAlignEvent(Minecraft.getInstance().player, partialTicks);
        NeoForge.EVENT_BUS.post(event);
    }

}
