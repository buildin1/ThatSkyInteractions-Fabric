package net.neoforged.bus.api;

import java.util.function.Consumer;

public interface IEventBus {

    void register(Object target);

    void unregister(Object target);

    <T extends Event> void addListener(Consumer<T> listener);

    <T extends Event> T post(T event);
}
