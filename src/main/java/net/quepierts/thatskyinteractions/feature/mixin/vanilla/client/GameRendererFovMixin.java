package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.GameRenderer;
import net.quepierts.thatskyinteractions.feature.client.ClientCameraEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 互动镜头效果：在 FOV 上叠加客户端脉冲。
 *
 * <p>26.x 的 FOV 由 {@code Camera#getFov} 给出；1.20.1 没有这个方法，
 * FOV 计算在 {@code GameRenderer#getFov(Camera, float, boolean)}（返回 double）。
 */
@Mixin(GameRenderer.class)
public class GameRendererFovMixin {

    @ModifyReturnValue(method = "getFov(Lnet/minecraft/client/Camera;FZ)D", at = @At("RETURN"))
    private double tsi$interactionFov(final double original) {
        return original * ClientCameraEffects.fovFactor();
    }
}
