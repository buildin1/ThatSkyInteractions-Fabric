package dev.anvilcraft.lib.v2.registrum.providers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * AnvilLib API 兼容层：datagen 标签 provider（仅编译兼容，产物已生成于 src/generated/resources）。
 */
public abstract class RegistrumTagsProvider<T> implements RegistrumProvider {

    public abstract void add(TagKey<T> tag);

    public static class IntrinsicImpl<T> extends IntrinsicHolderTagsProvider<T> implements RegistrumProvider {

        public IntrinsicImpl(
                PackOutput output,
                ResourceKey<? extends Registry<T>> registryKey,
                CompletableFuture<HolderLookup.Provider> lookupProvider,
                Function<T, ResourceKey<T>> keyExtractor
        ) {
            super(output, registryKey, lookupProvider, keyExtractor);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
        }

        @Override
        public TagAppender<T, T> tag(TagKey<T> tag) {
            return super.tag(tag);
        }

        public void add(TagKey<T> tag) {
            this.tag(tag);
        }

        /** NeoForge 的 TagAppender#add(TagEntry) 等价物（datagen 用） */
        public IntrinsicImpl<T> addOptionalElement(TagKey<T> tag, net.minecraft.resources.Identifier id) {
            this.getOrCreateRawBuilder(tag).add(net.minecraft.tags.TagEntry.optionalElement(id));
            return this;
        }
    }
}
