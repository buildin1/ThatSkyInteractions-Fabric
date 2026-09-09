package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.core.animation.DefaultMinecraftSkeletonLayout;
import net.quepierts.thatskyinteractions.feature.bond.PlayerBondAttachment;
import net.quepierts.thatskyinteractions.feature.client.animation.PlayerAnimationHook;
import net.quepierts.thatskyinteractions.feature.client.model.MinecraftModelAdaptor;
import net.quepierts.thatskyinteractions.feature.client.model.MinecraftModelSkeleton;
import net.quepierts.thatskyinteractions.feature.client.render.EntityModelExtension;
import net.quepierts.thatskyinteractions.feature.client.renderstate.AnimationStateModifier;
import net.quepierts.thatskyinteractions.feature.client.renderstate.ClientRenderStateData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin implements EntityModelExtension {

    @Unique
    private MinecraftModelAdaptor a4j$ModelAdaptor;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void a4j$init(final ModelPart root, final boolean slim, final CallbackInfo ci) {
        final var skeleton = MinecraftModelSkeleton.auto(root, DefaultMinecraftSkeletonLayout.HUMANOID);
        this.a4j$ModelAdaptor = MinecraftModelAdaptor.of(skeleton);
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void tsi$setupAnim(final AvatarRenderState state, final CallbackInfo ci) {
        final var extension = (net.neoforged.neoforge.client.extensions.IRenderStateExtension) state;
        final var controller = extension.getRenderData(AnimationStateModifier.CONTEXT_KEY);

        if (controller != null) {
            PlayerAnimationHook.onSetupAnimation(controller, this.a4j$ModelAdaptor);
        }

        // 动作细节增强：牵手/背起时头部朝向对方（在动画姿态基础上叠加，不覆盖）
        final Entity entity = extension.getRenderData(ClientRenderStateData.ENTITY);
        if (entity instanceof Player self) {
            final Player partner = tsi$findPartner(self);
            if (partner != null) {
                // head 声明在父类 HumanoidModel 中，运行时向上转型获取
                final var humanoid = (net.minecraft.client.model.HumanoidModel<?>) (Object) this;
                tsi$lookAt(humanoid.head, self, partner);
            }
        }
    }

    @Unique
    private static Player tsi$findPartner(final Player self) {

        final PlayerBondAttachment bond = PlayerBondAttachment.getAttachment(self);
        final var handhold = bond.getHandhold();

        if (handhold.isFollowing()) {
            return handhold.getLeader();
        }
        if (handhold.isLeading()) {
            final Player left = handhold.getLeft();
            if (left != null && left != self) {
                return left;
            }
            final Player right = handhold.getRight();
            if (right != null && right != self) {
                return right;
            }
        }

        final var carry = bond.getCarry();
        if (carry.isCarrying()) {
            return carry.getCarried();
        }
        if (carry.isBeingCarried()) {
            return carry.getCarrier();
        }

        return null;
    }

    @Unique
    private static void tsi$lookAt(final ModelPart head, final Player self, final Player target) {

        final var eye = self.getEyePosition();
        final var other = target.getEyePosition();

        final double dx = other.x - eye.x;
        final double dy = other.y - eye.y;
        final double dz = other.z - eye.z;
        final double horizontal = Math.sqrt(dx * dx + dz * dz);

        if (horizontal < 0.05) {
            return;
        }

        final float desiredYaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        final float desiredPitch = (float) (-(Mth.atan2(dy, horizontal) * (180.0 / Math.PI)));

        final float yawOffset = Mth.clamp(Mth.wrapDegrees(desiredYaw - self.yBodyRot), -55.0F, 55.0F);
        final float pitch = Mth.clamp(desiredPitch, -30.0F, 30.0F);

        head.yRot += (yawOffset * Mth.DEG_TO_RAD - head.yRot) * 0.5F;
        head.xRot += (pitch * Mth.DEG_TO_RAD - head.xRot) * 0.5F;
    }

    @Unique
    @Override
    public MinecraftModelAdaptor a4j$GetModelAdaptor() {
        return this.a4j$ModelAdaptor;
    }
}
