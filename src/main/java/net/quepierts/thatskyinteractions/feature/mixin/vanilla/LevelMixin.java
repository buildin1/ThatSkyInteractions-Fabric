package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentHolderAccess;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 让 Level 具备 attachment 持有者能力。1.20.1 没有 Fabric 的 AttachmentTarget，
 * 存储直接注入到 Level 上。
 */
@Mixin(Level.class)
public abstract class LevelMixin implements IAttachmentHolder, AttachmentHolderAccess {

    @Unique
    private final Map<AttachmentType<?>, Object> tsi$attachments = new IdentityHashMap<>();

    @Unique
    @Override
    public Map<AttachmentType<?>, Object> tsi$attachments() {
        return this.tsi$attachments;
    }
}
