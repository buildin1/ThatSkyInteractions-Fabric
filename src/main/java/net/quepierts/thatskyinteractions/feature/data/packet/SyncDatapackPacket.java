package net.quepierts.thatskyinteractions.feature.data.packet;

import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.data.DataSyncSystem;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("unused")
public record SyncDatapackPacket(
        int         id,
        PacketCache cache
) implements IClientboundPacket {

    public static final Type<SyncDatapackPacket> TYPE
            = IPacket.type(ThatSkyInteractions.location("sync_datapack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDatapackPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncDatapackPacket::id,
            PacketCache.STREAM_CODEC,
            SyncDatapackPacket::cache,
            SyncDatapackPacket::new
    );

    @Override
    public void handleOnClient(final @NonNull Player player) {
        DataSyncSystem.handle(this);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
