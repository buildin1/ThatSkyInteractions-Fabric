package net.neoforged.neoforge.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * NeoForge API 兼容层：服务端发包工具，底层为 Fabric Networking。
 * 参数用 Object... 以兼容 NeoForge 的“单个 payload + 数组”混用调用形态。
 */
public final class PacketDistributor {

    private PacketDistributor() {}

    private static List<CustomPacketPayload> flatten(Object... payloads) {
        List<CustomPacketPayload> result = new ArrayList<>();
        for (Object payload : payloads) {
            if (payload instanceof CustomPacketPayload single) {
                result.add(single);
            } else if (payload instanceof Object[] array) {
                for (Object element : array) {
                    if (element instanceof CustomPacketPayload packet) {
                        result.add(packet);
                    }
                }
            }
        }
        return result;
    }

    private static void send(ServerPlayer player, List<CustomPacketPayload> payloads) {
        for (CustomPacketPayload payload : payloads) {
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendToPlayer(ServerPlayer player, Object... payloads) {
        send(player, flatten(payloads));
    }

    public static void sendToAllPlayers(Object... payloads) {
        MinecraftServer server = ServerHolder.get();
        if (server == null) return;
        List<CustomPacketPayload> list = flatten(payloads);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            send(player, list);
        }
    }

    public static void sendToPlayersInDimension(ServerLevel level, Object... payloads) {
        List<CustomPacketPayload> list = flatten(payloads);
        for (ServerPlayer player : level.players()) {
            send(player, list);
        }
    }

    public static void sendToPlayersTrackingEntity(Entity entity, Object... payloads) {
        if (entity.level() instanceof ServerLevel level) {
            List<CustomPacketPayload> list = flatten(payloads);
            for (ServerPlayer player : TrackingHelper.getTrackingPlayers(level, entity)) {
                send(player, list);
            }
        }
    }

    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, Object... payloads) {
        if (entity.level() instanceof ServerLevel level) {
            List<CustomPacketPayload> list = flatten(payloads);
            // 追踪者 + 自身，已去重（避免目标玩家收到两次）
            for (ServerPlayer player : TrackingHelper.getTrackingPlayersAndSelf(level, entity)) {
                send(player, list);
            }
        }
    }

    public static void sendToPlayersNear(ServerLevel level, ServerPlayer excluded, double x, double y, double z, double radius, Object... payloads) {
        List<CustomPacketPayload> list = flatten(payloads);
        for (ServerPlayer player : level.players()) {
            if (player == excluded) continue;
            double dx = player.getX() - x;
            double dy = player.getY() - y;
            double dz = player.getZ() - z;
            if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                send(player, list);
            }
        }
    }

    /** 客户端可用：发送到服务器（在双端共享代码中调用时需自行确保仅在客户端执行） */
    public static void sendToServer(Object... payloads) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            for (CustomPacketPayload payload : flatten(payloads)) {
                ClientPacketDistributor.sendToServer(payload);
            }
        }
    }
}
