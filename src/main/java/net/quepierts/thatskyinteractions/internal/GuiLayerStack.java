package net.quepierts.thatskyinteractions.internal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * NeoForge Minecraft.pushGuiLayer/popGuiLayer 的等价实现（供客户端 GUI 层切换使用）。
 */
public final class GuiLayerStack {

    private static final Deque<Screen> LAYERS = new ArrayDeque<>();

    private GuiLayerStack() {}

    public static void push(Screen layer) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            LAYERS.addLast(minecraft.screen);
        }
        minecraft.setScreen(layer);
    }

    public static void pop() {
        Minecraft.getInstance().setScreen(LAYERS.pollLast());
    }
}
