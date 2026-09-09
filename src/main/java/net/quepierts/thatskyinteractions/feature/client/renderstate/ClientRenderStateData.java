package net.quepierts.thatskyinteractions.feature.client.renderstate;

import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;

/**
 * 渲染状态上的客户端附加数据键（供 Mixin 在模型装配阶段取回实体）。
 */
public final class ClientRenderStateData {

    /** 当前渲染状态对应的实体引用 */
    public static final ContextKey<Entity> ENTITY = RenderStateModifiers.create("render_entity_ref");

    private ClientRenderStateData() {}
}
