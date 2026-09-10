package dev.anvilcraft.lib.v2.registrum.builders.data;

import com.mojang.serialization.MapCodec;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.builders.AbstractBuilder;
import dev.anvilcraft.lib.v2.registrum.builders.BuilderCallback;
import dev.anvilcraft.lib.v2.registrum.util.entry.data.AttachmentEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentPersistence;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * AnvilLib API 兼容层：attachment 注册构建器。
 *
 * <p>1.20.1 没有 Fabric 的 Data Attachment API，存储与持久化由
 * {@code net.neoforged.neoforge.attachment} 下的自研实现承担。
 *
 * <p>{@code sync} 只记录流编解码器，不挂自动同步——与 26.x 的实际行为一致：
 * 那边的自动同步只在 {@code getData} 惰性创建时触发一次，friendship / preference
 * 的真正同步走的是 mod 自己的数据包（PacketCache / SyncDatapackPacket）。
 */
public class AttachmentBuilder<E> extends AbstractBuilder<AttachmentType<?>, AttachmentType<E>, Object, AttachmentBuilder<E>> {

    private final Function<IAttachmentHolder, E> defaultFactory;
    private final ResourceLocation id;

    private IAttachmentSerializer<E> serializer;
    private MapCodec<E> persistenceCodec;
    private Predicate<? super E> serializePredicate;
    private StreamCodec<? super FriendlyByteBuf, E> syncCodec;
    private boolean copyOnDeath;

    public AttachmentBuilder(
            AbstractRegistrum<?> owner,
            Object parent,
            String name,
            BuilderCallback callback,
            Function<IAttachmentHolder, E> defaultFactory
    ) {
        super(owner, parent, name, callback, null);
        this.defaultFactory = defaultFactory;
        this.id = owner.id(name);
    }

    public AttachmentBuilder<E> serialize(MapCodec<E> codec) {
        this.persistenceCodec = codec;
        return this;
    }

    /** NeoForge 的 holder 感知序列化器：由 AttachmentPersistence 在实体存档时读写。 */
    public AttachmentBuilder<E> serialize(IAttachmentSerializer<E> serializer) {
        this.serializer = serializer;
        return this;
    }

    public AttachmentBuilder<E> serialize(MapCodec<E> codec, Predicate<? super E> predicate) {
        this.persistenceCodec = codec;
        this.serializePredicate = predicate;
        return this;
    }

    public AttachmentBuilder<E> sync(StreamCodec<? super FriendlyByteBuf, E> codec) {
        this.syncCodec = codec;
        return this;
    }

    public AttachmentBuilder<E> copyOnDeath() {
        this.copyOnDeath = true;
        return this;
    }

    @Override
    protected AttachmentType<E> createEntry() {
        final AttachmentType<E> type = new AttachmentType<>(
                this.id,
                this.defaultFactory,
                this.serializer,
                this.persistenceCodec,
                this.serializePredicate,
                this.copyOnDeath
        );

        if (this.serializer != null) {
            AttachmentPersistence.register(this.id, type, this.serializer);
        } else if (this.persistenceCodec != null) {
            AttachmentPersistence.register(this.id, type, codecSerializer(type));
        }

        return type;
    }

    @Override
    public AttachmentEntry<E> register() {
        // attachment 无注册表：直接构建类型并包装（绕过 BuilderCallback 的注册表逻辑）
        final AttachmentType<E> type = this.createEntry();
        return new AttachmentEntry<>(this.getOwner(),
                new net.neoforged.neoforge.registries.DeferredHolder<>(null, this.id, () -> type));
    }

    /** 把 MapCodec(+谓词) 形式包装成 holder 感知的序列化器，统一走同一条存档路径。 */
    private IAttachmentSerializer<E> codecSerializer(final AttachmentType<E> type) {
        final MapCodec<E> codec = this.persistenceCodec;
        final Predicate<? super E> predicate = this.serializePredicate;
        final String key = "value";

        return new IAttachmentSerializer<>() {
            @Override
            public E read(final IAttachmentHolder holder, final net.neoforged.neoforge.attachment.storage.ValueInput input) {
                return input.read(key, codec).orElse(null);
            }

            @Override
            public boolean write(final E attachment, final net.neoforged.neoforge.attachment.storage.ValueOutput output) {
                if (predicate != null && !predicate.test(attachment)) {
                    return false;
                }
                output.storeNullable(key, codec, attachment);
                return true;
            }
        };
    }
}
