package dev.anvilcraft.lib.v2.registrum.builders;

import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import dev.anvilcraft.lib.v2.util.nullness.NonNullSupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * AnvilLib API 兼容层：仅保留本 mod 使用的成员。
 */
public interface Builder<R, T extends R, P, S extends Builder<R, T, P, S>> extends NonNullSupplier<RegistryEntry<R, T>> {

    RegistryEntry<R, T> register();

    AbstractRegistrum<?> getOwner();

    String getName();

    ResourceKey<? extends Registry<R>> getRegistryKey();
}
