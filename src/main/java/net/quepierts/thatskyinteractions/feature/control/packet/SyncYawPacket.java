package net.quepierts.thatskyinteractions.feature.control.packet;

import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record SyncYawPacket(
        UUID    player,
        float   yaw
) implements IClientboundPacket {

    public static final Type<SyncYawPacket> TYPE
            = IPacket.type(ThatSkyInteractions.location("control/align_yaw"));

    public static final StreamCodec<ByteBuf, SyncYawPacket> STREAM_CODEC
            = StreamCodec.composite(
                    dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.UUID,
                    SyncYawPacket::player,
                    ByteBufCodecs.FLOAT,
                    SyncYawPacket::yaw,
                    SyncYawPacket::new
            );

    public static SyncYawPacket of(
            final @NonNull  Player  player
    ) {
        return new SyncYawPacket(
                player.getUUID(),
                player.getYRot()
        );
    }

    @Override
    public void handleOnClient(final @NonNull Player player) {
        final Player target = player.level().getPlayerByUUID(this.player);
        if (target != null) {
            target.setYRot(this.yaw);
            target.setYHeadRot(this.yaw);
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
