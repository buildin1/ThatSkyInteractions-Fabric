package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void tsi$onKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        var keyEvent = new InputEvent.Key(event, action);
        NeoForge.EVENT_BUS.post(keyEvent);
        if (keyEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
