package net.neoforged.neoforge.event.tick;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public class LevelTickEvent extends Event {

    private final Level level;

    protected LevelTickEvent(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return this.level;
    }

    public static class Pre extends LevelTickEvent {
        public Pre(Level level) {
            super(level);
        }
    }
}
