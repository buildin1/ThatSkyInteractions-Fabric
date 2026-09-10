package net.neoforged.neoforge.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class AddServerReloadListenersEvent extends Event {

    private final Object registries;
    private final List<ListenerEntry> listeners = new ArrayList<>();

    public AddServerReloadListenersEvent(Object registries) {
        this.registries = registries;
    }

    public Object getRegistries() {
        return this.registries;
    }

    public void addListener(ResourceLocation identifier, PreparableReloadListener listener) {
        this.listeners.add(new ListenerEntry(identifier, listener));
    }

    public List<ListenerEntry> getListeners() {
        return this.listeners;
    }

    public record ListenerEntry(ResourceLocation identifier, PreparableReloadListener listener) {}
}
