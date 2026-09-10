package net.quepierts.thatskyinteractions.feature.expression.packet;

import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.anvilcraft.lib.v2.network.packet.IServerboundPacket;
import io.netty.buffer.ByteBuf;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.expression.PlayerExpressionSystem;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record ExpressionRequestPacket(
        Operation               operation,
        Optional<ResourceLocation>    identifier,
        int                     level
) implements IServerboundPacket {

    public static final Type<ExpressionRequestPacket> TYPE
            = IPacket.type(ThatSkyInteractions.location("expression/request"));

    public static final StreamCodec<ByteBuf, ExpressionRequestPacket> STREAM_CODEC
            = StreamCodec.composite(
                    ByteBufCodecs.BYTE.map(Operation::decode, Operation::encode),
                    ExpressionRequestPacket::operation,
                    ByteBufCodecs.optional(dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.RESOURCE_LOCATION),
                    ExpressionRequestPacket::identifier,
                    ByteBufCodecs.VAR_INT,
                    ExpressionRequestPacket::level,
                    ExpressionRequestPacket::new
            );

    public static ExpressionRequestPacket perform(
            final @NonNull  ResourceLocation  id,
            final           int         level
    ) {
        return new ExpressionRequestPacket(Operation.PERFORM, Optional.of(id), level);
    }

    public static ExpressionRequestPacket cancel() {
        return new ExpressionRequestPacket(Operation.CANCEL, Optional.empty(), 0);
    }

    @Override
    public void handleOnServer(@NonNull Player player) {
        final var sender = (ServerPlayer) player;

        switch (this.operation()) {
            case PERFORM:
                PlayerExpressionSystem.enqueue(sender, this.identifier().orElseThrow(), this.level());
                break;
            case CANCEL:
                PlayerExpressionSystem.interrupt(sender);
                break;
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Operation {
        PERFORM,
        CANCEL;

        static final Operation[] VALUES = values();

        public static Operation decode(byte id) {
            return VALUES[id];
        }

        public byte encode() {
            return (byte) ordinal();
        }
    }
}
