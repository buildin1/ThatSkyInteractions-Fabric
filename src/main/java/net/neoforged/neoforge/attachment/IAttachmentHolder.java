package net.neoforged.neoforge.attachment;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 1.20.1 兼容层：attachment 持有者。
 *
 * <p>存储由 {@code EntityMixin} / {@code LevelMixin} 注入并经 {@link AttachmentHolderAccess} 暴露。
 * {@code getData} 保留 NeoForge 的「按 holder 惰性构造默认值」语义。
 */
public interface IAttachmentHolder {

    private Map<AttachmentType<?>, Object> tsi$map() {
        return ((AttachmentHolderAccess) this).tsi$attachments();
    }

    @SuppressWarnings("unchecked")
    default <T> T getData(AttachmentType<T> type) {
        final Map<AttachmentType<?>, Object> map = this.tsi$map();
        final Object existing = map.get(type);
        if (existing != null) {
            return (T) existing;
        }
        final T created = type.defaultFactory().apply(this);
        map.put(type, created);
        return created;
    }

    default <T> T getData(Supplier<? extends AttachmentType<T>> type) {
        return this.getData(type.get());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    default <T> T getExistingDataOrNull(AttachmentType<T> type) {
        return (T) this.tsi$map().get(type);
    }

    @Nullable
    default <T> T getExistingDataOrNull(Supplier<? extends AttachmentType<T>> type) {
        return this.getExistingDataOrNull(type.get());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    default <T> T setData(AttachmentType<T> type, T value) {
        return (T) this.tsi$map().put(type, value);
    }

    @Nullable
    default <T> T setData(Supplier<? extends AttachmentType<T>> type, T value) {
        return this.setData(type.get(), value);
    }

    default boolean hasData(AttachmentType<?> type) {
        return this.tsi$map().containsKey(type);
    }

    default boolean hasData(Supplier<? extends AttachmentType<?>> type) {
        return this.hasData(type.get());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    default <T> T removeData(AttachmentType<T> type) {
        return (T) this.tsi$map().remove(type);
    }

    @Nullable
    default <T> T removeData(Supplier<? extends AttachmentType<T>> type) {
        return this.removeData(type.get());
    }
}
