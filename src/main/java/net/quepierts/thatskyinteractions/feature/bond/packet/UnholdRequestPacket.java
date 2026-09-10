package net.quepierts.thatskyinteractions.feature.bond.packet;

import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.anvilcraft.lib.v2.network.packet.IServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.bond.PlayerBondSystem;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

public record UnholdRequestPacket(
        Optional<UUID>  other
) implements IServerboundPacket {

    public static final Type<UnholdRequestPacket> TYPE
            = IPacket.type(ThatSkyInteractions.location("handhold/unhold"));

    public static final StreamCodec<ByteBuf, UnholdRequestPacket> STREAM_CODEC
            = StreamCodec.composite(
                    ByteBufCodecs.optional(dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.UUID),
                    UnholdRequestPacket::other,
                    UnholdRequestPacket::new
            );

    public static UnholdRequestPacket all() {
        return new UnholdRequestPacket(Optional.empty());
    }

    public static UnholdRequestPacket other(
            final @NonNull Player other
    ) {
        return new UnholdRequestPacket(Optional.of(other.getUUID()));
    }

    @Override
    public void handleOnServer(final @NonNull Player player) {

        if (this.other().isPresent()) {
            final var other = player.level().getPlayerByUUID(this.other().get());

            if (other instanceof ServerPlayer sp) {
                PlayerBondSystem.unhold((ServerPlayer) player, sp);
            }

        } else {
            PlayerBondSystem.unholdAll((ServerPlayer) player);
        }

    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
