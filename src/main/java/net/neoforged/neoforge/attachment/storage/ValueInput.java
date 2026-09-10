package net.neoforged.neoforge.attachment.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 1.20.1 兼容层：{@code net.neoforged.neoforge.attachment.storage.ValueInput} 的等价实现，底层是 CompoundTag。
 */
public final class ValueInput {

    private static final Logger LOGGER = LoggerFactory.getLogger("tsi-attachment");

    private final CompoundTag tag;

    public ValueInput(final CompoundTag tag) {
        this.tag = tag;
    }

    public CompoundTag tag() {
        return this.tag;
    }

    /** 与 26.x 语义一致：子节点缺失时返回空的 Optional 载体，而不是 null。 */
    public Optional<ValueInput> child(final String name) {
        if (!this.tag.contains(name, CompoundTag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return Optional.of(new ValueInput(this.tag.getCompound(name)));
    }

    public ValueInput childOrEmpty(final String name) {
        if (!this.tag.contains(name, CompoundTag.TAG_COMPOUND)) {
            return new ValueInput(new CompoundTag());
        }
        return new ValueInput(this.tag.getCompound(name));
    }

    public boolean isEmpty() {
        return this.tag.isEmpty();
    }

    public <T> Optional<T> read(final String name, final Codec<T> codec) {
        if (!this.tag.contains(name)) {
            return Optional.empty();
        }
        return codec.parse(NbtOps.INSTANCE, this.tag.get(name))
                .resultOrPartial(error -> LOGGER.error("Failed to decode {}: {}", name, error));
    }

    public <T> Optional<T> read(final String name, final MapCodec<T> codec) {
        return this.read(name, codec.codec());
    }
}
