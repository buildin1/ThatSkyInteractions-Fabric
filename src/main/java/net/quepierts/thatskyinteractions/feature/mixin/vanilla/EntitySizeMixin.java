package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntitySizeMixin {

    @ModifyExpressionValue(
            method = "refreshDimensions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"
            )
    )
    private EntityDimensions tsi$onSize(EntityDimensions original) {
        var event = new EntityEvent.Size((Entity) (Object) this, original);
        NeoForge.EVENT_BUS.post(event);
        return event.getNewSize();
    }
}
