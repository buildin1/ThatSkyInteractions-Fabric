package net.neoforged.neoforge.client.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;

/**
 * NeoForge API 兼容层：客户端发包工具。
 */
public final class ClientPacketDistributor {

    private ClientPacketDistributor() {}

    public static void sendToServer(CustomPacketPayload... payloads) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            throw new IllegalStateException("sendToServer can only be called on the client");
        }
        for (CustomPacketPayload payload : payloads) {
            ClientPlayNetworking.send(
                    payload.type().id(),
                    dev.anvilcraft.lib.v2.network.codec.PayloadCodecs.encode(payload)
            );
        }
    }
}
