package dev.anvilcraft.lib.v2.network.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 1.20.1 兼容层：{@code dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf} 的等价类型。
 *
 * <p>原版在 1.20.5 引入，用来让编解码器拿到连接自带的注册表访问器。1.20.1 没有这层，
 * 注册表查找走全局 {@code BuiltInRegistries}，所以这里只是 FriendlyByteBuf 的一个类型别名，
 * 让业务代码里的 {@code StreamCodec<RegistryFriendlyByteBuf, T>} 签名保持不变。
 */
public class RegistryFriendlyByteBuf extends FriendlyByteBuf {

    public RegistryFriendlyByteBuf(final ByteBuf source) {
        super(source);
    }

    public static RegistryFriendlyByteBuf of(final ByteBuf source) {
        return source instanceof RegistryFriendlyByteBuf buf ? buf : new RegistryFriendlyByteBuf(source);
    }
}
