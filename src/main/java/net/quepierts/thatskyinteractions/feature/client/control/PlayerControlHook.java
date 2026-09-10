package net.quepierts.thatskyinteractions.feature.client.control;

import lombok.experimental.UtilityClass;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.common.NeoForge;
import net.quepierts.thatskyinteractions.feature.client.control.event.LocalPlayerMovedEvent;

/**
 * 1.20.1 的 Input 是一堆公开字段（没有 26.x 的 keyPresses 记录，也没有可写的 moveVector），
 * 所以取消/重定向移动改为直接清零或写回冲量字段。
 */
@UtilityClass
public class PlayerControlHook {

    public static void onUpdatePlayerMotion(
            final Player        player,
            final Input         input
    ) {

        final var moveVector    = input.getMoveVector();
        final var moved         = moveVector.x != 0.0f
                                || moveVector.y != 0.0f
                                || input.jumping
                                || input.shiftKeyDown;

        if (!moved) {
            return;
        }

        final var mEvent        = new LocalPlayerMovedEvent(player, input, moveVector);
        NeoForge.EVENT_BUS      .post(mEvent);

        if (mEvent.isCanceled()) {
            input.leftImpulse       = 0.0f;
            input.forwardImpulse    = 0.0f;
            input.up                = false;
            input.down              = false;
            input.left              = false;
            input.right             = false;
            input.jumping           = false;
            input.shiftKeyDown      = false;
        }

        if (mEvent.isRedirected()) {
            final Vec2 vector       = mEvent.getRedirectVector();
            input.leftImpulse       = vector.x;
            input.forwardImpulse    = vector.y;
        }

    }

}
