package net.neoforged.neoforge.client.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.ArrayList;
import java.util.List;

public class RegisterGuiLayersEvent extends Event {

    public record LayerEntry(Identifier id, GuiLayer layer, boolean aboveAll) {}

    private final List<LayerEntry> entries = new ArrayList<>();

    public void registerAboveAll(Identifier id, GuiLayer layer) {
        this.entries.add(new LayerEntry(id, layer, true));
    }

    public List<LayerEntry> getEntries() {
        return this.entries;
    }
}
