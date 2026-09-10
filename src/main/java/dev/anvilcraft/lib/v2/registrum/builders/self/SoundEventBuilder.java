package dev.anvilcraft.lib.v2.registrum.builders.self;

import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.builders.AbstractBuilder;
import dev.anvilcraft.lib.v2.registrum.builders.BuilderCallback;
import dev.anvilcraft.lib.v2.registrum.util.entry.SoundEventEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * AnvilLib API 兼容层：声音事件注册。
 */
public class SoundEventBuilder<P> extends AbstractBuilder<SoundEvent, SoundEvent, P, SoundEventBuilder<P>> {

    public SoundEventBuilder(AbstractRegistrum<?> owner, P parent, String name, BuilderCallback callback) {
        super(owner, parent, name, callback, null);
    }

    @Override
    protected SoundEvent createEntry() {
        return SoundEvent.createVariableRangeEvent(this.getOwner().id(this.getName()));
    }

    @Override
    public SoundEventEntry register() {
        SoundEvent sound = this.createEntry();
        Registry.register(BuiltInRegistries.SOUND_EVENT, sound.getLocation(), sound);
        DeferredHolder<SoundEvent, SoundEvent> holder =
                new DeferredHolder<SoundEvent, SoundEvent>((net.minecraft.resources.ResourceKey) BuiltInRegistries.SOUND_EVENT.key(), sound.getLocation(), () -> sound);
        return new SoundEventEntry(this.getOwner(), holder);
    }

    @Override
    protected SoundEventEntry createEntryWrapper(DeferredHolder<SoundEvent, SoundEvent> delegate) {
        return new SoundEventEntry(this.getOwner(), delegate);
    }
}
