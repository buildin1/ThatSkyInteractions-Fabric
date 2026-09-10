package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.core.animation.DefaultMinecraftSkeletonLayout;
import net.quepierts.thatskyinteractions.feature.animation.PlayerAnimationSystem;
import net.quepierts.thatskyinteractions.feature.bond.PlayerBondAttachment;
import net.quepierts.thatskyinteractions.feature.client.animation.PlayerAnimationHook;
import net.quepierts.thatskyinteractions.feature.client.model.MinecraftModelAdaptor;
import net.quepierts.thatskyinteractions.feature.client.model.MinecraftModelSkeleton;
import net.quepierts.thatskyinteractions.feature.client.render.EntityModelExtension;
import net.quepierts.thatskyinteractions.feature.client.renderstate.AnimationStateModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.20.1 的 {@code setupAnim} 直接吃实体，没有 26.x 的 EntityRenderState 管线，
 * 因此动画的每帧推进与姿态应用都在这里完成。
 */
@Mixin(PlayerModel.class)
public class PlayerModelMixin implements EntityModelExtension {

    @Unique
    private MinecraftModelAdaptor a4j$ModelAdaptor;

    @Shadow @Final public ModelPart leftSleeve;
    @Shadow @Final public ModelPart rightSleeve;
    @Shadow @Final public ModelPart leftPants;
    @Shadow @Final public ModelPart rightPants;
    @Shadow @Final public ModelPart jacket;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void a4j$init(final ModelPart root, final boolean slim, final CallbackInfo ci) {
        final var skeleton = MinecraftModelSkeleton.auto(root, DefaultMinecraftSkeletonLayout.HUMANOID);
        this.a4j$ModelAdaptor = MinecraftModelAdaptor.of(skeleton);
    }

    /**
     * 1.20.1 必须在原版 {@code setupAnim} 之前把各部件复位。
     *
     * <p>模型实例是复用的，而原版 {@code setupAnim} 只无条件重写旋转，位置/缩放只在
     * 部分分支里赋值。动画一旦改过这些分量，残值会跨帧累积；欧拉角在残值基础上做插值
     * 还会绕远路——表现就是手臂整圈旋转而不是弯曲。
     *
     * <p>做法取自原作者在 SimpleAnimator (1.20 分支) 里的 PlayerModelMixin。
     */
    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("HEAD")
    )
    private void tsi$resetPose(
            final LivingEntity  entity,
            final float         limbSwing,
            final float         limbSwingAmount,
            final float         ageInTicks,
            final float         netHeadYaw,
            final float         headPitch,
            final CallbackInfo  ci
    ) {
        final var humanoid = (net.minecraft.client.model.HumanoidModel<?>) (Object) this;
        humanoid.head       .resetPose();
        humanoid.body       .resetPose();
        humanoid.leftArm    .resetPose();
        humanoid.rightArm   .resetPose();
        humanoid.leftLeg    .resetPose();
        humanoid.rightLeg   .resetPose();
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    public void tsi$setupAnim(
            final LivingEntity  entity,
            final float         limbSwing,
            final float         limbSwingAmount,
            final float         ageInTicks,
            final float         netHeadYaw,
            final float         headPitch,
            final CallbackInfo  ci
    ) {
        if (PlayerAnimationSystem.tryParseAnimatable(entity) == null) {
            return;
        }

        boolean posed = false;

        final var partialTick = net.minecraft.client.Minecraft.getInstance().getFrameTime();
        final var controller  = AnimationStateModifier.INSTANCE.accept(entity, partialTick);

        if (controller != null) {
            PlayerAnimationHook.onSetupAnimation(controller, this.a4j$ModelAdaptor);
            posed = true;
        }

        // 动作细节增强：牵手/背起时头部朝向对方（在动画姿态基础上叠加，不覆盖）
        if (entity instanceof Player self) {
            final Player partner = tsi$findPartner(self);
            if (partner != null) {
                final var humanoid = (net.minecraft.client.model.HumanoidModel<?>) (Object) this;
                tsi$lookAt(humanoid.head, self, partner);
                posed = true;
            }
        }

        if (posed) {
            tsi$copyOuterLayers();
        }
    }

    /**
     * 原版把外层部件（帽子/袖子/裤腿/夹克）从基础部件拷贝这一步，发生在
     * {@code HumanoidModel#setupAnim}（hat）与 {@code PlayerModel#setupAnim}（其余）
     * 的中途——都在本 mixin 的 TAIL 注入之前。所以动画改完基础部件后必须再拷一次，
     * 否则外层还停在动画前的姿态：穿着皮肤第二层时身体和手臂会明显分层错位。
     */
    @Unique
    private void tsi$copyOuterLayers() {
        final var humanoid = (net.minecraft.client.model.HumanoidModel<?>) (Object) this;
        humanoid.hat        .copyFrom(humanoid.head);
        this.leftSleeve     .copyFrom(humanoid.leftArm);
        this.rightSleeve    .copyFrom(humanoid.rightArm);
        this.leftPants      .copyFrom(humanoid.leftLeg);
        this.rightPants     .copyFrom(humanoid.rightLeg);
        this.jacket         .copyFrom(humanoid.body);
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
