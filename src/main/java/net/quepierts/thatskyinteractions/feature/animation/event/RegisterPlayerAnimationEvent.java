package net.quepierts.thatskyinteractions.feature.animation.event;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.quepierts.thatskyinteractions.core.animation.model.PlayerAnimationDefinition;
import net.quepierts.thatskyinteractions.core.animation.model.SourceDefinition;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Slf4j
@RequiredArgsConstructor
public final class RegisterPlayerAnimationEvent extends Event {

    private final Object2ObjectMap<ResourceLocation, PlayerAnimationDefinition> definitions;

    public void register(
            @NonNull ResourceLocation                 id,
            @NonNull PlayerAnimationDefinition  definition
    ) {
        if (this.definitions.put(id, definition) != null) {
            log.error("Animation already registered: {}", id);
        }
    }

    public void redirect(
            @NonNull ResourceLocation                 id,
            @NonNull PlayerAnimationDefinition  definition
    ) {
        if (this.definitions.put(id, definition) == null) {
            log.error("Animation not registered: {}", id);
        } else {
            log.info("Animation overridden: {}", id);
        }
    }

    public void modify(
            @NonNull ResourceLocation                 id,
            @NonNull String                     name,
            @NonNull SourceDefinition           source,
            @NonNull String                     layer
    ) {
        final var definition    = this.definitions.get(id);
        if (definition == null) {
            log.error("Animation not registered: {}", id);
            return;
        }

        try {
            definition.sources().put(name, source);
        } catch (Exception e) {

            final var sources   = new Object2ObjectOpenHashMap<>(definition.sources());
            final var duplicate = new PlayerAnimationDefinition(
                    definition.type(),
                    definition.override(),
                    sources,
                    definition.unlock(),
                    definition.abortable(),
                    definition.restrictMotion(),
                    definition.rootMotion(),
                    layer
            );

            this.definitions    .put(id, duplicate);
        }
    }

    public void unregister(
            @NonNull ResourceLocation                 id
    ) {
        if (this.definitions.remove(id) == null) {
            log.error("Animation not registered: {}", id);
        } else {
            log.info("Animation unregistered: {}", id);
        }
    }

    public boolean contains(
            @NonNull ResourceLocation                 id
    ) {
        return this.definitions.containsKey(id);
    }

    public @Nullable PlayerAnimationDefinition get(
            @NonNull ResourceLocation                 id
    ) {
        return this.definitions.get(id);
    }

}
