package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.quepierts.thatskyinteractions.feature.control.event.EntityTurnEvent;
import net.quepierts.veynir.core.misc.Generic;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IAttachmentHolder, net.neoforged.neoforge.attachment.AttachmentHolderAccess {

    @org.spongepowered.asm.mixin.Unique
    private final java.util.Map<net.neoforged.neoforge.attachment.AttachmentType<?>, Object> tsi$attachments = new java.util.IdentityHashMap<>();

    @org.spongepowered.asm.mixin.Unique
    @Override
    public java.util.Map<net.neoforged.neoforge.attachment.AttachmentType<?>, Object> tsi$attachments() {
        return this.tsi$attachments;
    }

    @Shadow
    private Vec3 position;

    @Shadow
    public abstract float getBbHeight();

    @Shadow
    public abstract void refreshDimensions();

    // 1.20.1 的实体存档还是 CompoundTag，26.x 的 ValueOutput/ValueInput 尚未引入
    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void tsi$saveAttachments(net.minecraft.nbt.CompoundTag tag, CallbackInfoReturnable<net.minecraft.nbt.CompoundTag> cir) {
        net.neoforged.neoforge.attachment.AttachmentPersistence.save((Entity) (Object) this, tag);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void tsi$loadAttachments(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        net.neoforged.neoforge.attachment.AttachmentPersistence.load((Entity) (Object) this, tag);
    }

    @Shadow
    private @Nullable Entity vehicle;

    @Inject(
            method = "turn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tsi$beforeEntityTurn(
            final double        xo,
            final double        yo,
            final CallbackInfo  ci
    ) {
        final var event = new EntityTurnEvent.Pre(Generic.cast(this), xo, yo);
        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "turn",
            at = @At("TAIL")
    )
    private void tsi$afterEntityTurn(
            final double        xo,
            final double        yo,
            final CallbackInfo  ci
    ) {
        NeoForge.EVENT_BUS.post(new EntityTurnEvent.Post(Generic.cast(this), xo, yo));
    }

    // 26.x 在 startRiding 里用 EntityType#canSerialize 挡住"骑玩家"，所以原项目要改写它的返回值。
    // 1.20.1 没有这道闸：couldAcceptPassenger 只有 Marker 覆写，而 force=true 会直接跳过
    // canRide / canAddPassenger，因此 startRiding(player, true) 本就成立，无需注入。

    /*@Inject(
            method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;addPassenger(Lnet/minecraft/world/entity/Entity;)V"
            )
    )
    private void tsi$refreshDimensions(
            final Entity                            entityToRide,
            final boolean                           force,
            final boolean                           sendEventAndTriggers,
            final CallbackInfoReturnable<Boolean>   cir
    ) {
        if ((Object) this instanceof Player) {
            this.refreshDimensions();
        }
    }

    @Inject(
            method      = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            at          = @At("HEAD"),
            cancellable = true
    )
    private void tsi$onPositionRider(
            final Entity                passenger,
            final Entity.MoveFunction   moveFunction,
            final CallbackInfo          ci
    ) {

        final var attachment    = ((net.neoforged.neoforge.attachment.IAttachmentHolder) this).getExistingDataOrNull(AttachmentTypes.PLAYER_BOUND);

        if (attachment == null) {
            return;
        }

        final var relation      = attachment.getCarry();

        if (relation.getCarried() != passenger) {
            return;
        }

        final var position      = this.position;
        moveFunction.accept(passenger, position.x, position.y + this.getBbHeight(), position.z);

        ci.cancel();

    }*/

}
