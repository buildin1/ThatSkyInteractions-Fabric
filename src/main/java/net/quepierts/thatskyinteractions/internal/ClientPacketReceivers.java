package net.quepierts.thatskyinteractions.internal;

import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 客户端专属的 S2C 接收器注册。
 * 本类只在客户端被反射加载（见 {@code ClientReceiverHook}），
 * 避免服务端因引用客户端 API 而在环境剥离阶段崩溃。
 */
public final class ClientPacketReceivers {

    private ClientPacketReceivers() {}

    public static <T extends IClientboundPacket> void register(
            final CustomPacketPayload.Type<T> type,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, ctx) ->
                ctx.client().execute(() -> payload.handleOnClient(ctx.player())));
    }
}
