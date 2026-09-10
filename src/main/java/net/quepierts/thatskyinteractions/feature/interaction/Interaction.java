package net.quepierts.thatskyinteractions.feature.interaction;

import com.mojang.serialization.Codec;
import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.animation.event.PlayerAnimationControllerEvent;
import net.quepierts.thatskyinteractions.feature.animation.event.RegisterPlayerAnimationEvent;
import net.quepierts.thatskyinteractions.feature.expression.event.PlayerExpressionEvent;
import net.quepierts.thatskyinteractions.feature.registry.TsiRegistries;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public interface Interaction {

    ResourceLocation DEFAULT_EXPRESSION_REQUESTER
            = ThatSkyInteractions.location("interaction.requester");

    ResourceLocation DEFAULT_EXPRESSION_RECEIVER
            = ThatSkyInteractions.location("interaction.receiver");

    Codec<Interaction> CODEC
            = TsiRegistries.INTERACTION_TYPE
            .byNameCodec()
            .dispatch(Interaction::getType, type -> type.codec().codec());

    StreamCodec<RegistryFriendlyByteBuf, Interaction> STREAM_CODEC
            = ByteBufCodecs.<RegistryFriendlyByteBuf, InteractionType<? extends Interaction>>registry(
                    TsiRegistries.Keys.INTERACTION_TYPE
            ).dispatch(Interaction::getType, InteractionType::streamCodec);

    @NonNull InteractionType<? extends Interaction> getType();

    void onInvite(
            final @NonNull  ServerPlayer            requester,
            final @NonNull  ServerPlayer            receiver
    );

    void onAccepted(
            final @NonNull  ServerPlayer            requester,
            final @NonNull  ServerPlayer            receiver
    );

    void onCancel(
            final @NonNull  ServerPlayer            requester,
            final @Nullable ServerPlayer            receiver
    );

    default void onFinished(
            final @NonNull  ServerPlayer            requester,
            final @NonNull  ServerPlayer            receiver
    ) { }

    default boolean shouldFinish(
            final @NonNull  Player                  player
    ) {
        return true;
    }

    default void onInterrupted(
            final @NonNull  ServerPlayer            requester,
            final @NonNull  ServerPlayer            receiver
    ) { }

    default void onRegisterPlayerAnimation(
            final @NonNull RegisterPlayerAnimationEvent event,
            final @NonNull ResourceLocation                   identifier,
            final          int                          level
    ) { }

    default void onGenerateData(
            final @NonNull ResourceLocation                   identifier,
            final          int                          level
    ) { }

    default void onAnimationFinished(
            final @NonNull ServerPlayer                     player,
            final PlayerAnimationControllerEvent.Finished   event
    ) { }

    default void onExpressionFinished(
            final @NonNull ServerPlayer                     player,
            final @NonNull ResourceLocation                       expression
    ) { }

    default boolean positional() {
        return true;
    }

    default boolean is(final @NonNull Interaction other) {
        return this == other;
    }

    default boolean is(final @NonNull InteractionType<?> type) {
        return this.getType() == type;
    }

    default <E extends Interaction> boolean is(final @NonNull Supplier<InteractionType<E>> supplier) {
        return this.getType() == supplier.get();
    }

    default @NonNull ResourceLocation getRequesterExpression() {
        return DEFAULT_EXPRESSION_REQUESTER;
    }

    default @NonNull ResourceLocation getReceiverExpression() {
        return DEFAULT_EXPRESSION_RECEIVER;
    }

}
