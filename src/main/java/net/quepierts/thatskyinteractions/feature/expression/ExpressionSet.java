package net.quepierts.thatskyinteractions.feature.expression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.thatskyinteractions.feature.data.Order;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record ExpressionSet(
        ResourceLocation icon,
        List<Expression> expressions,
        Order priority
) {

    public static final Codec<ExpressionSet> CODEC
            = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("icon").forGetter(ExpressionSet::icon),
                    Expression.CODEC.listOf().fieldOf("expressions").forGetter(ExpressionSet::expressions),
                    Order.CODEC.optionalFieldOf("priority", Order.DEFAULT).forGetter(ExpressionSet::priority)
            ).apply(instance, ExpressionSet::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExpressionSet> STREAM_CODEC
            = StreamCodec.composite(
                    dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.RESOURCE_LOCATION,
                    ExpressionSet::icon,
                    dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.list(Expression.STREAM_CODEC),
                    ExpressionSet::expressions,
                    Order.STREAM_CODEC,
                    ExpressionSet::priority,
                    ExpressionSet::new
            );

    public boolean leveled() {
        return this.expressions.size() > 1;
    }

    public int levels() {
        return this.expressions.size() == 1 ? 0 : this.expressions.size();
    }
}
