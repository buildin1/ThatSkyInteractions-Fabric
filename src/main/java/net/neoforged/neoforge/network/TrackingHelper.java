package net.neoforged.neoforge.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.quepierts.thatskyinteractions.feature.mixin.vanilla.ChunkMapAccessor;
import net.quepierts.thatskyinteractions.feature.mixin.vanilla.TrackedEntityAccessor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 获取真实追踪某实体的玩家集合（等价 NeoForge 的追踪者语义）：
 * 取 ChunkMap 中该实体的 TrackedEntity.seenBy。
 */
public final class TrackingHelper {

    private TrackingHelper() {}

    public static List<ServerPlayer> getTrackingPlayers(ServerLevel level, Entity entity) {

        final var chunkMap = level.getChunkSource().chunkMap;
        final Object tracked = ((ChunkMapAccessor) (Object) chunkMap).tsi$getEntityMap().get(entity.getId());

        if (tracked == null) {
            return List.of();
        }

        final Set<ServerPlayerConnection> seenBy = ((TrackedEntityAccessor) tracked).tsi$getSeenBy();
        final List<ServerPlayer> players = new ArrayList<>(seenBy.size());

        for (final ServerPlayerConnection connection : seenBy) {
            players.add(connection.getPlayer());
        }

        return players;
    }

    /** 追踪者 + 实体自身（若为玩家），并去重 */
    public static List<ServerPlayer> getTrackingPlayersAndSelf(ServerLevel level, Entity entity) {
        final Set<ServerPlayer> players = new LinkedHashSet<>(getTrackingPlayers(level, entity));
        if (entity instanceof ServerPlayer self) {
            players.add(self);
        }
        return new ArrayList<>(players);
    }
}
