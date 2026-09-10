package net.quepierts.thatskyinteractions.feature.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.feature.registry.TsiParticleTypes;
import org.jspecify.annotations.NonNull;

/**
 * 1.20.1 的 ParticleOptions 还是「手写网络读写 + 命令解析器」，
 * 26.x 的 MapCodec/StreamCodec 组合是后来才引入的。
 */
public record CallParticleOption(
        int id
) implements ParticleOptions {

    public static final Codec<CallParticleOption> CODEC
            = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("id").forGetter(CallParticleOption::id)
    ).apply(instance, CallParticleOption::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CallParticleOption> STREAM_CODEC
            = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            CallParticleOption::id,
            CallParticleOption::new
    );

    public static final ParticleOptions.Deserializer<CallParticleOption> DESERIALIZER =
            new ParticleOptions.Deserializer<>() {
                @Override
                public @NonNull CallParticleOption fromCommand(
                        final @NonNull ParticleType<CallParticleOption> type,
                        final @NonNull StringReader reader
                ) throws CommandSyntaxException {
                    reader.expect(' ');
                    return new CallParticleOption(reader.readInt());
                }

                @Override
                public @NonNull CallParticleOption fromNetwork(
                        final @NonNull ParticleType<CallParticleOption> type,
                        final @NonNull FriendlyByteBuf buffer
                ) {
                    return new CallParticleOption(buffer.readVarInt());
                }
            };

    public static CallParticleOption of(final @NonNull Player player) {
        return new CallParticleOption(player.getId());
    }

    @Override
    public @NonNull ParticleType<?> getType() {
        return TsiParticleTypes.CALL.get();
    }

    @Override
    public void writeToNetwork(final @NonNull FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id);
    }

    @Override
    public @NonNull String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + this.id;
    }
}
