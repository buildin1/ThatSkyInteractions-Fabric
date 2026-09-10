package dev.anvilcraft.lib.v2.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.ShaderInstance;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * AnvilLib API 兼容层：SDF 着色器持有者。
 *
 * <p>26.x 分支用的是 {@code RenderPipeline} + UBO；1.20.1 没有那套 GPU 抽象，
 * 这里退回到原版的 {@link ShaderInstance}，由 Fabric 的
 * {@code CoreShaderRegistrationCallback} 在资源重载时创建。
 */
public final class ALRPipelines {

    private static @Nullable ShaderInstance sdfGraphics;

    /** 回调只挂一次。 */
    private static boolean registered;

    /**
     * Fabric API 0.92 的 {@code CoreShaderRegistrationCallback} 每次资源重载会把监听器调用
     * 一次<b>每个原版核心着色器</b>（1.20.1 实测 59 次），而不是一次。若在监听器里无条件
     * {@code context.register(...)}，同一个着色器会被编译 59 份、泄漏 GL 程序对象，
     * 且最终留在字段里的是哪一份不确定。同一次重载传进来的 context 是同一个对象，
     * 据此做「每轮重载只注册一次」的守卫。
     */
    private static @Nullable Object lastContext;

    private ALRPipelines() {}

    /** 由客户端引导在 mod 初始化阶段调用一次。 */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        CoreShaderRegistrationCallback.EVENT.register(context -> {
            if (context == lastContext) {
                return;
            }
            lastContext = context;
            try {
                context.register(
                        AnvilLibRendering.location("sdf_graphics"),
                        com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR_TEX,
                        shader -> ALRPipelines.sdfGraphics = shader
                );
            } catch (IOException e) {
                throw new IllegalStateException("Failed to register SDF core shader", e);
            }
        });
    }

    public static @Nullable ShaderInstance sdfGraphics() {
        return sdfGraphics;
    }
}
