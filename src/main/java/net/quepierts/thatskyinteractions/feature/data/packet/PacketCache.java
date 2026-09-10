package net.quepierts.thatskyinteractions.feature.data.packet;

import io.netty.buffer.Unpooled;
import lombok.NoArgsConstructor;
import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import net.quepierts.veynir.core.misc.Generic;
import org.jspecify.annotations.NonNull;

@NoArgsConstructor
public final class PacketCache {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketCache> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PacketCache decode(final RegistryFriendlyByteBuf byteBuf) {
            var cache = new PacketCache();
            cache.read(byteBuf);
            return cache;
        }

        @Override
        public void encode(final RegistryFriendlyByteBuf byteBuf, final PacketCache packetCache) {
            packetCache.write(byteBuf);
        }
    };

    private volatile RegistryFriendlyByteBuf buffer;
    private int readableBytes;

    private StreamCodec<? super RegistryFriendlyByteBuf, ?> cachedCodec;
    private Object cachedObject;

    public void cache(
            final @NonNull StreamCodec<? super RegistryFriendlyByteBuf, ?> codec,
            final @NonNull Object object
    ) {
        this.cachedCodec = codec;
        this.cachedObject = object;

        this.free();
    }

    /*public <T> void encode(
            final @NonNull StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            final @NonNull T object
    ) {
        var fresh = new RegistryFriendlyByteBuf(Unpooled.buffer());
        codec.encode(fresh, object);

        this.free();

        this.buffer = fresh;
        this.readableBytes = fresh.readableBytes();
    }*/

    public <T> T decode(
            final @NonNull StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        return codec.decode(this.buffer);
    }

    public void write(
            final @NonNull RegistryFriendlyByteBuf target
    ) {
        if (this.buffer == null) {

            // 1.20.1 的 RegistryFriendlyByteBuf 只是 FriendlyByteBuf 的别名，不带注册表访问器
            this.buffer = new RegistryFriendlyByteBuf(Unpooled.buffer());

            if (this.cachedCodec != null && this.cachedObject != null) {
                this.cachedCodec.encode(this.buffer, Generic.cast(this.cachedObject));
                this.cachedCodec = null;
                this.cachedObject = null;
                this.readableBytes = this.buffer.readableBytes();
            }

        }
        target.writeBytes(this.buffer, 0, this.readableBytes);
    }

    public boolean ready() {
        return this.buffer != null && this.buffer.isReadable();
    }

    public void free() {
        if (this.buffer != null) {
            this.buffer.release();
            this.buffer = null;
        }
    }

    public void read(final RegistryFriendlyByteBuf byteBuf) {
        this.free();
        final var bytes = byteBuf.readableBytes();
        this.buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(bytes));
        this.buffer.writeBytes(byteBuf);
        this.readableBytes = bytes;
    }
}
