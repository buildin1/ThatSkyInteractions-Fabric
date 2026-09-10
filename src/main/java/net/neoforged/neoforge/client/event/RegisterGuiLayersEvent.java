package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.ArrayList;
import java.util.List;

public class RegisterGuiLayersEvent extends Event {

    public record LayerEntry(ResourceLocation id, GuiLayer layer, boolean aboveAll) {}

    private final List<LayerEntry> entries = new ArrayList<>();

    public void registerAboveAll(ResourceLocation id, GuiLayer layer) {
        this.entries.add(new LayerEntry(id, layer, true));
    }

    public List<LayerEntry> getEntries() {
        return this.entries;
    }
}
