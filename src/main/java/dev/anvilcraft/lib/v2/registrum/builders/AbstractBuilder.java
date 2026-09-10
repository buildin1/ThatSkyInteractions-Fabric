package dev.anvilcraft.lib.v2.registrum.builders;

import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * AnvilLib API 兼容层：保留本 mod 构建器子类用到的扩展点。
 */
@SuppressWarnings("unused")
public abstract class AbstractBuilder<R, T extends R, P, S extends AbstractBuilder<R, T, P, S>> implements Builder<R, T, P, S> {

    private final AbstractRegistrum<?> owner;
    private final P parent;
    private final String name;
    private final BuilderCallback callback;
    private final ResourceKey<? extends Registry<R>> registryKey;

    protected AbstractBuilder(
            AbstractRegistrum<?> owner,
            P parent,
            String name,
            BuilderCallback callback,
            ResourceKey<? extends Registry<R>> registryKey
    ) {
        this.owner = owner;
        this.parent = parent;
        this.name = name;
        this.callback = callback;
        this.registryKey = registryKey;
    }

    protected abstract T createEntry();

    @Override
    public RegistryEntry<R, T> register() {
        return this.callback.accept(this.name, this.registryKey, this, this::createEntry, this::createEntryWrapper);
    }

    protected RegistryEntry<R, T> createEntryWrapper(DeferredHolder<R, T> delegate) {
        return new RegistryEntry<>(this.getOwner(), delegate);
    }

    @Override
    public AbstractRegistrum<?> getOwner() {
        return this.owner;
    }

    public P getParent() {
        return this.parent;
    }

    @Override
    public String getName() {
        return this.name;
    }

    protected BuilderCallback getCallback() {
        return this.callback;
    }

    @Override
    public ResourceKey<? extends Registry<R>> getRegistryKey() {
        return this.registryKey;
    }

    public ResourceKey<T> getResourceKey() {
        return ResourceKey.create((ResourceKey) this.registryKey, new ResourceLocation(this.owner.getModid(), this.name));
    }

    @Override
    public RegistryEntry<R, T> get() {
        return this.register();
    }
}
