package dev.anvilcraft.lib.v2.network.codec;

import net.minecraft.resources.ResourceLocation;

/**
 * 1.20.1 兼容层：{@code net.minecraft.network.protocol.common.custom.CustomPacketPayload} 的等价接口。
 * 原版在 1.20.2 才引入 payload 化的网络层，1.20.1 仍是按 channel id + FriendlyByteBuf 收发。
 */
public interface CustomPacketPayload {

    Type<? extends CustomPacketPayload> type();

    record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }
}
