package net.quepierts.thatskyinteractions.feature.friendship.behaviour;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.friendship.FriendshipTreeNode;
import net.quepierts.thatskyinteractions.core.friendship.model.NodeState;
import net.quepierts.thatskyinteractions.core.model.Currency;
import net.quepierts.thatskyinteractions.feature.gui.packet.PlayerInteractionUiPacket;
import net.quepierts.thatskyinteractions.feature.interaction.PlayerInteractionManager;
import net.quepierts.thatskyinteractions.feature.interaction.PlayerInteractionSystem;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InteractionBehaviour implements FriendshipBehaviour {

    public static final InteractionBehaviour    INSTANCE    = new InteractionBehaviour();
    public static final String                  TYPE        = "interaction";
    public static final ResourceLocation              DEFAULT     = ThatSkyInteractions.location("none");

    private final Map<String, ResourceLocation>       cache       = new HashMap<>();
    private final Map<String, ResourceLocation>       icons       = new HashMap<>();

    @Override
    public void execute(
            final @NonNull  ServerPlayer                requester,
            final @NonNull  ServerPlayer                receiver,
            final @NonNull  FriendshipTreeNode          node
    ) {

        final var metadata      = node.getMetadata();
        final var interaction   = metadata.get("interaction");
        final var identifier    = this.cache.computeIfAbsent(interaction, s -> new ResourceLocation(s));
        final var level         = metadata.get("level");

        if (PlayerInteractionSystem.invite(
                requester,
                receiver,
                level == null ? identifier : identifier.withSuffix("_" + level)
        )) {

            PacketDistributor.sendToPlayer(
                    receiver,
                    PlayerInteractionUiPacket.invite(
                            requester,
                            this.icon(identifier)
                    )
            );

        }
    }

    @Override
    public @NonNull ResourceLocation getIcon(
            final @NonNull  Player                      player,
            final @NonNull  FriendshipTreeNode          node,
            final @NonNull  NodeState                   state
    ) {
        final var metadata      = node.getMetadata();
        final var interaction   = metadata.get("interaction");
        final var identifier    = this.cache.computeIfAbsent(interaction, s -> new ResourceLocation(s));

        return this.icon(identifier);
    }

    @Override
    public @NonNull Component getUnlockMessage(
            final @NonNull FriendshipTreeNode           node
    ) {

        // format: "id" or "namespace:id"
        // required: "id"
        final var metadata      = node.getMetadata();
        final var interaction   = metadata.get("interaction");
        final var identifier    = this.cache.computeIfAbsent(interaction, s -> new ResourceLocation(s));

        final var name          = identifier.getPath();

        return Component.translatable(
                "gui.thatskyinteractions.message.unlock.interaction.request",
                (node.getCost().currency() == Currency.WHITE_CANDLE ? SPRITE_CANDLE : SPRITE_ACS)
                        .copy()
                        .withStyle(Styles.SHADOWLESS),
                Component.translatable("interaction.thatskyinteractions." + name)
                        .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(FriendshipBehaviour.HIGHLIGHT_TEXT_COLOR)))
                        .withStyle(Styles.BOLD)
        ).withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(FriendshipBehaviour.NORMAL_TEXT_COLOR)));

    }

    private ResourceLocation icon(final @NonNull ResourceLocation identifier) {
        final var set           = PlayerInteractionManager.getInstance().getSet(identifier);
        return set == null ? DEFAULT : set.icon();
    }

    private ResourceLocation icon(String interaction) {
        return this.icons.computeIfAbsent(interaction, str -> {
            final var id        = this.cache.computeIfAbsent(str, s -> new ResourceLocation(s));
            return new ResourceLocation(
                    id.getNamespace(),
                    "interaction/" + id.getPath() + ".png"
            );
        });
    }
}
