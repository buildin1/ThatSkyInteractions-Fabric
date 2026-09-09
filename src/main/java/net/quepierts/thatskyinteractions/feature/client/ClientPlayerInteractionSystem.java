package net.quepierts.thatskyinteractions.feature.client;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.client.control.event.LocalPlayerMovedEvent;
import net.quepierts.thatskyinteractions.feature.interaction.PlayerInteractionAttachment;
import net.quepierts.thatskyinteractions.feature.interaction.PlayerInteractionSystem;
import net.quepierts.thatskyinteractions.feature.interaction.packet.InteractionRequestPacket;

@UtilityClass
@SuppressWarnings({"unused", "DataFlowIssue"})
public class ClientPlayerInteractionSystem {

    public static void invite(
            @NonNull Player         other,
            @NonNull Identifier     interaction
    ) {
        ClientPacketDistributor.sendToServer(
                InteractionRequestPacket.invite(
                        other,
                        interaction
                )
        );
    }

    public static void accept(
            @NonNull Player other
    ) {
        ClientPacketDistributor.sendToServer(
                InteractionRequestPacket.accept(
                        other
                )
        );
    }

    public static void cancel() {
        ClientPacketDistributor.sendToServer(
                InteractionRequestPacket.cancel()
        );
    }

    /** 本地玩家正在等待对方接受邀请时取消该邀请（避免被锁定到 60 秒超时） */
    public static void cancelIfWaiting() {
        final var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (PlayerInteractionAttachment.getAttachment(player).hasSentRequest()) {
            cancel();
        }
    }

    public static PlayerInteractionAttachment getLocalInteractionData() {
        return PlayerInteractionAttachment.getAttachment(Minecraft.getInstance().player);
    }
}
