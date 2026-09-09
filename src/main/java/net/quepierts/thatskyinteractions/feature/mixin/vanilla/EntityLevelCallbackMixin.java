package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityLevelCallbackMixin {

    @Inject(method = "setLevelCallback", at = @At("HEAD"))
    private void tsi$onEntityLeaveLevel(EntityInLevelCallback callback, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        // 移出 level 时回调被置为 NULL（与 NeoForge 触发点一致）
        if (callback == EntityInLevelCallback.NULL && self.level() != null && self.level().getEntity(self.getUUID()) != null) {
            NeoForge.EVENT_BUS.post(new EntityLeaveLevelEvent(self, self.level()));
        }
    }
}
