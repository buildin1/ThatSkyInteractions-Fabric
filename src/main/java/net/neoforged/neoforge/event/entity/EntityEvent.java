package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.neoforged.bus.api.Event;

public class EntityEvent extends Event {

    private final Entity entity;

    protected EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public static class Size extends EntityEvent {

        private EntityDimensions newSize;

        public Size(Entity entity, EntityDimensions newSize) {
            super(entity);
            this.newSize = newSize;
        }

        public EntityDimensions getOldSize() {
            return this.getEntity().getDimensions(this.getEntity().getPose());
        }

        public EntityDimensions getNewSize() {
            return this.newSize;
        }

        public void setNewSize(EntityDimensions size) {
            this.newSize = size;
        }
    }
}
