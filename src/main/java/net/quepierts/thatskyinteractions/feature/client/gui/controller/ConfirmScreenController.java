package net.quepierts.thatskyinteractions.feature.client.gui.controller;

import net.minecraft.client.Minecraft;
import net.quepierts.thatskyinteractions.feature.gui.ConfirmData;

public class ConfirmScreenController extends ScreenController<ConfirmData> {

    public ConfirmScreenController(final ConfirmData data) {
        super(data);
    }

    public void confirm() {
        this.getModel().confirm().run();
        net.quepierts.thatskyinteractions.internal.GuiLayerStack.pop();
    }

    public void cancel() {
        this.getModel().cancel().run();
        net.quepierts.thatskyinteractions.internal.GuiLayerStack.pop();
    }

}
