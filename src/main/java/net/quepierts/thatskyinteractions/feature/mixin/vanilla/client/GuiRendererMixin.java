package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.anvilcraft.lib.v2.rendering.sdf.SdfGraphics;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * SDF GUI 元素渲染支持（等价 AnvilLib GuiRendererMixin 的必要部分）：
 * 1. 每帧重置 SDF 参数缓冲索引（否则索引溢出会崩溃）；
 * 2. 在每个 GUI 渲染 pass 上绑定 SDF 参数 UBO。
 * <p>
 * 所有 SDF 元素共用同一 UBO，按顶点 UV1.x 索引，因此每 pass 绑定一次即等价于原实现。
 */
@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Inject(method = "addElementsToMeshes", at = @At("HEAD"))
    private void tsi$frameStart(GuiRenderState.TraverseRange range, CallbackInfo ci) {
        SdfGraphics.flush();
    }

    @WrapOperation(
            method = "executeDrawRange",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;bindDefaultUniforms(Lcom/mojang/blaze3d/systems/RenderPass;)V"
            )
    )
    private void tsi$bindSdfUniforms(RenderPass pass, Operation<Void> original) {
        original.call(pass);
        SdfGraphics.bind(pass);
    }
}
