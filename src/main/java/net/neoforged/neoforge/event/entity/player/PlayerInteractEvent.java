package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerInteractEvent extends Event implements ICancellableEvent {

    private boolean canceled = false;

    protected final Player player;
    protected final Level level;
    protected final InteractionHand hand;

    protected PlayerInteractEvent(Player player, Level level, InteractionHand hand) {
        this.player = player;
        this.level = level;
        this.hand = hand;
    }

    @Override
    public boolean isCanceled() {
        return this.canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public Player getEntity() {
        return this.player;
    }

    public Level getLevel() {
        return this.level;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public static class EntityInteract extends PlayerInteractEvent {

        private final Entity target;

        public EntityInteract(Player player, Level level, Entity target, InteractionHand hand) {
            super(player, level, hand);
            this.target = target;
        }

        public Entity getTarget() {
            return this.target;
        }

        private net.minecraft.world.InteractionResult cancellationResult = net.minecraft.world.InteractionResult.PASS;

        public void setCancellationResult(net.minecraft.world.InteractionResult result) {
            this.cancellationResult = result;
        }

        public net.minecraft.world.InteractionResult getCancellationResult() {
            return this.cancellationResult;
        }
    }
}
