package net.quepierts.thatskyinteractions.feature.interaction;

import net.minecraft.resources.ResourceLocation;
import net.quepierts.thatskyinteractions.feature.expression.event.RegisterExpressionEvent;
import org.jspecify.annotations.NonNull;

// todo: delegate to PlayerExpressionSystem
public interface Expressional {

    void onRegisterExpression(
            final @NonNull RegisterExpressionEvent      event,
            final @NonNull ResourceLocation                   identifier,
            final          int                          level
    );

}
