package dev.anvilcraft.lib.v2.util.nullness;

@FunctionalInterface
public interface NonNullFunction<T, R> {

    R apply(T t);
}
