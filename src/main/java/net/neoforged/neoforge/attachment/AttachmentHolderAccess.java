package net.neoforged.neoforge.attachment;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 1.20.1 兼容层：attachment 的实际存储。
 *
 * <p>1.20.1 没有 Fabric 的 Data Attachment API（该模块最早只到 1.20.5 那一代），
 * 所以存储由 mixin 注入到 {@code Entity} 与 {@code Level} 上，通过本接口暴露。
 * 26.x 分支上这层由 {@code AttachmentTarget} 提供。
 */
public interface AttachmentHolderAccess {

    Map<AttachmentType<?>, Object> tsi$attachments();

    /** 供 mixin 用的默认实现载体。 */
    final class Storage {

        private final Map<AttachmentType<?>, Object> map = new IdentityHashMap<>();

        public Map<AttachmentType<?>, Object> map() {
            return this.map;
        }
    }
}
