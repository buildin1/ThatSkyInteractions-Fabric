package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 暴露 ChunkMap 的实体追踪表（用于精确获取某实体的追踪者集合）。
 */
@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {

    @Accessor("entityMap")
    Int2ObjectMap<Object> tsi$getEntityMap();
}
