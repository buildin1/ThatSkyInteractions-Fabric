package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public class EntityLeaveLevelEvent extends Event {

    private final Entity entity;
    private final Level level;

    public EntityLeaveLevelEvent(Entity entity, Level level) {
        this.entity = entity;
        this.level = level;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Level getLevel() {
        return this.level;
    }
}
