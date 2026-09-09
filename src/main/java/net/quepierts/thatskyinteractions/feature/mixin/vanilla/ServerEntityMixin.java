package net.quepierts.thatskyinteractions.feature.mixin.vanilla;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {

    @Shadow
    private Entity entity;

    /**
     * NeoForge 在 addPairing 的尾部触发（发送实体生成包并 startSeenByPlayer 之后），
     * 保证客户端此时已能查到该实体，动画/羁绊同步包不会被丢弃。
     */
    @Inject(method = "addPairing", at = @At("TAIL"))
    private void tsi$startTracking(ServerPlayer player, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.StartTracking(player, this.entity));
    }
}
