package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.quepierts.thatskyinteractions.feature.client.control.event.LocalPlayerTurnEvent;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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

    /** ScreenEvent.MouseButtonPressed.Post 事件源（含 wasClickHandled） */
    @WrapOperation(
            method = "onButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"
            )
    )
    private boolean tsi$afterScreenClick(
            final Screen screen,
            final MouseButtonEvent event,
            final boolean doubleClick,
            final Operation<Boolean> original
    ) {
        final boolean handled = original.call(screen, event, doubleClick);
        NeoForge.EVENTS.post(new ScreenEvent.MouseButtonPressed.Post(
                screen,
                event.x(),
                event.y(),
                event.button(),
                handled
        ));
        return handled;
    }

    /**
     * InputEvent.MouseScrollingEvent 事件源。仅在「无 GUI、有玩家」的分支触发，与 NeoForge 一致；
     * 事件被取消时返回零向量，vanilla 侧会因 {@code wheelXY.x == 0 && wheelXY.y == 0} 直接 return，
     * 等价于 NeoForge 取消后不切换快捷栏。互动镜头缩放（ClientCameraSystem#onScroll）依赖它。
     */
    @WrapOperation(
            method = "onScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/ScrollWheelHandler;onMouseScroll(DD)Lorg/joml/Vector2i;"
            )
    )
    private Vector2i tsi$onScroll(
            final ScrollWheelHandler instance,
            final double scaledX,
            final double scaledY,
            final Operation<Vector2i> original
    ) {
        final var self  = (MouseHandler) (Object) this;
        final var event = new InputEvent.MouseScrollingEvent(scaledX, scaledY, self.xpos(), self.ypos());

        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            return new Vector2i(0, 0);
        }

        return original.call(instance, scaledX, scaledY);
    }
}
