package dev.anvilcraft.lib.v2.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

/**
 * 1.20.1 兼容层：补上高版本才有的 Codec 组合子。
 */
public final class CodecCompat {

    private CodecCompat() {}

    /**
     * 26.x 的 {@code ExtraCodecs.VECTOR3F} 值类型是只读的 {@code Vector3fc}，
     * 1.20.1 的是可变的 {@code Vector3f}。这里给出只读视图，让业务代码的签名保持不变。
     */
    public static final Codec<org.joml.Vector3fc> VECTOR3FC =
            net.minecraft.util.ExtraCodecs.VECTOR3F.xmap(
                    v -> (org.joml.Vector3fc) v,
                    org.joml.Vector3f::new
            );

    /**
     * 等价 1.20.5+ 的 {@code ExtraCodecs.withAlternative}：先试主编解码器，失败再试备选。
     * 1.20.1 只有 {@code Codec.either}，这里按同样语义包一层。
     */
    public static <T> Codec<T> withAlternative(final Codec<T> primary, final Codec<? extends T> alternative) {
        return Codec.either(primary, alternative)
                .xmap(
                        either -> either.map(value -> value, value -> value),
                        Either::left
                );
    }
}
