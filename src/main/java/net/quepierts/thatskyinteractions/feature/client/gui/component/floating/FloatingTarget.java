package net.quepierts.thatskyinteractions.feature.client.gui.component.floating;

import net.minecraft.core.BlockPos;

import java.util.UUID;

public sealed interface FloatingTarget {
    record Entity(UUID uuid) implements FloatingTarget {}
    record Block(BlockPos pos) implements FloatingTarget {}

    /** 靠近提示图标专用键，避免与邀请按钮（Entity 键）互相覆盖 */
    record Prompt(UUID uuid) implements FloatingTarget {}
}
