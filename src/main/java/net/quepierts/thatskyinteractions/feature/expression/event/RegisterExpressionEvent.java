package net.quepierts.thatskyinteractions.feature.expression.event;

import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.quepierts.thatskyinteractions.feature.expression.Expression;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;

@RequiredArgsConstructor
public final class RegisterExpressionEvent extends Event {

    private final BiConsumer<ResourceLocation, Expression> registrar;

    public void register(
            final @NonNull ResourceLocation   identifier,
            final @NonNull Expression   expression
    ) {
        this.registrar.accept(identifier, expression);
    }

}
