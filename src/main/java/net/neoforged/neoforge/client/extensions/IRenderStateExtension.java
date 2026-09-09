package net.neoforged.neoforge.client.extensions;

import net.minecraft.util.context.ContextKey;
import org.jspecify.annotations.Nullable;

/**
 * NeoForge API 兼容层：渲染状态扩展（ContextKey 数据存取）。
 * 由 {@code feature.mixin.vanilla.client.EntityRenderStateMixin} 提供存储实现。
 */
public interface IRenderStateExtension {

    @Nullable
    <T> T getRenderData(ContextKey<T> key);

    <T> void setRenderData(ContextKey<T> key, T value);
}
