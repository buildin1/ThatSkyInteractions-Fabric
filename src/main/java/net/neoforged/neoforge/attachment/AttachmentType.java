package net.neoforged.neoforge.attachment;

import java.util.function.Function;

/**
 * NeoForge API 兼容层：attachment 类型，包装 Fabric Data Attachment API 的类型。
 * defaultFactory 保留 NeoForge 的“按 holder 构造默认值”语义（由 IAttachmentHolder.getData 惰性创建）。
 */
public final class AttachmentType<T> {

    private final net.fabricmc.fabric.api.attachment.v1.AttachmentType<T> fabricType;
    private final Function<IAttachmentHolder, T> defaultFactory;

    public AttachmentType(
            net.fabricmc.fabric.api.attachment.v1.AttachmentType<T> fabricType,
            Function<IAttachmentHolder, T> defaultFactory
    ) {
        this.fabricType = fabricType;
        this.defaultFactory = defaultFactory;
    }

    public net.fabricmc.fabric.api.attachment.v1.AttachmentType<T> fabricType() {
        return this.fabricType;
    }

    public Function<IAttachmentHolder, T> defaultFactory() {
        return this.defaultFactory;
    }

    public T getDefault() {
        return this.defaultFactory != null ? this.defaultFactory.apply(null) : null;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {

        private Function<IAttachmentHolder, T> defaultFactory;
        private com.mojang.serialization.MapCodec<? extends T> persistenceCodec;
        private java.util.function.Predicate<? super T> serializePredicate;
        private net.minecraft.network.codec.StreamCodec<?, T> syncCodec;
        private boolean copyOnDeath;
        private boolean persistent;

        public Builder<T> defaultValue(Function<IAttachmentHolder, T> factory) {
            this.defaultFactory = factory;
            return this;
        }

        public Builder<T> persistent(com.mojang.serialization.MapCodec<? extends T> codec) {
            this.persistenceCodec = codec;
            this.persistent = true;
            return this;
        }

        public Builder<T> serializePredicate(java.util.function.Predicate<? super T> predicate) {
            this.serializePredicate = predicate;
            return this;
        }

        public Builder<T> sync(net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> codec) {
            this.syncCodec = codec;
            return this;
        }

        public Builder<T> copyOnDeath() {
            this.copyOnDeath = true;
            return this;
        }

        public boolean isCopyOnDeath() {
            return this.copyOnDeath;
        }

        public com.mojang.serialization.MapCodec<? extends T> persistenceCodec() {
            return this.persistenceCodec;
        }

        public net.minecraft.network.codec.StreamCodec<?, T> syncCodec() {
            return this.syncCodec;
        }

        public Function<IAttachmentHolder, T> defaultFactory() {
            return this.defaultFactory;
        }
    }
}
