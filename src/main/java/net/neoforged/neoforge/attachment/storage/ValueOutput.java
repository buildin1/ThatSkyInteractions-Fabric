package net.neoforged.neoforge.attachment.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1.20.1 兼容层：{@code net.neoforged.neoforge.attachment.storage.ValueOutput} 的等价实现，底层是 CompoundTag。
 * 26.x 用 ValueInput/ValueOutput 抽象读写，1.20.1 仍是直接操作 NBT。
 */
public final class ValueOutput {

    private static final Logger LOGGER = LoggerFactory.getLogger("tsi-attachment");

    private final CompoundTag tag;

    public ValueOutput(final CompoundTag tag) {
        this.tag = tag;
    }

    public CompoundTag tag() {
        return this.tag;
    }

    public ValueOutput child(final String name) {
        final CompoundTag child = new CompoundTag();
        this.tag.put(name, child);
        return new ValueOutput(child);
    }

    public <T> void storeNullable(final String name, final Codec<T> codec, final T value) {
        if (value == null) {
            return;
        }
        codec.encodeStart(NbtOps.INSTANCE, value)
                .resultOrPartial(error -> LOGGER.error("Failed to encode {}: {}", name, error))
                .ifPresent(encoded -> this.tag.put(name, encoded));
    }

    public <T> void storeNullable(final String name, final MapCodec<T> codec, final T value) {
        this.storeNullable(name, codec.codec(), value);
    }

    public void put(final String name, final Tag value) {
        this.tag.put(name, value);
    }
}
