package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import net.quepierts.thatskyinteractions.feature.client.ClientCameraEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 互动镜头效果：在相机 FOV 上叠加客户端脉冲。
 */
@Mixin(Camera.class)
public class CameraFovMixin {

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private float tsi$interactionFov(final float original) {
        return original * ClientCameraEffects.fovFactor();
    }
}
