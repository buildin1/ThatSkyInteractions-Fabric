package net.quepierts.thatskyinteractions.feature.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

// 1.20.1 用 TextureSheetParticle（SingleQuadParticle 的 sprite 构造是 26.x 才有的）
public final class CallParticle extends TextureSheetParticle {

    private final @NonNull Player player;

    public CallParticle(
            final ClientLevel level,
            final double x,
            final double y,
            final double z,
            final TextureAtlasSprite sprite,
            final @NonNull Player player
    ) {
        super(level, x, y, z);
        this.setSprite(sprite);
        this.quadSize = 0.05f;
        this.player = player;
    }

    @Override
    public void tick() {

        final var player        = this.player;
        if (player.isRemoved()) {
            this.remove();
            return;
        }

        final var eyeHeight     = player.getEyeHeight();
        this.x                  = player.getX();
        this.y                  = player.getY() + eyeHeight;
        this.z                  = player.getZ();
        this.xo                 = this.x;
        this.yo                 = this.y;
        this.zo                 = this.z;
        this.alpha              = Math.max(0.0F, this.alpha - 0.1F);
        this.quadSize           = this.quadSize + 0.1F;

        if (this.alpha <= 0.0F) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
