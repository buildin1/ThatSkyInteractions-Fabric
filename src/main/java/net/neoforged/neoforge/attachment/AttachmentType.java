package net.neoforged.neoforge.attachment;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 1.20.1 兼容层：attachment 类型。
 *
 * <p>26.x 分支上这是 Fabric {@code AttachmentType} 的包装；1.20.1 没有那套 API，
 * 所以类型自带全部元数据，存储与持久化由本包内的其它类负责。
 */
public final class AttachmentType<T> {

    private final ResourceLocation id;
    private final Function<IAttachmentHolder, T> defaultFactory;

    /** holder 感知的序列化器（NeoForge 语义），与 persistenceCodec 二选一。 */
    private final IAttachmentSerializer<T> serializer;
    private final MapCodec<T> persistenceCodec;
    private final Predicate<? super T> serializePredicate;

    private final boolean copyOnDeath;

    public AttachmentType(
            final ResourceLocation id,
            final Function<IAttachmentHolder, T> defaultFactory,
            final IAttachmentSerializer<T> serializer,
            final MapCodec<T> persistenceCodec,
            final Predicate<? super T> serializePredicate,
            final boolean copyOnDeath
    ) {
        this.id = id;
        this.defaultFactory = defaultFactory;
        this.serializer = serializer;
        this.persistenceCodec = persistenceCodec;
        this.serializePredicate = serializePredicate;
        this.copyOnDeath = copyOnDeath;
    }

    public ResourceLocation id() {
        return this.id;
    }

    public Function<IAttachmentHolder, T> defaultFactory() {
        return this.defaultFactory;
    }

    public IAttachmentSerializer<T> serializer() {
        return this.serializer;
    }

    public MapCodec<T> persistenceCodec() {
        return this.persistenceCodec;
    }

    public Predicate<? super T> serializePredicate() {
        return this.serializePredicate;
    }

    public boolean copyOnDeath() {
        return this.copyOnDeath;
    }

    public boolean persistent() {
        return this.serializer != null || this.persistenceCodec != null;
    }

    public T getDefault() {
        return this.defaultFactory != null ? this.defaultFactory.apply(null) : null;
    }

    @Override
    public String toString() {
        return "AttachmentType[" + this.id + "]";
    }
}
