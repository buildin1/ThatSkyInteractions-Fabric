package net.quepierts.thatskyinteractions.feature.client.renderstate;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.quepierts.thatskyinteractions.feature.animation.PlayerAnimationController;
import net.quepierts.thatskyinteractions.feature.animation.PlayerAnimationSystem;
import org.jspecify.annotations.NonNull;

/**
 * 1.20.1 没有 EntityRenderState / RenderStateModifier 那套管线，
 * 动画的每帧推进直接发生在模型装配阶段（PlayerModelMixin#setupAnim），
 * 控制器也直接从实体取，不再需要中间的渲染状态载体。
 */
public class AnimationStateModifier {

    public static final AnimationStateModifier INSTANCE = new AnimationStateModifier();

    /**
     * 推进动画并准备世界→模型局部的变换矩阵，返回该实体的控制器。
     */
    public PlayerAnimationController accept(
            final @NonNull LivingEntity animatable,
            final          float        partialTick
    ) {
        final var data          = PlayerAnimationSystem.getAnimationData(animatable);
        final var controller    = data.getController();

        final var minecraft     = Minecraft.getInstance();
        final var frozen        = minecraft.isPaused();
        final var delta         = frozen ? 0.0f : partialTick;

        controller.update(delta);
        controller.setupToLocal(animatable, partialTick);

        return controller;
    }
}
