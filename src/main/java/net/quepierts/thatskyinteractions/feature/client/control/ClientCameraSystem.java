package net.quepierts.thatskyinteractions.feature.client.control;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.client.player.Input;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.client.control.event.ComputeCameraPositionEvent;
import net.quepierts.thatskyinteractions.feature.client.control.event.LocalPlayerMovedEvent;
import net.quepierts.thatskyinteractions.feature.client.control.event.LocalPlayerTurnEvent;
import net.quepierts.thatskyinteractions.feature.client.reference.TsiKeys;
import net.quepierts.thatskyinteractions.feature.client.render.GameRendererUpdateEvent;
import org.lwjgl.glfw.GLFW;

@UtilityClass
@EventBusSubscriber(value = Dist.CLIENT, modid = ThatSkyInteractions.MODID)
public class ClientCameraSystem {

    private static final CameraController CONTROLLER = new CameraController();

    public static void updateMaxZoom(final float distance) {
        CONTROLLER.updateMaxZoom(distance);
    }

    @SubscribeEvent
    public static void onUpdate(final GameRendererUpdateEvent event) {
        CONTROLLER.update();
    }

    @SubscribeEvent
    public static void onScroll(final InputEvent.MouseScrollingEvent event) {
        if (CONTROLLER.isLocked()) {
            return;
        }
        event.setCanceled(true);
        CONTROLLER.onScroll((float) event.getScrollDeltaY());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLocalPlayerTurn(final LocalPlayerTurnEvent event) {
        if (CONTROLLER.isLocked()) {
            return;
        }

        if (!net.minecraft.client.gui.screens.Screen.hasAltDown()) {
            CONTROLLER.turn((float) event.getXo(), (float) event.getYo());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(final ViewportEvent.ComputeCameraAngles event) {
        CONTROLLER.onComputeCameraAngles(
                event.getYaw(),
                event.getPitch(),
                Minecraft.getInstance().options.getCameraType().isMirrored(),
                (yaw, pitch) -> {
                    event.setYaw(yaw);
                    event.setPitch(pitch);
                }
        );
    }

    @SubscribeEvent
    public static void onComputeCameraPosition(final ComputeCameraPositionEvent event) {
        CONTROLLER.onComputeCameraPosition(
                event.getX(),
                event.getY(),
                event.getZ(),
                (x, y, z) -> {
                    event.setX(x);
                    event.setY(y);
                    event.setZ(z);
                }
        );
    }

    @SubscribeEvent
    public static void onCalculateCameraDistance(final CalculateDetachedCameraDistanceEvent event) {
        CONTROLLER.onCalculateCameraDistance(
                event.getDistance(),
                event::setDistance
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLocalPlayerMoved(final LocalPlayerMovedEvent event) {
        if (CONTROLLER.isLocked()) {
            return;
        }

        final var cameraYRot    = CONTROLLER.getYRot();
        final var inputVector   = event.getMoveVector();
        // turn player by move vector
        final var player        = event.getPlayer();
        float delta             = (float) Math.toDegrees(Math.atan2(-inputVector.x, inputVector.y));
        float targetYRot        = Mth.wrapDegrees(cameraYRot + delta);
        float difference        = Mth.wrapDegrees(targetYRot - player.getYRot());

        player.turn(difference, 0.0f);

        if (Math.abs(difference) > 90.0f) {
            event.setCanceled(true);
        } else {
            // 1.20.1 的 Input 是可变对象而非记录：直接写冲量字段，只保留前进
            final var origin  = event.getInput();
            final var sprint  = net.minecraft.client.Minecraft.getInstance().options.keySprint.isDown();

            final var forward = new Input();
            forward.up              = true;
            forward.forwardImpulse  = 1.0f;
            forward.jumping         = origin.jumping;
            forward.shiftKeyDown    = origin.shiftKeyDown;

            event.redirect(
                    forward,
                    Vec2.UNIT_Y
            );

            CONTROLLER.onPlayerInput(
                    sprint,
                    targetYRot
            );
        }
    }

    @SubscribeEvent
    public static void onKeyInput(final InputEvent.Key event) {

        if (Minecraft.getInstance().screen != null) {
            return;
        }

        final var matches = TsiKeys.KEY_UNLOCK_CAMERA.matches(event.getKey(), event.getScanCode());

        if (matches && event.getAction() == GLFW.GLFW_PRESS) {
            CONTROLLER.toggle();
        }

    }

    @SubscribeEvent
    public static void onLoggedOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        CONTROLLER.toggle(false);
        CONTROLLER.cleanup();
    }
}