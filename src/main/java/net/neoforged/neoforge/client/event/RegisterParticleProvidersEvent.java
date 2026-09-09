package net.neoforged.neoforge.client.event;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RegisterParticleProvidersEvent extends Event {

    /** NeoForge 的 SpriteParticleRegistration 等价形状 */
    public interface SpriteParticleRegistration<T extends ParticleOptions> extends Function<SpriteSet, ParticleProvider<T>> {
    }

    public record ProviderEntry<T extends ParticleOptions>(
            ParticleType<T> type,
            SpriteParticleRegistration<T> provider
    ) {}

    private final List<ProviderEntry<?>> providers = new ArrayList<>();

    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, SpriteParticleRegistration<T> provider) {
        this.providers.add(new ProviderEntry<>(type, provider));
    }

    public List<ProviderEntry<?>> getProviders() {
        return this.providers;
    }
}
