package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void tsi$beforeEntityTick(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Pre((Entity) (Object) this));
    }
}
