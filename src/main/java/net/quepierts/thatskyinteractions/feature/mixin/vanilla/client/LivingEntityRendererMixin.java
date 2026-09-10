package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.quepierts.thatskyinteractions.feature.client.animation.PlayerAnimationHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 根骨骼变换。26.x 挂在 ModelFeatureRenderer 上，1.20.1 没有那套特性渲染体系，
 * 改挂到 LivingEntityRenderer#render 里 setupAnim 之后——同样是「姿态已算好、尚未绘制」的时机。
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void a4j$renderModel(
            final LivingEntity      entity,
            final float             entityYaw,
            final float             partialTicks,
            final PoseStack         poseStack,
            final MultiBufferSource buffer,
            final int               packedLight,
            final CallbackInfo      ci
    ) {
        PlayerAnimationHook.onSetupRootAnimation(entity, poseStack);
    }
}
