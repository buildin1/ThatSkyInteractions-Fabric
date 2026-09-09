package net.neoforged.neoforge.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface GuiLayer {

    void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
}
