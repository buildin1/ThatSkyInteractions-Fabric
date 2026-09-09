package net.neoforged.neoforge.event;

import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class AddServerReloadListenersEvent extends Event {

    private final ReloadableServerRegistries.Holder registries;
    private final List<ListenerEntry> listeners = new ArrayList<>();

    public AddServerReloadListenersEvent(ReloadableServerRegistries.Holder registries) {
        this.registries = registries;
    }

    public ReloadableServerRegistries.Holder getRegistries() {
        return this.registries;
    }

    public void addListener(Identifier identifier, PreparableReloadListener listener) {
        this.listeners.add(new ListenerEntry(identifier, listener));
    }

    public List<ListenerEntry> getListeners() {
        return this.listeners;
    }

    public record ListenerEntry(Identifier identifier, PreparableReloadListener listener) {}
}
