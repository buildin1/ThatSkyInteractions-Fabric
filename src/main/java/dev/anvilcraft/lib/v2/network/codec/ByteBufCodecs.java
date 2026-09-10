package dev.anvilcraft.lib.v2.network.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * 1.20.1 兼容层：{@code net.minecraft.network.codec.ByteBufCodecs} 的等价实现。
 * 只实现本 mod 用到的成员，编码格式与原版一致。
 */
public final class ByteBufCodecs {

    private ByteBufCodecs() {}

    public static final StreamCodec<ByteBuf, Boolean> BOOL = StreamCodec.of(
            (value, buf) -> buf.writeBoolean(value),
            ByteBuf::readBoolean
    );

    public static final StreamCodec<ByteBuf, Byte> BYTE = StreamCodec.of(
            (value, buf) -> buf.writeByte(value),
            ByteBuf::readByte
    );

    public static final StreamCodec<ByteBuf, Integer> INT = StreamCodec.of(
            (value, buf) -> buf.writeInt(value),
            ByteBuf::readInt
    );

    public static final StreamCodec<ByteBuf, Float> FLOAT = StreamCodec.of(
            (value, buf) -> buf.writeFloat(value),
            ByteBuf::readFloat
    );

    public static final StreamCodec<ByteBuf, Integer> VAR_INT = StreamCodec.of(
            (value, buf) -> new FriendlyByteBuf(buf).writeVarInt(value),
            buf -> new FriendlyByteBuf(buf).readVarInt()
    );

    public static final StreamCodec<ByteBuf, String> STRING_UTF8 = StreamCodec.of(
            (value, buf) -> new FriendlyByteBuf(buf).writeUtf(value),
            buf -> new FriendlyByteBuf(buf).readUtf()
    );

    public static final StreamCodec<ByteBuf, org.joml.Vector3fc> VECTOR3F = StreamCodec.of(
            (value, buf) -> {
                buf.writeFloat(value.x());
                buf.writeFloat(value.y());
                buf.writeFloat(value.z());
            },
            buf -> new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat())
    );

    /** 可空值：先写一个存在位，再写值本体（与原版一致）。 */
    public static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(final StreamCodec<? super B, V> codec) {
        return new StreamCodec<>() {
            @Override
            public Optional<V> decode(final B buffer) {
                return buffer.readBoolean() ? Optional.of(codec.decode(buffer)) : Optional.empty();
            }

            @Override
            public void encode(final B buffer, final Optional<V> value) {
                if (value.isPresent()) {
                    buffer.writeBoolean(true);
                    codec.encode(buffer, value.get());
                } else {
                    buffer.writeBoolean(false);
                }
            }
        };
    }

    public static <B extends ByteBuf, V, C extends java.util.Collection<V>> StreamCodec<B, C> collection(
            final IntFunction<C> factory,
            final StreamCodec<? super B, V> elementCodec
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(final B buffer) {
                final int size = new FriendlyByteBuf(buffer).readVarInt();
                final C result = factory.apply(size);
                for (int i = 0; i < size; i++) {
                    result.add(elementCodec.decode(buffer));
                }
                return result;
            }

            @Override
            public void encode(final B buffer, final C value) {
                new FriendlyByteBuf(buffer).writeVarInt(value.size());
                for (final V element : value) {
                    elementCodec.encode(buffer, element);
                }
            }
        };
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
            final IntFunction<M> factory,
            final StreamCodec<? super B, K> keyCodec,
            final StreamCodec<? super B, V> valueCodec
    ) {
        return new StreamCodec<>() {
            @Override
            public M decode(final B buffer) {
                final int size = new FriendlyByteBuf(buffer).readVarInt();
                final M result = factory.apply(size);
                for (int i = 0; i < size; i++) {
                    result.put(keyCodec.decode(buffer), valueCodec.decode(buffer));
                }
                return result;
            }

            @Override
            public void encode(final B buffer, final M value) {
                new FriendlyByteBuf(buffer).writeVarInt(value.size());
                value.forEach((k, v) -> {
                    keyCodec.encode(buffer, k);
                    valueCodec.encode(buffer, v);
                });
            }
        };
    }

    /** 元素列表：先写长度再逐个写。 */
    public static <B extends ByteBuf, V> StreamCodec<B, java.util.List<V>> list(final StreamCodec<? super B, V> element) {
        return collection(java.util.ArrayList::new, element).map(l -> (java.util.List<V>) l, l -> new java.util.ArrayList<>(l));
    }

    public static final StreamCodec<ByteBuf, net.minecraft.world.phys.Vec3> VEC3 = StreamCodec.of(
            (value, buf) -> {
                buf.writeDouble(value.x);
                buf.writeDouble(value.y);
                buf.writeDouble(value.z);
            },
            buf -> new net.minecraft.world.phys.Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    public static final StreamCodec<ByteBuf, java.util.UUID> UUID = StreamCodec.of(
            (value, buf) -> new FriendlyByteBuf(buf).writeUUID(value),
            buf -> new FriendlyByteBuf(buf).readUUID()
    );

    public static final StreamCodec<ByteBuf, net.minecraft.resources.ResourceLocation> RESOURCE_LOCATION = StreamCodec.of(
            (value, buf) -> new FriendlyByteBuf(buf).writeResourceLocation(value),
            buf -> new FriendlyByteBuf(buf).readResourceLocation()
    );

    /**
     * 注册表元素：按 id 传输。
     *
     * <p>1.20.1 没有 {@code RegistryFriendlyByteBuf}，拿不到连接自带的注册表访问器，
     * 所以从全局根注册表按 key 解析——本 mod 的自定义注册表也注册在根注册表上。
     */
    @SuppressWarnings("unchecked")
    public static <B extends ByteBuf, T> StreamCodec<B, T> registry(final ResourceKey<? extends Registry<T>> key) {
        return new StreamCodec<>() {
            private Registry<T> registry;

            private Registry<T> registry() {
                if (this.registry == null) {
                    this.registry = (Registry<T>) net.minecraft.core.registries.BuiltInRegistries.REGISTRY.get(key.location());
                    if (this.registry == null) {
                        throw new IllegalStateException("Registry not found: " + key.location());
                    }
                }
                return this.registry;
            }

            @Override
            public T decode(final B buffer) {
                return this.registry().byId(new FriendlyByteBuf(buffer).readVarInt());
            }

            @Override
            public void encode(final B buffer, final T value) {
                new FriendlyByteBuf(buffer).writeVarInt(this.registry().getId(value));
            }
        };
    }
}
