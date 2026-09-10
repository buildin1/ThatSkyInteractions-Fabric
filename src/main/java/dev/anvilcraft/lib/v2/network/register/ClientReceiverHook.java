package dev.anvilcraft.lib.v2.network.register;

import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;

/**
 * 通过反射把客户端接收器注册转发给客户端专属类，
 * 避免服务端加载引用客户端 API 的类（Fabric 环境剥离会直接拒绝）。
 */
public final class ClientReceiverHook {

    private ClientReceiverHook() {}

    public static void register(CustomPacketPayload.Type<?> type, StreamCodec<? super RegistryFriendlyByteBuf, ?> codec) {
        try {
            Class<?> clazz = Class.forName("net.quepierts.thatskyinteractions.internal.ClientPacketReceivers");
            clazz.getMethod("register", CustomPacketPayload.Type.class, StreamCodec.class)
                    .invoke(null, type, codec);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register client packet receiver", e);
        }
    }
}
