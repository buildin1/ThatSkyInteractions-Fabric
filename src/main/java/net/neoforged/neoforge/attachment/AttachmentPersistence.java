package net.neoforged.neoforge.attachment;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * NeoForge API 兼容层：holder 感知的 attachment 持久化。
 * NeoForge 的 IAttachmentSerializer 在读写时需要 holder（用于设置实体 UUID 等），
 * Fabric 的 persistent codec 不提供 holder，因此在 Entity 存档钩子中自行读写
 * （与 NeoForge 把附件写入实体 NBT 的行为一致）。
 */
public final class AttachmentPersistence {

    private static final Logger LOGGER = LoggerFactory.getLogger("tsi-attachment");
    private static final String ROOT = "thatskyinteractions:attachments";

    private static final Map<Identifier, Entry<?>> ENTRIES = new LinkedHashMap<>();

    private AttachmentPersistence() {}

    private record Entry<E>(AttachmentType<E> type, IAttachmentSerializer<E> serializer) {}

    public static <E> void register(Identifier id, AttachmentType<E> type, IAttachmentSerializer<E> serializer) {
        ENTRIES.put(id, new Entry<>(type, serializer));
    }

    @SuppressWarnings("unchecked")
    public static void save(Entity entity, ValueOutput output) {
        if (ENTRIES.isEmpty()) {
            return;
        }
        ValueOutput root = output.child(ROOT);
        for (Map.Entry<Identifier, Entry<?>> e : ENTRIES.entrySet()) {
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
    }

    @SuppressWarnings("unchecked")
    public static void load(Entity entity, ValueInput input) {
        if (ENTRIES.isEmpty()) {
            return;
        }
        ValueInput root = input.childOrEmpty(ROOT);
        for (Map.Entry<Identifier, Entry<?>> e : ENTRIES.entrySet()) {
            Entry<Object> entry = (Entry<Object>) e.getValue();
            var child = root.child(e.getKey().toString());
            if (child.isEmpty()) {
                continue;
            }
            try {
                Object value = entry.serializer().read((IAttachmentHolder) entity, child.get());
                if (value != null) {
                    entity.setAttached(entry.type().fabricType(), value);
                }
            } catch (Exception ex) {
                LOGGER.error("Failed to load attachment {}", e.getKey(), ex);
            }
        }
    }
}
