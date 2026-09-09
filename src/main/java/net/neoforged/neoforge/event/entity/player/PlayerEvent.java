package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class PlayerEvent extends Event {

    private final Player player;

    protected PlayerEvent(Player player) {
        this.player = player;
    }

    public Player getEntity() {
        return this.player;
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(Player player) {
            super(player);
        }
    }

    public static class StartTracking extends PlayerEvent {

        private final Entity target;

        public StartTracking(Player player, Entity target) {
            super(player);
            this.target = target;
        }

        public Entity getTarget() {
            return this.target;
        }
    }
}
