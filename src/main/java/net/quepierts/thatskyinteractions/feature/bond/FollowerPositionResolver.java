package net.quepierts.thatskyinteractions.feature.bond;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public final class FollowerPositionResolver {

    public boolean locked = false;

    public double x;
    public double y;
    public double z;

    public int counter = -1;

    public void follow(
            final @NonNull Player   leader,
            final @NonNull Player   follower,
            final boolean           left
    ) {

        final var current       = follower.position();
        final var position      = PlayerBondSystem.computeHandholdPosition(leader, left);

        final var distance      = Math.sqrt(current.distanceToSqr(position));
        if (distance < 0.01) {
            this.counter        = 0;
            this.locked         = true;
        }

        if (this.counter > 60) {
            // force update

            follower            .teleportTo(position.x, position.y, position.z);
            this                .update(position);
            this.counter        = -1;
            return;
        } else if (this.counter > 10) {
            this.locked = false;
        }

        var dx                  = position.x - current.x;
        var dy                  = position.y - current.y;
        var dz                  = position.z - current.z;

        if (!this.locked) {
            final var length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            final var speed = Math.min(Math.max(5 - distance, 0) * 0.05 + 0.5, 1.0f);
            if (length > speed) {
                final var factor = speed / length;
                dx *= factor;
                dy *= factor;
                dz *= factor;
            }
        }

        final var delta         = new Vec3(dx, dy, dz);

        follower.setDeltaMovement(0, 0, 0);
        follower.move(MoverType.SELF, delta);

        // 远程玩家在客户端会被原版 LivingEntity#aiStep 按 lerpSteps 朝服务端下发的
        // 旧位置插值（lerpTo 每帧给 lerpSteps=3），正好把这里的移动拽回去——leader 视角
        // 看到的 follower 就是原地不动 + 被拽一下的抽搐。把 lerpSteps 清 0 让本次移动生效。
        if (follower.level().isClientSide() && !follower.isLocalPlayer()) {
            follower.lerpTo(
                    follower.getX(), follower.getY(), follower.getZ(),
                    follower.getYRot(), follower.getXRot(),
                    0, false
            );
        }

        if (this.counter < 0 || distance > 4 && follower.distanceToSqr(this.x, this.y, this.z) < 0.1) {
            // seems stuck somewhere
            this.counter ++;
        }
        this.update(follower.position());

    }

    public void clampRotation(
            Player      leader,
            Player      follower,
            float       partialTick
    ) {
        final var leaderYRot    = net.minecraft.util.Mth.rotLerp(partialTick, leader.yRotO, leader.getYRot());
        final var followerYRot  = net.minecraft.util.Mth.rotLerp(partialTick, follower.yRotO, follower.getYRot());

        follower.setYBodyRot(leader.yBodyRot);
        float delta = Mth.wrapDegrees(followerYRot - leaderYRot);
        float targetDelta = Mth.clamp(delta, -45.0F, 45.0F);
        follower.yRotO += targetDelta - delta;
        follower.setYRot(follower.getYRot() + targetDelta - delta);
        follower.setYHeadRot(follower.getYRot());
    }

    public boolean shouldUnhold(
            final @NonNull Player   leader,
            final @NonNull Player   follower
    ) {
        final var d2 = 64 * 64;
        return leader.distanceToSqr(follower) > d2;
    }


    private void update(final @NonNull Vec3 position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public void reset() {
        this.locked = false;
        this.counter = -1;
    }
}
