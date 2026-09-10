package net.neoforged.neoforge.attachment;

import net.neoforged.neoforge.attachment.storage.ValueInput;
import net.neoforged.neoforge.attachment.storage.ValueOutput;

/**
 * NeoForge API 兼容层：attachment 序列化器（保留 NeoForge 26.x 的单泛型形式）。
 * 1.20.1 上 ValueInput/ValueOutput 由本兼容层以 CompoundTag 实现。
 */
public interface IAttachmentSerializer<T> {

    T read(IAttachmentHolder holder, ValueInput input);

    boolean write(T attachment, ValueOutput output);
}
