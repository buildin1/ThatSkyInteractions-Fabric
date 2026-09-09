package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 让 Level 具备 NeoForge 的 attachment 持有者能力（Fabric 的 AttachmentTarget 已提供存储）。
 */
@Mixin(Level.class)
public abstract class LevelMixin implements IAttachmentHolder {
}
