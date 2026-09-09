package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class PlayerTickEvent extends Event {

    private final Player player;

    private PlayerTickEvent(Player player) {
        this.player = player;
    }

    public Player getEntity() {
        return this.player;
    }

    public static class Pre extends PlayerTickEvent {
        public Pre(Player player) {
            super(player);
        }
    }

    public static class Post extends PlayerTickEvent {
        public Post(Player player) {
            super(player);
        }
    }
}
