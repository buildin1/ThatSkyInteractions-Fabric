package net.quepierts.thatskyinteractions.feature.friendship.behaviour;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.friendship.FriendshipTreeNode;
import net.quepierts.thatskyinteractions.core.friendship.model.NodeState;
import org.jspecify.annotations.NonNull;

public final class LockBehaviour implements FriendshipBehaviour {

    public static final LockBehaviour   INSTANCE    = new LockBehaviour();
    public static final String          TYPE        = "lock";
    public static final ResourceLocation      ICON        = ThatSkyInteractions.location("lock");

    @Override
    public void execute(
            final @NonNull ServerPlayer                 requester,
            final @NonNull ServerPlayer                 receiver,
            final @NonNull FriendshipTreeNode           node
    ) {

    }

    @Override
    public @NonNull ResourceLocation getIcon(
            final @NonNull  Player                      player,
            final @NonNull  FriendshipTreeNode          node,
            final @NonNull  NodeState                   state
    ) {
        return ICON;
    }

    @Override
    public @NonNull Component getUnlockMessage(
            final @NonNull FriendshipTreeNode           node
    ) {
        return Component.translatable(
                "gui.thatskyinteractions.message.unlock.lock.request",
                FriendshipBehaviour.SPRITE_CANDLE
                        .withStyle(Styles.SHADOWLESS),
                Component.translatable("gui.thatskyinteractions.message.unlock.lock.intimacy")
                        .withStyle(Styles.BOLD)
                        .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(FriendshipBehaviour.HIGHLIGHT_TEXT_COLOR)))
        ).withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(FriendshipBehaviour.NORMAL_TEXT_COLOR)));
    }

}
