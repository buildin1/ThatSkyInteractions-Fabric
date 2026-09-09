package dev.anvilcraft.lib.v2.registrum.providers;

import net.minecraft.world.entity.EntityType;

/**
 * AnvilLib API 兼容层：datagen provider 类型标记（Fabric 移植使用已生成资源）。
 */
public interface ProviderType<T extends RegistrumProvider> {

    ProviderType<RegistrumTagsProvider.IntrinsicImpl<EntityType<?>>> ENTITY_TAGS = new ProviderType<>() {};
}
