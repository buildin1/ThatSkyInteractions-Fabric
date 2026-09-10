package net.quepierts.thatskyinteractions.feature.animation;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import net.quepierts.thatskyinteractions.core.animation.model.PlayerBone;
import net.quepierts.thatskyinteractions.core.animation.model.PlayerMask;

@UtilityClass
public class PlayerMaskParser {

    // 1.20.1 没有 Codec#withAlternative，也没有带上下限的 listOf
    public static final Codec<PlayerMask> CODEC
            = dev.anvilcraft.lib.v2.codec.CodecCompat.withAlternative(
                    Codec.INT.xmap(
                            PlayerMask::direct,
                            PlayerMask::getMask
                    ),
                    Codec.STRING.xmap(
                            PlayerBone::of,
                            PlayerBone::getName
                    ).listOf().xmap(
                            PlayerMask::of,
                            PlayerMask::toList
                    )
            );

    public static final StreamCodec<ByteBuf, PlayerMask> STREAM_CODEC
            = StreamCodec.composite(
                    ByteBufCodecs.INT,
                    PlayerMask::getMask,
                    PlayerMask::direct
            );

}
