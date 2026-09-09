package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void tsi$beforeLevelTick(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new LevelTickEvent.Pre((ServerLevel) (Object) this));
    }
}
