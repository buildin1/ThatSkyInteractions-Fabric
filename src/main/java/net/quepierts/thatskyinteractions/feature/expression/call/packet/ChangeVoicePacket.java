package net.quepierts.thatskyinteractions.feature.expression.call.packet;

import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.anvilcraft.lib.v2.network.packet.ISensitiveBiPacket;
import io.netty.buffer.ByteBuf;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.expression.PlayerPreferenceAttachment;
import org.jspecify.annotations.NonNull;

public record ChangeVoicePacket(
        ResourceLocation voice
) implements ISensitiveBiPacket {

    public static final Type<ChangeVoicePacket> TYPE
            = IPacket.type(ThatSkyInteractions.location("preference/voice"));

    public static final StreamCodec<ByteBuf, ChangeVoicePacket> STREAM_CODEC
            = dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.RESOURCE_LOCATION.map(
                    ChangeVoicePacket::new,
                    ChangeVoicePacket::voice
            );


    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(final @NonNull Player player) {
        PlayerPreferenceAttachment.getAttachment(player).setVoice(this.voice());
    }

    @Override
    public void handleOnServer(final @NonNull Player player) {
        PlayerPreferenceAttachment.getAttachment(player).setVoice(this.voice());
        PacketDistributor.sendToPlayer((ServerPlayer) player, this);
    }
}
