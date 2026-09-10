package dev.anvilcraft.lib.v2.registrum;

import dev.anvilcraft.lib.v2.registrum.builders.BuilderCallback;
import dev.anvilcraft.lib.v2.registrum.builders.data.AttachmentBuilder;
import dev.anvilcraft.lib.v2.registrum.builders.self.SoundEventBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * AnvilLib API 兼容层：实现本 mod 用到的注册能力子集。
 */
public abstract class AbstractRegistrum<S extends AbstractRegistrum<S>> {

    private final String modid;

    public AbstractRegistrum(String modid) {
        this.modid = modid;
    }

    public String getModid() {
        return this.modid;
    }

    public ResourceLocation id(String path) {
        return new ResourceLocation(this.modid, path);
    }

    public <E> AttachmentBuilder<E> attachment(String name, Function<IAttachmentHolder, E> defaultFactory) {
        return new AttachmentBuilder<>(this, null, name, BuilderCallback.immediate(this), defaultFactory);
    }

    public <E> AttachmentBuilder<E> attachment(String name, Supplier<E> defaultSupplier) {
        return new AttachmentBuilder<>(this, null, name, BuilderCallback.immediate(this), holder -> defaultSupplier.get());
    }

    public SoundEventBuilder<?> soundEvent(String name) {
        return new SoundEventBuilder<>(this, null, name, BuilderCallback.immediate(this));
    }

    public <R, T extends R, P, B extends dev.anvilcraft.lib.v2.registrum.builders.Builder<R, T, P, B>> B entry(
            String name,
            Function<BuilderCallback, B> factory
    ) {
        return factory.apply(BuilderCallback.immediate(this));
    }

    public void addDataGenerator(
            dev.anvilcraft.lib.v2.registrum.providers.ProviderType<?> providerType,
            java.util.function.Consumer<dev.anvilcraft.lib.v2.registrum.providers.RegistrumTagsProvider.IntrinsicImpl<net.minecraft.world.entity.EntityType<?>>> consumer
    ) {
        // Fabric 移植：datagen 产物已存在于 src/generated/resources，运行期无需处理
    }
}
