package net.neoforged.neoforge.attachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.storage.ValueInput;
import net.neoforged.neoforge.attachment.storage.ValueOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 1.20.1 兼容层：attachment 的持久化与 copyOnDeath。
 *
 * <p>写入实体 NBT 的一个子标签，与 NeoForge 把附件写进实体 NBT 的行为一致。
 * 1.20.1 没有 ValueInput/ValueOutput，由本包的同名 shim 以 CompoundTag 承载。
 */
public final class AttachmentPersistence {

    private static final Logger LOGGER = LoggerFactory.getLogger("tsi-attachment");
    private static final String ROOT = "thatskyinteractions:attachments";

    private static final Map<ResourceLocation, Entry<?>> ENTRIES = new LinkedHashMap<>();

    private AttachmentPersistence() {}

    private record Entry<E>(AttachmentType<E> type, IAttachmentSerializer<E> serializer) {}

    public static <E> void register(ResourceLocation id, AttachmentType<E> type, IAttachmentSerializer<E> serializer) {
        ENTRIES.put(id, new Entry<>(type, serializer));
    }

    /** 所有已注册的类型（copyOnDeath 需要遍历）。 */
    public static java.util.List<AttachmentType<?>> registeredTypes() {
        final java.util.List<AttachmentType<?>> list = new java.util.ArrayList<>();
        for (Entry<?> e : ENTRIES.values()) {
            list.add(e.type());
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static void save(Entity entity, CompoundTag tag) {
        if (ENTRIES.isEmpty()) {
            return;
        }
        final CompoundTag rootTag = new CompoundTag();
        final ValueOutput root = new ValueOutput(rootTag);

        for (Map.Entry<ResourceLocation, Entry<?>> e : ENTRIES.entrySet()) {
            Entry<Object> entry = (Entry<Object>) e.getValue();
            Object value = ((IAttachmentHolder) entity).getExistingDataOrNull(entry.type());
            if (value == null) {
                continue;
            }
            try {
                entry.serializer().write(value, root.child(e.getKey().toString()));
            } catch (Exception ex) {
                LOGGER.error("Failed to save attachment {}", e.getKey(), ex);
            }
        }

        if (!rootTag.isEmpty()) {
            tag.put(ROOT, rootTag);
        }
    }

    @SuppressWarnings("unchecked")
    public static void load(Entity entity, CompoundTag tag) {
        if (ENTRIES.isEmpty() || !tag.contains(ROOT, CompoundTag.TAG_COMPOUND)) {
            return;
        }
        final ValueInput root = new ValueInput(tag.getCompound(ROOT));

        for (Map.Entry<ResourceLocation, Entry<?>> e : ENTRIES.entrySet()) {
            Entry<Object> entry = (Entry<Object>) e.getValue();
            var child = root.child(e.getKey().toString());
            if (child.isEmpty()) {
                continue;
            }
            try {
                Object value = entry.serializer().read((IAttachmentHolder) entity, child.get());
                if (value != null) {
                    ((IAttachmentHolder) entity).setData(entry.type(), value);
                }
            } catch (Exception ex) {
                LOGGER.error("Failed to load attachment {}", e.getKey(), ex);
            }
        }
    }

    /**
     * 玩家死亡 / 换维度重建时把标了 copyOnDeath 的附件搬过去。
     * 由 {@code CompatBootstrap} 挂到 Fabric 的 ServerPlayerEvents.COPY_FROM 上。
     */
    @SuppressWarnings("unchecked")
    public static void copyOnDeath(final IAttachmentHolder from, final IAttachmentHolder to) {
        for (Entry<?> e : ENTRIES.values()) {
            AttachmentType<Object> type = (AttachmentType<Object>) e.type();
            if (!type.copyOnDeath()) {
                continue;
            }
            Object value = from.getExistingDataOrNull(type);
            if (value != null) {
                to.setData(type, value);
            }
        }
    }
}
