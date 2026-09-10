package net.neoforged.neoforge.registries;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/**
 * NeoForge API 兼容层：延迟注册持有者。
 */
public class DeferredHolder<R, T extends R> implements Supplier<T> {

    private final ResourceKey<R> registryKey;
    private final ResourceLocation id;
    private final Supplier<T> supplier;

    public DeferredHolder(ResourceKey<R> registryKey, ResourceLocation id, Supplier<T> supplier) {
        this.registryKey = registryKey;
        this.id = id;
        this.supplier = supplier;
    }

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation id, Supplier<T> supplier) {
        return new DeferredHolder<>(null, id, supplier);
    }

    @Override
    public T get() {
        return this.supplier.get();
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public ResourceKey<T> getKey() {
        return ResourceKey.create((ResourceKey) this.registryKey, this.id);
    }

    public Holder<T> asHolder() {
        throw new UnsupportedOperationException("asHolder is not supported in the Fabric compat layer");
    }

    public ResourceKey<R> getRegistryKey() {
        return this.registryKey;
    }
}
