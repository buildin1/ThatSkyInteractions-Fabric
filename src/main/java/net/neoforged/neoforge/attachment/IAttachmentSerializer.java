package net.neoforged.neoforge.attachment;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * NeoForge API 兼容层：attachment 序列化器接口（保留 NeoForge 26.x 的单泛型形式）。
 * 本 mod 的实现由 AttachmentBuilder.serialize 转换为 Fabric 的 Codec。
 */
public interface IAttachmentSerializer<T> {

    T read(IAttachmentHolder holder, ValueInput input);

    boolean write(T attachment, ValueOutput output);
}
