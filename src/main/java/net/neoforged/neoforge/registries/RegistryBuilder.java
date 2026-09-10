package net.neoforged.neoforge.registries;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NeoForge API 兼容层：自定义注册表构建器，底层为 FabricRegistryBuilder。
 * 静态注册表映射供 Registrum 构建器注册时反查注册表实例。
 */
public final class RegistryBuilder<R> {

    private static final Map<ResourceKey<?>, Registry<?>> CUSTOM_REGISTRIES = new ConcurrentHashMap<>();

    private final ResourceKey<Registry<R>> key;
    private boolean sync = false;

    public RegistryBuilder(ResourceKey<Registry<R>> key) {
        this.key = key;
    }

    public RegistryBuilder<R> sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public Registry<R> create() {
        // 1.20.1 的 FabricRegistryBuilder 需要显式给出注册表实例
        FabricRegistryBuilder<R, MappedRegistry<R>> builder =
                FabricRegistryBuilder.from(new MappedRegistry<R>(this.key, com.mojang.serialization.Lifecycle.stable(), false));
        if (this.sync) {
            builder.attribute(RegistryAttribute.SYNCED);
        }
        Registry<R> registry = builder.buildAndRegister();
        CUSTOM_REGISTRIES.put(this.key, registry);
        return registry;
    }

    @SuppressWarnings("unchecked")
    public static <R> Registry<R> getCustomRegistry(ResourceKey<Registry<R>> key) {
        return (Registry<R>) CUSTOM_REGISTRIES.get(key);
    }

    public static ResourceLocation defaultId() {
        return new ResourceLocation("minecraft", "default");
    }
}
