package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import net.minecraft.client.KeyboardHandler;
import dev.anvilcraft.lib.v2.input.KeyEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.20.1 的 {@code keyPress(long, int key, int scancode, int action, int modifiers)}
 * 仍是散参数（26.x 已经打包成 KeyEvent 记录），这里在入口处组回 KeyEvent，
 * 让 InputEvent.Key 的监听器不用改。
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void tsi$onKeyPress(
            final long          windowPointer,
            final int           key,
            final int           scancode,
            final int           action,
            final int           modifiers,
            final CallbackInfo  ci
    ) {
        var keyEvent = new InputEvent.Key(new KeyEvent(key, scancode, modifiers), action);
        NeoForge.EVENT_BUS.post(keyEvent);
        if (keyEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
