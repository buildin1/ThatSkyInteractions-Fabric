package dev.anvilcraft.lib.v2.registrum.util.entry;

import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * AnvilLib API 兼容层：注册表条目包装。
 */
public class RegistryEntry<R, S extends R> extends DeferredHolder<R, S> {

    public RegistryEntry(AbstractRegistrum<?> owner, DeferredHolder<R, S> delegate) {
        super(delegate.getRegistryKey(), delegate.getId(), delegate);
    }

    public static <X> X cast(Class<? super X> clazz, RegistryEntry<?, ?> entry) {
        return (X) entry.get();
    }
}
