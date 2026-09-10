package net.quepierts.thatskyinteractions.feature.interaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record InteractionSet(
        ResourceLocation          icon,
        List<Interaction>   interactions
) {

    public static final Codec<InteractionSet> CODEC
            = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("icon").forGetter(InteractionSet::icon),
                Interaction.CODEC.listOf().fieldOf("interactions").forGetter(InteractionSet::interactions)
            ).apply(instance, InteractionSet::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InteractionSet> STREAM_CODEC
            = StreamCodec.composite(
                    dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.RESOURCE_LOCATION,
                    InteractionSet::icon,
                    dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.list(Interaction.STREAM_CODEC),
                    InteractionSet::interactions,
                    InteractionSet::new
            );

    public boolean leveled() {
        return this.interactions.size() > 1;
    }

    public int levels() {
        return this.interactions.size() == 1 ? 0 : this.interactions.size();
    }

}
