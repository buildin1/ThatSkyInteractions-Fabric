package net.quepierts.thatskyinteractions.feature.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;
import net.minecraft.core.UUIDUtil;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import net.quepierts.thatskyinteractions.core.model.PlayerPair;

@UtilityClass
public class PlayerPairParser {

    public static final Codec<PlayerPair> CODEC
            = RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("left").forGetter(PlayerPair::getLeft),
                    UUIDUtil.CODEC.fieldOf("right").forGetter(PlayerPair::getRight)
            ).apply(instance, PlayerPair::new));

    public static final StreamCodec<ByteBuf, PlayerPair> STREAM_CODEC
            = StreamCodec.composite(
                    dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.UUID,
                    PlayerPair::getLeft,
                    dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.UUID,
                    PlayerPair::getRight,
                    PlayerPair::new
            );

}
