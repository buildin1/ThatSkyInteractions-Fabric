package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/**
 * NeoForge 的 LevelTickEvent.Pre 在客户端同样会触发（客户端 level 的 tween 依赖它推进）。
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void tsi$beforeLevelTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new LevelTickEvent.Pre((Level) (Object) this));
    }
}
