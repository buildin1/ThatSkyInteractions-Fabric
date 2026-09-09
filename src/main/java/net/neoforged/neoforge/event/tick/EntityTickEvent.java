package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

public class EntityTickEvent extends Event {

    private final Entity entity;

    protected EntityTickEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public static class Pre extends EntityTickEvent {
        public Pre(Entity entity) {
            super(entity);
        }
    }
}
