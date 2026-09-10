package net.quepierts.thatskyinteractions.feature.client;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.quepierts.thatskyinteractions.feature.animation.PlayerAnimationAttachment;
import net.quepierts.thatskyinteractions.feature.animation.packet.AnimationRequestPacket;
import org.jspecify.annotations.NonNull;

@UtilityClass
@SuppressWarnings({"unused", "DataFlowIssue"})
public class ClientPlayerAnimationSystem {

    public static void play(@NonNull ResourceLocation animation) {

        ClientPacketDistributor.sendToServer(
                AnimationRequestPacket.play(
                        animation
                )
        );
    }

    public static void abort() {
        ClientPacketDistributor.sendToServer(
                AnimationRequestPacket.abort()
        );
    }

    public static void exit() {
        ClientPacketDistributor.sendToServer(
                AnimationRequestPacket.exit()
        );
    }

    public static void event(@NonNull String event) {
        ClientPacketDistributor.sendToServer(
                AnimationRequestPacket.event(
                        event
                )
        );
    }

    public static PlayerAnimationAttachment getLocalAnimationData() {
        return PlayerAnimationAttachment.getAttachment(Minecraft.getInstance().player);
    }

}
