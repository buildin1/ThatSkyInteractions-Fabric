package net.neoforged.neoforge.common;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventBus;
import net.neoforged.bus.api.IEventBus;

public final class NeoForge {

    public static final IEventBus EVENTS = new EventBus();
    public static final IEventBus EVENT_BUS = EVENTS;

    private NeoForge() {}

    public static <T extends Event> T post(T event) {
        return EVENTS.post(event);
    }
}
