package net.quepierts.thatskyinteractions.feature.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.bond.PlayerBondAttachment;
import net.quepierts.thatskyinteractions.feature.interaction.event.PlayerInteractionEvent;

/**
 * 互动反馈：牵手/背起/接受互动时播放粒子与音效（纯客户端表现）。
 * 每个客户端只根据自己本地玩家的状态变化播放，无需网络同步。
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = ThatSkyInteractions.MODID)
public final class ClientInteractionFeedback {

    private static boolean lastHolding = false;
    private static boolean lastCarrying = false;

    private ClientInteractionFeedback() {}

    public static void tick(final Minecraft minecraft) {

        final Player self = minecraft.player;
        if (self == null || minecraft.level == null) {
            lastHolding = false;
            lastCarrying = false;
            return;
        }

        final PlayerBondAttachment bond = PlayerBondAttachment.getAttachment(self);
        final var handhold = bond.getHandhold();
        final var carry = bond.getCarry();

        final boolean holding = handhold.isFollowing() || handhold.isLeading();
        final boolean carrying = carry.isCarrying() || carry.isBeingCarried();

        if (holding != lastHolding) {
            lastHolding = holding;
            if (holding) {
                ClientCameraEffects.trigger();
                play(self, SoundEvents.AMETHYST_BLOCK_CHIME, 0.9f, 1.4f);
                particles(self, ParticleTypes.HEART, 4);
                final Player leader = handhold.getLeader();
                if (leader != null && leader != self) {
                    particles(leader, ParticleTypes.HEART, 4);
                }
            } else {
                play(self, SoundEvents.NOTE_BLOCK_PLING.value(), 0.5f, 0.8f);
            }
        }

        if (carrying != lastCarrying) {
            lastCarrying = carrying;
            if (carrying) {
                ClientCameraEffects.trigger();
                play(self, SoundEvents.ARMOR_EQUIP_LEATHER, 0.9f, 1.2f);
                particles(self, ParticleTypes.CLOUD, 8);
                final Player carrier = carry.getCarrier();
                if (carrier != null && carrier != self) {
                    particles(carrier, ParticleTypes.CLOUD, 8);
                }
            } else {
                play(self, SoundEvents.PLAYER_SMALL_FALL, 0.8f, 1.0f);
                particles(self, ParticleTypes.CLOUD, 5);
            }
        }
    }

    @SubscribeEvent
    public static void onInteractionAccepted(final PlayerInteractionEvent.Accept.Post event) {

        if (!event.isClient()) {
            return;
        }

        final var local = Minecraft.getInstance().player;
        if (local == null) {
            return;
        }

        // 只在本地玩家参与的互动上反馈
        if (event.getRequester() != local && event.getReceiver() != local) {
            return;
        }

        ClientCameraEffects.trigger();
        play(local, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f, 1.6f);
        particles(local, ParticleTypes.HAPPY_VILLAGER, 8);

        final var other = event.getRequester() == local ? event.getReceiver() : event.getRequester();
        if (other != null && other != local) {
            particles(other, ParticleTypes.HAPPY_VILLAGER, 8);
        }
    }

    private static void play(final Player player, final SoundEvent sound, final float volume, final float pitch) {
        player.level().playLocalSound(
                player.getX(), player.getEyeY(), player.getZ(),
                sound, SoundSource.PLAYERS, volume, pitch, false
        );
    }

    private static void particles(final Player player, final ParticleOptions particle, final int count) {
        final var random = player.getRandom();
        for (int i = 0; i < count; i++) {
            final double x = player.getX() + (random.nextDouble() - 0.5) * 0.9;
            final double y = player.getY() + random.nextDouble() * 1.8 + 0.2;
            final double z = player.getZ() + (random.nextDouble() - 0.5) * 0.9;
            player.level().addParticle(
                    particle,
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.05,
                    random.nextDouble() * 0.05,
                    (random.nextDouble() - 0.5) * 0.05
            );
        }
    }
}
