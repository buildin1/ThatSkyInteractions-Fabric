package dev.anvilcraft.lib.v2.network.packet;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * AnvilLib API 兼容层：payload 基础接口。
 */
public interface IPacket extends CustomPacketPayload {

    static <T extends IPacket> Type<T> type(Identifier id) {
        return new Type<>(id);
    }
}
