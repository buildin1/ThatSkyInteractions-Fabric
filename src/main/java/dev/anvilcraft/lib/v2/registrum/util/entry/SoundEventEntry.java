package dev.anvilcraft.lib.v2.registrum.util.entry;

import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public class SoundEventEntry extends RegistryEntry<SoundEvent, SoundEvent> {

    public SoundEventEntry(AbstractRegistrum<?> owner, DeferredHolder<SoundEvent, SoundEvent> key) {
        super(owner, key);
    }
}
