package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class EntityMountEvent extends Event implements ICancellableEvent {

    private boolean canceled = false;

    private final Entity entityMounting;
    private final Entity entityBeingMounted;
    private final boolean isMounting;

    public EntityMountEvent(Entity entityMounting, Entity entityBeingMounted, boolean isMounting) {
        this.entityMounting = entityMounting;
        this.entityBeingMounted = entityBeingMounted;
        this.isMounting = isMounting;
    }

    @Override
    public boolean isCanceled() {
        return this.canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    /** NeoForge API：与 getEntityMounting 等价的别名 */
    public Entity getEntity() {
        return this.entityMounting;
    }

    public Entity getEntityMounting() {
        return this.entityMounting;
    }

    public Entity getEntityBeingMounted() {
        return this.entityBeingMounted;
    }

    public boolean isMounting() {
        return this.isMounting;
    }

    public boolean isDismounting() {
        return !this.isMounting;
    }
}
