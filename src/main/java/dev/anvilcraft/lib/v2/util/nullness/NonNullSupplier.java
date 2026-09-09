package dev.anvilcraft.lib.v2.util.nullness;

@FunctionalInterface
public interface NonNullSupplier<T> {

    T get();
}
