package dev.anvilcraft.lib.v2.network.codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1.20.1 兼容层：payload 类型 → 流编解码器的注册表。
 *
 * <p>1.20.1 的网络层还是「channel id + FriendlyByteBuf」，没有 26.x 的
 * {@code PayloadTypeRegistry}。发送侧需要按 payload 的 type id 找回编解码器把它写进
 * buffer，这里就是那张表。注册顺序由 {@code TsiPackets} 的显式清单固定，
 * 但 1.20.1 是按 channel id 寻址的，所以顺序不再影响正确性。
 */
public final class PayloadCodecs {

    private static final Map<ResourceLocation, StreamCodec<? super RegistryFriendlyByteBuf, ?>> CODECS
            = new ConcurrentHashMap<>();

    private PayloadCodecs() {}

    public static void register(
            final CustomPacketPayload.Type<?> type,
            final StreamCodec<? super RegistryFriendlyByteBuf, ?> codec
    ) {
        CODECS.put(type.id(), codec);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> StreamCodec<RegistryFriendlyByteBuf, T> get(final ResourceLocation id) {
        return (StreamCodec<RegistryFriendlyByteBuf, T>) CODECS.get(id);
    }

    /** 把 payload 编码进一个新的 buffer，供 Fabric 的 channel 发送使用。 */
    @SuppressWarnings("unchecked")
    public static FriendlyByteBuf encode(final CustomPacketPayload payload) {
        final var id    = payload.type().id();
        final var codec = (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>) CODECS.get(id);

        if (codec == null) {
            throw new IllegalStateException("No stream codec registered for payload " + id);
        }

        final var buf = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        codec.encode(buf, payload);
        return buf;
    }
}
