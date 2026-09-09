package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.Event;

/**
 * NeoForge API 兼容层：注册表在 RegistryBuilder.create() 中即时创建，
 * 本事件仅为编译兼容保留，register 为空操作。
 */
public final class NewRegistryEvent extends Event {

    public void register(Registry<?> registry) {
        // no-op
    }
}
