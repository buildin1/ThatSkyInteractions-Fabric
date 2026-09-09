package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.quepierts.thatskyinteractions.feature.client.gui.screen.AnimatableScreen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 替代 NeoForge ClientHookMixin：
 * 1. InteractionKeyMappingTriggered（攻击/使用/选取键）；
 * 2. GUI 层切换时 AnimatableScreen 的隐藏/显示动画。
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Nullable
    public Screen screen;

    @Unique
    private final java.util.Deque<Screen> tsi$guiLayers = new java.util.ArrayDeque<>();

    /** NeoForge API: push a GUI layer, keeping the screen below for animation. */
    public void pushGuiLayer(Screen layer) {
        Minecraft self = (Minecraft) (Object) this;
        if (self.screen != null) {
            this.tsi$guiLayers.addLast(self.screen);
        }
        self.setScreen(layer);
    }

    /** NeoForge API: pop the top GUI layer and restore the one below. */
    public void popGuiLayer() {
        Minecraft self = (Minecraft) (Object) this;
        self.setScreen(this.tsi$guiLayers.pollLast());
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void tsi$onAttack(CallbackInfoReturnable<Boolean> cir) {
        var event = InputEvent.InteractionKeyMappingTriggered.attack(net.minecraft.client.Minecraft.getInstance().options.keyAttack);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void tsi$onUseItem(CallbackInfo ci) {
        var event = InputEvent.InteractionKeyMappingTriggered.use(net.minecraft.client.Minecraft.getInstance().options.keyUse);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void tsi$onSetScreen(Screen newScreen, CallbackInfo ci) {
        Screen old = this.screen;
        if (old == null || old == newScreen) {
            return;
        }
        if (newScreen == null) {
            // popGuiLayer：回到游戏前恢复显示
            if (old instanceof AnimatableScreen<?, ?> animatable) {
                animatable.show();
            }
        } else if (old instanceof AnimatableScreen<?, ?> animatable) {
            // pushGuiLayer：新层压入时隐藏
            animatable.hide();
        } else if (newScreen instanceof AnimatableScreen<?, ?> animatable) {
            animatable.show();
        }
    }
}
