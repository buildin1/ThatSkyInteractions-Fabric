package dev.anvilcraft.lib.v2.registrum.builders.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.builders.AbstractBuilder;
import dev.anvilcraft.lib.v2.registrum.builders.BuilderCallback;
import dev.anvilcraft.lib.v2.registrum.util.entry.data.AttachmentEntry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.function.Predicate;

/**
 * AnvilLib API 兼容层：attachment 注册构建器，底层为 Fabric Data Attachment API。
 * sync 语义 = NeoForge 默认（同步给所有追踪者）；serialize = 持久化到实体存档。
 */
public class AttachmentBuilder<E> extends AbstractBuilder<net.neoforged.neoforge.attachment.AttachmentType<?>, net.neoforged.neoforge.attachment.AttachmentType<E>, Object, AttachmentBuilder<E>> {

    private AttachmentRegistry.Builder<E> fabricBuilder;
    private final java.util.function.Function<IAttachmentHolder, E> defaultFactory;
    private net.neoforged.neoforge.attachment.IAttachmentSerializer<E> serializer;
    private Identifier id;

    public AttachmentBuilder(
            AbstractRegistrum<?> owner,
            Object parent,
            String name,
            BuilderCallback callback,
            java.util.function.Function<IAttachmentHolder, E> defaultFactory
    ) {
        super(owner, parent, name, callback, null);
        this.defaultFactory = defaultFactory;
        this.id = owner.id(name);
        this.fabricBuilder = AttachmentRegistry.builder();
    }

    public AttachmentBuilder<E> serialize(MapCodec<E> codec) {
        this.fabricBuilder.persistent(codec.codec());
        return this;
    }

    /** NeoForge 的 holder 感知序列化器：由 AttachmentPersistence 在实体存档时读写 */
    public AttachmentBuilder<E> serialize(net.neoforged.neoforge.attachment.IAttachmentSerializer<E> serializer) {
        this.serializer = serializer;
        return this;
    }

    @SuppressWarnings("unchecked")
    public AttachmentBuilder<E> serialize(MapCodec<E> codec, Predicate<? super E> predicate) {
        // Fabric persistent 无谓词形式：包装编解码器，谓词不通过时编码为空记录（等价跳过）
        this.fabricBuilder.persistent(conditional(codec, predicate).codec());
        return this;
    }

    public AttachmentBuilder<E> sync(StreamCodec<? super RegistryFriendlyByteBuf, E> codec) {
        // NeoForge 语义：只同步给追踪该实体的客户端
        this.fabricBuilder.syncWith(codec, (target, player) -> {
            if (!(target instanceof net.minecraft.world.entity.Entity entity)) {
                return true;
            }
            if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) {
                return false;
            }
            return net.neoforged.neoforge.network.TrackingHelper
                    .getTrackingPlayersAndSelf(level, entity)
                    .contains(player);
        });
        return this;
    }

    public AttachmentBuilder<E> copyOnDeath() {
        this.fabricBuilder.copyOnDeath();
        return this;
    }

    @Override
    protected net.neoforged.neoforge.attachment.AttachmentType<E> createEntry() {
        AttachmentType<E> fabricType = this.fabricBuilder.buildAndRegister(this.id);
        net.neoforged.neoforge.attachment.AttachmentType<E> type =
                new net.neoforged.neoforge.attachment.AttachmentType<>(fabricType, this.defaultFactory);
        if (this.serializer != null) {
            net.neoforged.neoforge.attachment.AttachmentPersistence.register(this.id, type, this.serializer);
        }
        return type;
    }

    @Override
    public AttachmentEntry<E> register() {
        // attachment 无注册表：直接构建类型并包装（绕过 BuilderCallback 的注册表逻辑）
        net.neoforged.neoforge.attachment.AttachmentType<E> type = this.createEntry();
        return new AttachmentEntry<>(this.getOwner(),
                new net.neoforged.neoforge.registries.DeferredHolder<>(null, this.id, () -> type));
    }

    private static <E> MapCodec<E> conditional(MapCodec<E> codec, Predicate<? super E> predicate) {
        return new MapCodec<E>() {
            @Override
            public <T> com.mojang.serialization.RecordBuilder<T> encode(
                    E input, com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.RecordBuilder<T> prefix) {
                if (predicate.test(input)) {
                    return codec.encode(input, ops, prefix);
                }
                return prefix;
            }

            @Override
            public <T> com.mojang.serialization.DataResult<E> decode(
                    com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.MapLike<T> input) {
                return codec.decode(ops, input);
            }

            @Override
            public <T> java.util.stream.Stream<T> keys(com.mojang.serialization.DynamicOps<T> ops) {
                return codec.keys(ops);
            }
        };
    }
}
