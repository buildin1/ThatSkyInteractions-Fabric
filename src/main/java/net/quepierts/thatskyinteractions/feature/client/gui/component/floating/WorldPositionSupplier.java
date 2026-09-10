package net.quepierts.thatskyinteractions.feature.client.gui.component.floating;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public interface WorldPositionSupplier {

    static WorldPositionSupplier fixed(
            final @NonNull Vector3f position
    ) {
        final var copy = new Vector3f(position);
        return (dest) -> dest.set(copy.x(), copy.y(), copy.z());
    }

    static WorldPositionSupplier dynamic(
            final @NonNull Vector3f position
    ) {
        return (dest) -> dest.set(position);
    }

    static WorldPositionSupplier block(
            final @NonNull BlockPos position
    ) {
        final var copy = position.mutable();
        return (dest) -> dest.set(copy.getX(), copy.getY(), copy.getZ());
    }

    static WorldPositionSupplier block(
            final @NonNull BlockPos position,
            final @NonNull Vector3f offset
    ) {
        final var copy0 = position.mutable();
        final var copy1 = new Vector3f(offset);
        return (dest) -> dest.set(copy0.getX(), copy0.getY(), copy0.getZ()).add(copy1);
    }

    static WorldPositionSupplier entity(
            final @NonNull Entity   entity,
            final          float    yOffset
    ) {
        return (dest) -> dest.set(
                entity.getX(),
                entity.getY() + yOffset,
                entity.getZ()
        );
    }

    static WorldPositionSupplier entity(
            final @NonNull Entity   entity,
            final @NonNull Vector3f offset
    ) {
        final var copy = new Vector3f(offset);
        return (dest) -> {
            dest.set(
                    entity.getX(),
                    entity.getY(),
                    entity.getZ()
            ).add(copy);
        };
    }

    void get(final @NonNull Vector3f dest);
}
