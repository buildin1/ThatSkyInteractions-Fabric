package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/**
 * 暴露 ChunkMap.TrackedEntity 的 seenBy 集合（真实追踪者）。
 */
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public interface TrackedEntityAccessor {

    @Accessor("seenBy")
    Set<ServerPlayerConnection> tsi$getSeenBy();
}
