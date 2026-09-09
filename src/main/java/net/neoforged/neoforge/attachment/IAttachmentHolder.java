package net.neoforged.neoforge.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * NeoForge API 兼容层：attachment 持有者。
 * 由 {@code feature.mixin.vanilla.EntityMixin} 挂到 Entity 上；Level 等原版类型由 Fabric API
 * 的 AttachmentTarget 注入提供。同时提供 NeoForge 的 Supplier 重载（DeferredHolder 入口）。
 */
public interface IAttachmentHolder {

    default <T> T getData(AttachmentType<T> type) {
        AttachmentTarget self = (AttachmentTarget) this;
        T existing = self.getAttached(type.fabricType());
        if (existing != null) {
            return existing;
        }
        T created = type.defaultFactory().apply(this);
        self.setAttached(type.fabricType(), created);
        return created;
    }

    default <T> T getData(Supplier<? extends AttachmentType<T>> type) {
        return this.getData(type.get());
    }

    @Nullable
    default <T> T getExistingDataOrNull(AttachmentType<T> type) {
        return ((AttachmentTarget) this).getAttached(type.fabricType());
    }

    @Nullable
    default <T> T getExistingDataOrNull(Supplier<? extends AttachmentType<T>> type) {
        return this.getExistingDataOrNull(type.get());
    }

    @Nullable
    default <T> T setData(AttachmentType<T> type, T value) {
        AttachmentTarget self = (AttachmentTarget) this;
        T old = self.getAttached(type.fabricType());
        self.setAttached(type.fabricType(), value);
        return old;
    }

    @Nullable
    default <T> T setData(Supplier<? extends AttachmentType<T>> type, T value) {
        return this.setData(type.get(), value);
    }

    default boolean hasData(AttachmentType<?> type) {
        return ((AttachmentTarget) this).hasAttached(type.fabricType());
    }

    default boolean hasData(Supplier<? extends AttachmentType<?>> type) {
        return this.hasData(type.get());
    }

    @Nullable
    default <T> T removeData(AttachmentType<T> type) {
        AttachmentTarget self = (AttachmentTarget) this;
        T old = self.getAttached(type.fabricType());
        if (old != null) {
            self.removeAttached(type.fabricType());
        }
        return old;
    }

    @Nullable
    default <T> T removeData(Supplier<? extends AttachmentType<T>> type) {
        return this.removeData(type.get());
    }
}
