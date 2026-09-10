package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.quepierts.thatskyinteractions.feature.client.bond.ClientBondSystem;
import net.quepierts.thatskyinteractions.feature.client.render.GameRendererUpdateEvent;
import net.quepierts.thatskyinteractions.feature.registry.AttachmentTypes;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

/**
 * 1.20.1 没有 GameRenderer#update / #extractGui（都是 26.x 的 extract-render 两段式产物）。
 * 每帧事件改挂 render 头部；顶层 GUI 绘制由 ClientCompatBootstrap 走 HudRenderCallback。
 *
 * <p>另外 26.x 的实体拾取在 LocalPlayer#pick，1.20.1 在 GameRenderer#pick(float)，
 * 背人时"别把背着的人挡在准星上"的过滤因此挂在这里。
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(
            method = "render(FJZ)V",
            at = @At("HEAD")
    )
    private void tsi$update(
            final float         partialTicks,
            final long          nanoTime,
            final boolean       renderLevel,
            final CallbackInfo  ci
    ) {
        NeoForge.EVENT_BUS.post(new GameRendererUpdateEvent());
    }

    @WrapOperation(
            method = "pick(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"
            )
    )
    private static @Nullable EntityHitResult tsi$filter(
            final Entity                        except,
            final Vec3                          from,
            final Vec3                          _to,
            final AABB                          box,
            final Predicate<Entity>             matching,
            final double                        maxValue,
            final Operation<EntityHitResult>    original
    ) {

        final var carrying = ClientBondSystem.getLocalAttachment().getCarry().isCarrying();
        final Predicate<Entity> predicate = carrying ?
                entity -> {
                    final var attachment = ((net.neoforged.neoforge.attachment.IAttachmentHolder) entity).getExistingDataOrNull(AttachmentTypes.PLAYER_BOUND);
                    if (attachment != null && attachment.getCarry().getCarrier() == except) {
                        return false;
                    }
                    return EntitySelector.NO_SPECTATORS.and(Entity::isPickable).test(entity);
                } :
                matching;

        return original.call(except, from, _to, box, predicate, maxValue);
    }
}
