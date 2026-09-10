package dev.anvilcraft.lib.v2.network.codec;

import io.netty.buffer.ByteBuf;

import java.util.function.Function;

/**
 * 1.20.1 兼容层：{@code net.minecraft.network.codec.StreamCodec} 的等价实现。
 *
 * <p>原版在 1.20.5 才引入该类型，而本 mod 的 23 个网络包全部建立在
 * {@code StreamCodec.composite(...)} 之上。这里按原版语义实现，让包类的方法体保持不变，
 * 只需要改 import 行。
 */
public interface StreamCodec<B, V> {

    V decode(B buffer);

    void encode(B buffer, V value);

    // ---------------------------------------------------------------- 函数式接口

    @FunctionalInterface
    interface StreamDecoder<B, V> {
        V decode(B buffer);
    }

    @FunctionalInterface
    interface StreamEncoder<B, V> {
        void encode(B buffer, V value);
    }

    @FunctionalInterface
    interface StreamMemberEncoder<B, V> {
        void encode(V value, B buffer);
    }

    // ---------------------------------------------------------------- 构造

    static <B, V> StreamCodec<B, V> of(final StreamMemberEncoder<B, V> encoder, final StreamDecoder<B, V> decoder) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B buffer) {
                return decoder.decode(buffer);
            }

            @Override
            public void encode(final B buffer, final V value) {
                encoder.encode(value, buffer);
            }
        };
    }

    static <B, V> StreamCodec<B, V> ofMember(final StreamMemberEncoder<B, V> encoder, final StreamDecoder<B, V> decoder) {
        return of(encoder, decoder);
    }

    static <B, V> StreamCodec<B, V> unit(final V value) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B buffer) {
                return value;
            }

            @Override
            public void encode(final B buffer, final V ignored) {
            }
        };
    }

    // ---------------------------------------------------------------- 变换

    default <S> StreamCodec<B, S> map(
            final Function<? super V, ? extends S> to,
            final Function<? super S, ? extends V> from
    ) {
        final StreamCodec<B, V> self = this;
        return new StreamCodec<>() {
            @Override
            public S decode(final B buffer) {
                return to.apply(self.decode(buffer));
            }

            @Override
            public void encode(final B buffer, final S value) {
                self.encode(buffer, from.apply(value));
            }
        };
    }

    @SuppressWarnings("unchecked")
    default <O extends ByteBuf> StreamCodec<O, V> cast() {
        return (StreamCodec<O, V>) this;
    }

    /**
     * 按键分派到子编解码器，与原版 {@code StreamCodec#dispatch} 语义一致：
     * 先用本编解码器写键，再用键选出的编解码器写值本体。
     */
    default <S> StreamCodec<B, S> dispatch(
            final Function<? super S, ? extends V> keyGetter,
            final Function<? super V, ? extends StreamCodec<? super B, ? extends S>> codecGetter
    ) {
        final StreamCodec<B, V> keyCodec = this;
        return new StreamCodec<>() {
            @Override
            public S decode(final B buffer) {
                final V key = keyCodec.decode(buffer);
                return codecGetter.apply(key).decode(buffer);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void encode(final B buffer, final S value) {
                final V key = keyGetter.apply(value);
                keyCodec.encode(buffer, key);
                ((StreamCodec<B, S>) codecGetter.apply(key)).encode(buffer, value);
            }
        };
    }

    // ---------------------------------------------------------------- composite

    @FunctionalInterface
    interface Factory2<V, T1, T2> {
        V apply(T1 t1, T2 t2);
    }

    @FunctionalInterface
    interface Factory3<V, T1, T2, T3> {
        V apply(T1 t1, T2 t2, T3 t3);
    }

    @FunctionalInterface
    interface Factory4<V, T1, T2, T3, T4> {
        V apply(T1 t1, T2 t2, T3 t3, T4 t4);
    }

    @FunctionalInterface
    interface Factory5<V, T1, T2, T3, T4, T5> {
        V apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
    }

    @FunctionalInterface
    interface Factory6<V, T1, T2, T3, T4, T5, T6> {
        V apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);
    }

    @FunctionalInterface
    interface Factory7<V, T1, T2, T3, T4, T5, T6, T7> {
        V apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7);
    }

    @FunctionalInterface
    interface Factory8<V, T1, T2, T3, T4, T5, T6, T7, T8> {
        V apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8);
    }

    @FunctionalInterface
    interface Factory9<V, T1, T2, T3, T4, T5, T6, T7, T8, T9> {
        V apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9);
    }

    @FunctionalInterface
    interface Factory10<V, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> {
        V apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10);
    }

    static <B, V, T1> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> c1, Function<? super V, ? extends T1> g1,
            Function<T1, V> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B b) {
                return factory.apply(c1.decode(b));
            }

            @Override
            public void encode(final B b, final V v) {
                c1.encode(b, g1.apply(v));
            }
        };
    }

    static <B, V, T1, T2> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> c1, Function<? super V, ? extends T1> g1,
            StreamCodec<? super B, T2> c2, Function<? super V, ? extends T2> g2,
            Factory2<V, T1, T2> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B b) {
                return factory.apply(c1.decode(b), c2.decode(b));
            }

            @Override
            public void encode(final B b, final V v) {
                c1.encode(b, g1.apply(v));
                c2.encode(b, g2.apply(v));
            }
        };
    }

    static <B, V, T1, T2, T3> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> c1, Function<? super V, ? extends T1> g1,
            StreamCodec<? super B, T2> c2, Function<? super V, ? extends T2> g2,
            StreamCodec<? super B, T3> c3, Function<? super V, ? extends T3> g3,
            Factory3<V, T1, T2, T3> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B b) {
                return factory.apply(c1.decode(b), c2.decode(b), c3.decode(b));
            }

            @Override
            public void encode(final B b, final V v) {
                c1.encode(b, g1.apply(v));
                c2.encode(b, g2.apply(v));
                c3.encode(b, g3.apply(v));
            }
        };
    }

    static <B, V, T1, T2, T3, T4> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> c1, Function<? super V, ? extends T1> g1,
            StreamCodec<? super B, T2> c2, Function<? super V, ? extends T2> g2,
            StreamCodec<? super B, T3> c3, Function<? super V, ? extends T3> g3,
            StreamCodec<? super B, T4> c4, Function<? super V, ? extends T4> g4,
            Factory4<V, T1, T2, T3, T4> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B b) {
                return factory.apply(c1.decode(b), c2.decode(b), c3.decode(b), c4.decode(b));
            }

            @Override
            public void encode(final B b, final V v) {
                c1.encode(b, g1.apply(v));
                c2.encode(b, g2.apply(v));
                c3.encode(b, g3.apply(v));
                c4.encode(b, g4.apply(v));
            }
        };
    }

    static <B, V, T1, T2, T3, T4, T5> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> c1, Function<? super V, ? extends T1> g1,
            StreamCodec<? super B, T2> c2, Function<? super V, ? extends T2> g2,
            StreamCodec<? super B, T3> c3, Function<? super V, ? extends T3> g3,
            StreamCodec<? super B, T4> c4, Function<? super V, ? extends T4> g4,
            StreamCodec<? super B, T5> c5, Function<? super V, ? extends T5> g5,
            Factory5<V, T1, T2, T3, T4, T5> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B b) {
                return factory.apply(c1.decode(b), c2.decode(b), c3.decode(b), c4.decode(b), c5.decode(b));
            }

            @Override
            public void encode(final B b, final V v) {
                c1.encode(b, g1.apply(v));
                c2.encode(b, g2.apply(v));
                c3.encode(b, g3.apply(v));
                c4.encode(b, g4.apply(v));
                c5.encode(b, g5.apply(v));
            }
        };
    }

    static <B, V, T1, T2, T3, T4, T5, T6> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> c1, Function<? super V, ? extends T1> g1,
            StreamCodec<? super B, T2> c2, Function<? super V, ? extends T2> g2,
            StreamCodec<? super B, T3> c3, Function<? super V, ? extends T3> g3,
            StreamCodec<? super B, T4> c4, Function<? super V, ? extends T4> g4,
            StreamCodec<? super B, T5> c5, Function<? super V, ? extends T5> g5,
            StreamCodec<? super B, T6> c6, Function<? super V, ? extends T6> g6,
            Factory6<V, T1, T2, T3, T4, T5, T6> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B b) {
                return factory.apply(c1.decode(b), c2.decode(b), c3.decode(b), c4.decode(b), c5.decode(b), c6.decode(b));
            }

            @Override
            public void encode(final B b, final V v) {
                c1.encode(b, g1.apply(v));
                c2.encode(b, g2.apply(v));
                c3.encode(b, g3.apply(v));
                c4.encode(b, g4.apply(v));
                c5.encode(b, g5.apply(v));
                c6.encode(b, g6.apply(v));
            }
        };
    }

    static <B, V, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> c1, Function<? super V, ? extends T1> g1,
            StreamCodec<? super B, T2> c2, Function<? super V, ? extends T2> g2,
            StreamCodec<? super B, T3> c3, Function<? super V, ? extends T3> g3,
            StreamCodec<? super B, T4> c4, Function<? super V, ? extends T4> g4,
            StreamCodec<? super B, T5> c5, Function<? super V, ? extends T5> g5,
            StreamCodec<? super B, T6> c6, Function<? super V, ? extends T6> g6,
            StreamCodec<? super B, T7> c7, Function<? super V, ? extends T7> g7,
            Factory7<V, T1, T2, T3, T4, T5, T6, T7> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B b) {
                return factory.apply(c1.decode(b), c2.decode(b), c3.decode(b), c4.decode(b), c5.decode(b), c6.decode(b), c7.decode(b));
            }

            @Override
            public void encode(final B b, final V v) {
                c1.encode(b, g1.apply(v));
                c2.encode(b, g2.apply(v));
                c3.encode(b, g3.apply(v));
                c4.encode(b, g4.apply(v));
                c5.encode(b, g5.apply(v));
                c6.encode(b, g6.apply(v));
                c7.encode(b, g7.apply(v));
            }
        };
    }

    static <B, V, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> c1, Function<? super V, ? extends T1> g1,
            StreamCodec<? super B, T2> c2, Function<? super V, ? extends T2> g2,
            StreamCodec<? super B, T3> c3, Function<? super V, ? extends T3> g3,
            StreamCodec<? super B, T4> c4, Function<? super V, ? extends T4> g4,
            StreamCodec<? super B, T5> c5, Function<? super V, ? extends T5> g5,
            StreamCodec<? super B, T6> c6, Function<? super V, ? extends T6> g6,
            StreamCodec<? super B, T7> c7, Function<? super V, ? extends T7> g7,
            StreamCodec<? super B, T8> c8, Function<? super V, ? extends T8> g8,
            Factory8<V, T1, T2, T3, T4, T5, T6, T7, T8> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B b) {
                return factory.apply(c1.decode(b), c2.decode(b), c3.decode(b), c4.decode(b), c5.decode(b), c6.decode(b), c7.decode(b), c8.decode(b));
            }

            @Override
            public void encode(final B b, final V v) {
                c1.encode(b, g1.apply(v));
                c2.encode(b, g2.apply(v));
                c3.encode(b, g3.apply(v));
                c4.encode(b, g4.apply(v));
                c5.encode(b, g5.apply(v));
                c6.encode(b, g6.apply(v));
                c7.encode(b, g7.apply(v));
                c8.encode(b, g8.apply(v));
            }
        };
    }

    static <B, V, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> c1, Function<? super V, ? extends T1> g1,
            StreamCodec<? super B, T2> c2, Function<? super V, ? extends T2> g2,
            StreamCodec<? super B, T3> c3, Function<? super V, ? extends T3> g3,
            StreamCodec<? super B, T4> c4, Function<? super V, ? extends T4> g4,
            StreamCodec<? super B, T5> c5, Function<? super V, ? extends T5> g5,
            StreamCodec<? super B, T6> c6, Function<? super V, ? extends T6> g6,
            StreamCodec<? super B, T7> c7, Function<? super V, ? extends T7> g7,
            StreamCodec<? super B, T8> c8, Function<? super V, ? extends T8> g8,
            StreamCodec<? super B, T9> c9, Function<? super V, ? extends T9> g9,
            Factory9<V, T1, T2, T3, T4, T5, T6, T7, T8, T9> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B b) {
                return factory.apply(c1.decode(b), c2.decode(b), c3.decode(b), c4.decode(b), c5.decode(b), c6.decode(b), c7.decode(b), c8.decode(b), c9.decode(b));
            }

            @Override
            public void encode(final B b, final V v) {
                c1.encode(b, g1.apply(v));
                c2.encode(b, g2.apply(v));
                c3.encode(b, g3.apply(v));
                c4.encode(b, g4.apply(v));
                c5.encode(b, g5.apply(v));
                c6.encode(b, g6.apply(v));
                c7.encode(b, g7.apply(v));
                c8.encode(b, g8.apply(v));
                c9.encode(b, g9.apply(v));
            }
        };
    }

    static <B, V, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> c1, Function<? super V, ? extends T1> g1,
            StreamCodec<? super B, T2> c2, Function<? super V, ? extends T2> g2,
            StreamCodec<? super B, T3> c3, Function<? super V, ? extends T3> g3,
            StreamCodec<? super B, T4> c4, Function<? super V, ? extends T4> g4,
            StreamCodec<? super B, T5> c5, Function<? super V, ? extends T5> g5,
            StreamCodec<? super B, T6> c6, Function<? super V, ? extends T6> g6,
            StreamCodec<? super B, T7> c7, Function<? super V, ? extends T7> g7,
            StreamCodec<? super B, T8> c8, Function<? super V, ? extends T8> g8,
            StreamCodec<? super B, T9> c9, Function<? super V, ? extends T9> g9,
            StreamCodec<? super B, T10> c10, Function<? super V, ? extends T10> g10,
            Factory10<V, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B b) {
                return factory.apply(c1.decode(b), c2.decode(b), c3.decode(b), c4.decode(b), c5.decode(b), c6.decode(b), c7.decode(b), c8.decode(b), c9.decode(b), c10.decode(b));
            }

            @Override
            public void encode(final B b, final V v) {
                c1.encode(b, g1.apply(v));
                c2.encode(b, g2.apply(v));
                c3.encode(b, g3.apply(v));
                c4.encode(b, g4.apply(v));
                c5.encode(b, g5.apply(v));
                c6.encode(b, g6.apply(v));
                c7.encode(b, g7.apply(v));
                c8.encode(b, g8.apply(v));
                c9.encode(b, g9.apply(v));
                c10.encode(b, g10.apply(v));
            }
        };
    }
}
