package dev.anvilcraft.lib.v2.registrum.builders;

import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import dev.anvilcraft.lib.v2.util.nullness.NonNullFunction;
import dev.anvilcraft.lib.v2.util.nullness.NonNullSupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * AnvilLib API 兼容层：Fabric 上注册即时完成。
 */
@FunctionalInterface
public interface BuilderCallback {

    <R, T extends R> RegistryEntry<R, T> accept(
            String name,
            ResourceKey<? extends Registry<R>> type,
            Builder<R, T, ?, ?> builder,
            NonNullSupplier<? extends T> factory,
            NonNullFunction<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory
    );

    static <R, T extends R> BuilderCallback immediate(AbstractRegistrum<?> owner) {
        return new BuilderCallback() {
            @Override
            public <R2, T2 extends R2> RegistryEntry<R2, T2> accept(
                    String name,
                    ResourceKey<? extends Registry<R2>> type,
                    Builder<R2, T2, ?, ?> builder,
                    NonNullSupplier<? extends T2> factory,
                    NonNullFunction<DeferredHolder<R2, T2>, ? extends RegistryEntry<R2, T2>> entryFactory
            ) {
                T2 value = factory.get();
                Identifier id = Identifier.fromNamespaceAndPath(owner.getModid(), name);
                Registry<R2> registry = (Registry<R2>) (Registry<?>) RegistryBuilder.getCustomRegistry((net.minecraft.resources.ResourceKey) type);
                if (registry == null) {
                    throw new IllegalStateException("Registry not found for " + type + " (custom registries must be created before registration)");
                }
                Registry.register(registry, id, value);
                DeferredHolder<R2, T2> holder = new DeferredHolder<>((ResourceKey) type, id, () -> value);
                return entryFactory.apply(holder);
            }
        };
    }
}
