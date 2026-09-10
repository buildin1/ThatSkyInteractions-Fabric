package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.quepierts.thatskyinteractions.feature.client.control.event.LocalPlayerTurnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @WrapOperation(
            method = "turnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
            )
    )
    private void a4j$turnPlayer(
            final LocalPlayer instance,
            final double xo,
            final double yo,
            final Operation<Void> original
    ) {
        final var event = new LocalPlayerTurnEvent(instance, xo, yo);
        NeoForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            instance.turn(event.getXo(), event.getYo());
        }
    }

    /**
     * ScreenEvent.MouseButtonPressed.Post 事件源（含 wasClickHandled）。
     *
     * <p>1.20.1 的按键回调叫 {@code onPress}（26.x 是 {@code onButton}），而且里面那句
     * {@code screen.mouseClicked(...)} 被包在 {@code Screen.wrapScreenError} 的 lambda 中，
     * 编译后是独立的合成方法。所以这里直接打在该 lambda（intermediary 名 {@code method_1611}，
     * 1.20.1 固定不变；同类里的 {@code method_1605} 是 mouseReleased 那条）上。
     */
    @WrapOperation(
            method = "method_1611([ZLnet/minecraft/client/gui/screens/Screen;DDI)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"
            )
    )
    private static boolean tsi$afterScreenClick(
            final Screen                screen,
            final double                mouseX,
            final double                mouseY,
            final int                   button,
            final Operation<Boolean>    original
    ) {
        final boolean handled = original.call(screen, mouseX, mouseY, button);
        NeoForge.EVENTS.post(new ScreenEvent.MouseButtonPressed.Post(
                screen,
                mouseX,
                mouseY,
                button,
                handled
        ));
        return handled;
    }

    /**
     * InputEvent.MouseScrollingEvent 事件源。1.20.1 的滚轮逻辑内联在 onScroll 里、
     * 没有 ScrollWheelHandler，于是挂在「无 GUI、有玩家」那条分支的入口：
     * 事件被取消时直接 cancel 掉整个 onScroll，等价于原版不切换快捷栏。
     * 互动镜头缩放（ClientCameraSystem#onScroll）依赖它。
     */
    @Inject(
            method = "onScroll",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MouseHandler;accumulatedScroll:D",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void tsi$onScroll(
            final long          window,
            final double        xOffset,
            final double        yOffset,
            final CallbackInfo  ci
    ) {
        final var self  = (MouseHandler) (Object) this;
        final var event = new InputEvent.MouseScrollingEvent(xOffset, yOffset, self.xpos(), self.ypos());

        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
