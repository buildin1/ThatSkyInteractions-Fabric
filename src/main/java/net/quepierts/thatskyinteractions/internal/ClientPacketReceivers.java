package net.quepierts.thatskyinteractions.internal;

import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;

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
        // 1.20.1 按 channel id 收包，自行解码后再切回主线程
        ClientPlayNetworking.registerGlobalReceiver(type.id(), (client, handler, buf, sender) -> {
            final T payload = codec.decode(RegistryFriendlyByteBuf.of(buf));
            client.execute(() -> payload.handleOnClient(client.player));
        });
    }
}
