package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface GuiLayer {

    void render(GuiGraphics graphics, float partialTick);
}
