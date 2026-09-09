package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.extensions.IRenderStateExtension;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

/**
 * 为原版渲染状态提供 ContextKey 数据存储（等价 NeoForge 的 IRenderStateExtension 实现）。
 */
@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements IRenderStateExtension {

    @Unique
    private Map<ContextKey<?>, Object> tsi$renderData;

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> T getRenderData(ContextKey<T> key) {
        return this.tsi$renderData == null ? null : (T) this.tsi$renderData.get(key);
    }

    @Override
    public <T> void setRenderData(ContextKey<T> key, T value) {
        if (this.tsi$renderData == null) {
            this.tsi$renderData = new HashMap<>();
        }
        this.tsi$renderData.put(key, value);
    }
}
