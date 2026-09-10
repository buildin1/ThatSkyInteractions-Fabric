package net.quepierts.thatskyinteractions.feature.animation.packet;

import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.animation.PlayerAnimationController;
import net.quepierts.thatskyinteractions.feature.animation.PlayerAnimationSystem;
import org.jspecify.annotations.NonNull;

public record ClientboundSyncAnimationControllerPacket(
        int                                 id,
        PlayerAnimationController.Serialized serialized
) implements IClientboundPacket {

    public static final Type<ClientboundSyncAnimationControllerPacket> TYPE
            = IPacket.type(ThatSkyInteractions.location("animation/sync"));

    public static final StreamCodec<ByteBuf, ClientboundSyncAnimationControllerPacket> STREAM_CODEC
            = StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundSyncAnimationControllerPacket::id,
                    PlayerAnimationController.STREAM_CODEC,
                    ClientboundSyncAnimationControllerPacket::serialized,
                    ClientboundSyncAnimationControllerPacket::new
            );

    public static ClientboundSyncAnimationControllerPacket of(
            final @NonNull LivingEntity     entity
    ) {
        return new ClientboundSyncAnimationControllerPacket(
                entity.getId(),
                PlayerAnimationSystem.getAnimationData(entity).getController().serialize()
        );
    }

    @Override
    public void handleOnClient(final @NonNull Player player) {

        final var entity        = player.level().getEntity(this.id());
        final var animatable    = PlayerAnimationSystem.tryParseAnimatable(entity);
        if (animatable != null) {

            final var attachment = PlayerAnimationSystem.getAnimationData(animatable);
            final var controller = attachment.getController();
            controller.deserialize(this.serialized());
            // 用客户端本地的 tickCount 重设时间基准，避免服务端/客户端 tickCount 不一致导致动画跳变
            controller.resyncTick(animatable.tickCount);

            // just for in case
            animatable.yBodyRot = animatable.getYRot();

        }

    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
