package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.quepierts.thatskyinteractions.feature.client.control.PlayerControlHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.x 的实体拾取在 {@code LocalPlayer#pick}；1.20.1 在 {@code GameRenderer#pick(float)}，
 * 所以背人时的拾取过滤挪到了 {@link GameRendererMixin}。
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Shadow
    public Input input;

    @Inject(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/tutorial/Tutorial;onInput(Lnet/minecraft/client/player/Input;)V"
            )
    )
    private void a4j$onInput(CallbackInfo ci) {

        PlayerControlHook.onUpdatePlayerMotion((LocalPlayer) (Object) this, this.input);

    }

}
