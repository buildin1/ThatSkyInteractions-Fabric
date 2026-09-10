package net.quepierts.thatskyinteractions.feature.friendship.behaviour;

import com.mojang.serialization.Codec;
import lombok.experimental.UtilityClass;
import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.friendship.FriendshipTreeNode;
import net.quepierts.thatskyinteractions.core.friendship.model.NodeState;
import net.quepierts.thatskyinteractions.core.model.Currency;
import net.quepierts.thatskyinteractions.feature.registry.TsiRegistries;
import org.jspecify.annotations.NonNull;

// todo: make it similar to expressions and interactions
public interface FriendshipBehaviour {

    Codec<FriendshipBehaviour> CODEC
            = TsiRegistries.FRIENDSHIP_BEHAVIOUR
            .byNameCodec();

    StreamCodec<RegistryFriendlyByteBuf, FriendshipBehaviour> STREAM_CODEC
            = ByteBufCodecs.registry(TsiRegistries.Keys.FRIENDSHIP_BEHAVIOUR);

    @NonNull ResourceLocation DEFAULT_ICON    = ThatSkyInteractions.location("none");

    // 1.20.1 聊天组件没有内联图集精灵（Component.object 属 26.x），退回为文本
    net.minecraft.network.chat.MutableComponent SPRITE_CANDLE = net.minecraft.network.chat.Component.translatable("item.minecraft.candle");

    net.minecraft.network.chat.MutableComponent SPRITE_ACS = net.minecraft.network.chat.Component.translatable("item.minecraft.red_candle");


    int NORMAL_TEXT_COLOR               = 0xfff4f5e3;
    int HIGHLIGHT_TEXT_COLOR            = 0xfff67e1e;


    /**
     * @param requester the player who request this behaviour
     * @param receiver the player who will accept this behaviour
     * @param node the node which provide this behaviour and contains the metadata
     *
     * this method will call on server side
     */
    void execute(
            final @NonNull  ServerPlayer                requester,
            final @NonNull  ServerPlayer                receiver,
            final @NonNull  FriendshipTreeNode          node
    );

    default @NonNull ResourceLocation getIcon(
            final @NonNull  Player                      player,
            final @NonNull  FriendshipTreeNode          node,
            final @NonNull  NodeState                   state
    ) {
        return DEFAULT_ICON;
    }

    default @NonNull Component getUnlockMessage(
            final @NonNull  FriendshipTreeNode          node
    ) {
        return Component.translatable(
                "gui.thatskyinteractions.message.unlock.default.request",
                (node.getCost().currency() == Currency.WHITE_CANDLE ? SPRITE_CANDLE : SPRITE_ACS)
                        .copy()
                        .withStyle(Styles.SHADOWLESS),
                Component.translatable("node.thatskyinteractions." + node.getId())
                        .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(FriendshipBehaviour.HIGHLIGHT_TEXT_COLOR)))
                        .withStyle(Styles.BOLD)
        ).withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(FriendshipBehaviour.NORMAL_TEXT_COLOR)));
    }

    @UtilityClass
    class Styles {
        public static final Style BOLD          = Style.EMPTY.withBold(true);
        // 1.20.1 的 Style 没有 withoutShadow
        public static final Style SHADOWLESS    = Style.EMPTY;
    }

}
