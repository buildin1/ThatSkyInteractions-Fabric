package net.quepierts.thatskyinteractions.feature.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import org.jspecify.annotations.NonNull;

public class CallParticleType extends ParticleType<CallParticleOption> {

    public CallParticleType() {
        // 1.20.1 的 ParticleType 需要在构造时给出反序列化器
        super(false, CallParticleOption.DESERIALIZER);
    }

    @Override
    public @NonNull Codec<CallParticleOption> codec() {
        return CallParticleOption.CODEC;
    }
}
