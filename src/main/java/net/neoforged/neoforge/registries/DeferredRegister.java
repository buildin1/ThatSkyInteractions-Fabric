package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Supplier;

/**
 * NeoForge API 兼容层：立即注册进原版注册表（Fabric 无注册冻结时序差异问题，
 * 调用发生在 ModInitializer 中，时机合法）。
 */
public final class DeferredRegister<T> {

    private final ResourceKey<Registry<T>> registryKey;
    private final Registry<T> registry;
    private final String modid;

    private DeferredRegister(ResourceKey<Registry<T>> registryKey, Registry<T> registry, String modid) {
        this.registryKey = registryKey;
        this.registry = registry;
        this.modid = modid;
    }

    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> create(ResourceKey<Registry<T>> registryKey, String modid) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        return new DeferredRegister<>(registryKey, registry, modid);
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String modid) {
        return new DeferredRegister<>((ResourceKey) registry.key(), registry, modid);
    }

    public <I extends T> Supplier<I> register(String name, Supplier<I> supplier) {
        Identifier id = Identifier.fromNamespaceAndPath(this.modid, name);
        Registry.register(this.registry, id, supplier.get());
        return supplier;
    }

    public void register(IEventBus bus) {
        // Fabric：注册已在 register() 内即时完成
    }
}
