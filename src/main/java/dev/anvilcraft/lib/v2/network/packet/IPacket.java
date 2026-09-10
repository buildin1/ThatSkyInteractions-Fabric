package dev.anvilcraft.lib.v2.network.packet;

import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * AnvilLib API 兼容层：payload 基础接口。
 */
public interface IPacket extends CustomPacketPayload {

    static <T extends IPacket> Type<T> type(ResourceLocation id) {
        return new Type<>(id);
    }
}
