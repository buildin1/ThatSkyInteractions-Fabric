package net.quepierts.thatskyinteractions.feature.interaction;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import org.jspecify.annotations.NonNull;

public record InteractionType<T extends Interaction>(
        @NonNull MapCodec<T>                codec,
        @NonNull StreamCodec<ByteBuf, T>    streamCodec
) { }
