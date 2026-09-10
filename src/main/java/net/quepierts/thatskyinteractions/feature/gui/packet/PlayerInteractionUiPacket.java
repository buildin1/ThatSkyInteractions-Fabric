package net.quepierts.thatskyinteractions.feature.gui.packet;

import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.gui.handler.ClientInteractionUiHandler;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

public record PlayerInteractionUiPacket(
        Operation               operation,
        UUID                    requester,
        ResourceLocation              icon
) implements IClientboundPacket {

    public static final Type<PlayerInteractionUiPacket> TYPE
            = IPacket.type(ThatSkyInteractions.location("ui/interaction/player"));

    public static final StreamCodec<ByteBuf, PlayerInteractionUiPacket> STREAM_CODEC
            = StreamCodec.composite(
                    ByteBufCodecs.BYTE.map(
                            Operation::decode,
                            Operation::encode
                    ),
                    PlayerInteractionUiPacket::operation,
                    dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.UUID,
                    PlayerInteractionUiPacket::requester,
                    dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.RESOURCE_LOCATION,
                    PlayerInteractionUiPacket::icon,
                    PlayerInteractionUiPacket::new
            );

    public static PlayerInteractionUiPacket invite(
            final @NonNull Player requester,
            final @NonNull ResourceLocation icon
    ) {
        return new PlayerInteractionUiPacket(Operation.INVITE, requester.getUUID(), icon);
    }

    @Override
    public void handleOnClient(final @NonNull Player player) {

        final var requester = player.level().getPlayerByUUID(this.requester);
        if (requester == null) {
            return;
        }

        switch (this.operation()) {
            case INVITE: {
                ClientInteractionUiHandler.invite(requester, this.icon());
                break;
            }
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Operation {
        INVITE;

        static final Operation[] VALUES = values();

        public static Operation decode(byte id) {
            return VALUES[id];
        }

        public byte encode() {
            return (byte) ordinal();
        }
    }
    
}
