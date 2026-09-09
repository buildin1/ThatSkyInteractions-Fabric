package net.quepierts.thatskyinteractions.feature.expression;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.thatskyinteractions.feature.animation.PlayerAnimationSystem;
import net.quepierts.thatskyinteractions.feature.expression.event.PlayerExpressionEvent;
import net.quepierts.thatskyinteractions.feature.expression.packet.ExpressionControlPacket;
import net.quepierts.thatskyinteractions.feature.registry.AnimationLayerTypes;
import org.jspecify.annotations.NonNull;

@Slf4j
@UtilityClass
public class PlayerExpressionSystem {

    public static PlayerExpressionAttachment getAttachment(@NonNull Player player) {
        return PlayerExpressionAttachment.getAttachment(player);
    }

    public static boolean enqueue(
            final @NonNull  ServerPlayer    player,
            final @NonNull  Identifier      expressionId,
            final           int             level
    ) {

        final var attachment    = PlayerExpressionSystem.getAttachment(player);

        if (attachment.isExpressing()) {    // try interrupt
            final var result    = PlayerExpressionSystem.interrupt(player);

            if (result == 0) {
                return false;
            }
        }

        final var manager       = PlayerExpressionManager.getInstance();
        final var expression    = manager.get(expressionId, level);

        if (expression == null) {
            return false;
        }

        if (NeoForge.EVENT_BUS.post(new PlayerExpressionEvent.Enqueue.Pre(player, expressionId, level)).isCanceled()) {
            return false;
        }

        attachment.enqueue(expressionId, level);

        NeoForge.EVENT_BUS.post(new PlayerExpressionEvent.Enqueue.Post(player, expressionId, level));

        return true;

    }

    public static boolean perform(
            final @NonNull  ServerPlayer    player,
            final @NonNull  Identifier      expressionId,
            final           int             level
    ) {
        final var manager       = PlayerExpressionManager.getInstance();
        final var expression    = manager.get(expressionId, level);

        if (expression == null) {
            return false;
        }

        final var attachment    = PlayerExpressionSystem.getAttachment(player);

        if (attachment.isExpressing()) {
            return false;
        }

        if (NeoForge.EVENT_BUS.post(new PlayerExpressionEvent.Perform.Pre(player, expressionId, level)).isCanceled()) {
            return false;
        }

        if (!expression.immediate()) {
            attachment.start(expression, expressionId);
        }
        expression.onPerform(player);

        // 安全网：AbstractAnimationExpression 的所有实现都在 onPerform 里同步启动动画，
        // 表达式也只能靠动画的 Finished 事件解除（期间 isRestrictMotion 默认为 true）。
        // 一旦动画没能启动（数据缺失/数据包损坏），玩家会被永久锁在原地且只能 /kill。
        // 这里在同一 tick 内检查该不变量，失败就立刻回滚，绝不进入不可恢复状态。
        if (!expression.immediate() && expression instanceof AbstractAnimationExpression) {
            final var animation = PlayerAnimationSystem.getExistingAttachment(player);

            if (animation == null || !animation.getController().isPlaying()) {
                log.error(
                        "Expression {} (lv{}) started no animation for {}; aborting to avoid locking the player.",
                        expressionId, level, player.getName().getString()
                );
                attachment.clear();
                return false;
            }
        }

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                ExpressionControlPacket.perform(player.getUUID(), expressionId, level)
        );

        NeoForge.EVENT_BUS.post(new PlayerExpressionEvent.Perform.Post(player, expressionId, level));

        return true;
    }

    public static boolean perform(
            final @NonNull  ServerPlayer    player,
            final @NonNull  Identifier      expressionId
    ) {
        return perform(player, expressionId, 0);
    }

    public static void cancel(@NonNull ServerPlayer player) {
        final var attachment = getAttachment(player);
        final var currentId = attachment.getCurrent();

        if (currentId == null) {
            return;
        }

        attachment.clear();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                ExpressionControlPacket.cancel(player.getUUID(), currentId)
        );
        PlayerAnimationSystem.abort(player, AnimationLayerTypes.DEFAULT.getId());
    }

    public static int interrupt(@NonNull ServerPlayer player) {
        final var attachment = getAttachment(player);
        final var currentId = attachment.getCurrent();

        if (currentId == null) {
            return -1;
        }

        final var expression = attachment.getReference().get();

        if (expression == null) {
            return - 1;
        }

        if (expression.isInterruptible(player, attachment.getState())) {
            expression.onInterrupt(player);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    player,
                    ExpressionControlPacket.interrupt(player.getUUID(), currentId)
            );

            NeoForge.EVENT_BUS.post(new PlayerExpressionEvent.Interrupt(player, currentId));

            return 1;
        }

        return 0;
    }

    public static void finish(@NonNull ServerPlayer player) {
        final var attachment = getAttachment(player);
        final var currentId = attachment.getCurrent();

        if (currentId == null) {
            return;
        }

        final var expression = attachment.getReference().get();

        attachment.clear();

        if (expression != null) {
            expression.onFinished(player);
        }

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                ExpressionControlPacket.finished(player.getUUID(), currentId)
        );
    }

    public static void signal(
            final @NonNull  ServerPlayer        player,
                            int                 signal
    ) {
        final var attachment = getAttachment(player);
        final var expression = attachment.getReference().get();

        if (expression != null) {
            expression.onSignal(
                    player,
                    attachment.getState(),
                    signal
            );
        }
    }

    public static boolean isPerforming(@NonNull ServerPlayer player) {
        return PlayerExpressionSystem   .getAttachment(player)
                                        .isExpressing();
    }
}
