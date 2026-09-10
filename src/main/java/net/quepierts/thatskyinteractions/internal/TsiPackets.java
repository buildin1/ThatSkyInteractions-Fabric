package net.quepierts.thatskyinteractions.internal;

import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IServerboundPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import dev.anvilcraft.lib.v2.network.codec.CustomPacketPayload;

import net.quepierts.thatskyinteractions.feature.animation.packet.AnimationRequestPacket;
import net.quepierts.thatskyinteractions.feature.animation.packet.AnimationSignalPacket;
import net.quepierts.thatskyinteractions.feature.animation.packet.ClientboundAnimationControlPacket;
import net.quepierts.thatskyinteractions.feature.animation.packet.ClientboundSyncAnimationControllerPacket;
import net.quepierts.thatskyinteractions.feature.bond.packet.ClientboundCarryPacket;
import net.quepierts.thatskyinteractions.feature.bond.packet.ClientboundHandholdPacket;
import net.quepierts.thatskyinteractions.feature.bond.packet.ClientboundSyncBondPacket;
import net.quepierts.thatskyinteractions.feature.bond.packet.UnholdRequestPacket;
import net.quepierts.thatskyinteractions.feature.control.packet.AlignBodyPacket;
import net.quepierts.thatskyinteractions.feature.control.packet.NavigatePacket;
import net.quepierts.thatskyinteractions.feature.control.packet.SyncYawPacket;
import net.quepierts.thatskyinteractions.feature.control.packet.UpdatePlayerBodyPacket;
import net.quepierts.thatskyinteractions.feature.data.packet.SyncDatapackPacket;
import net.quepierts.thatskyinteractions.feature.expression.call.packet.CallRequestPacket;
import net.quepierts.thatskyinteractions.feature.expression.call.packet.ChangeVoicePacket;
import net.quepierts.thatskyinteractions.feature.expression.packet.ExpressionControlPacket;
import net.quepierts.thatskyinteractions.feature.expression.packet.ExpressionRequestPacket;
import net.quepierts.thatskyinteractions.feature.friendship.packet.PlayerFriendshipControlPacket;
import net.quepierts.thatskyinteractions.feature.friendship.packet.PlayerFriendshipRequestPacket;
import net.quepierts.thatskyinteractions.feature.gui.packet.PlayerFriendshipUiPacket;
import net.quepierts.thatskyinteractions.feature.gui.packet.PlayerInteractionUiPacket;
import net.quepierts.thatskyinteractions.feature.interaction.packet.ClientboundInteractionControlPacket;
import net.quepierts.thatskyinteractions.feature.interaction.packet.InteractionRequestPacket;

/**
 * 网络包显式注册清单（Fabric 惯例）。
 * <p>
 * 为什么不扫描：Fabric Loader 不提供 FML 那种 ASM 扫描结果；运行时扫描需要
 * {@code Class.forName}（服务端误加载客户端类会崩），且在生产 jar 里
 * {@code getRootPaths()} 返回的是 jar 文件而非目录，会导致零注册。
 * <p>
 * 本清单与 mod 的 23 个包一一对应（S2C 15 + C2S 6 + 双向 2）。新增包时**必须**在此补一行。
 */
public final class TsiPackets {

    private TsiPackets() {}

    public static void register() {

        // ==================== 服务端 → 客户端（15） ====================
        s2c(AlignBodyPacket.TYPE, AlignBodyPacket.STREAM_CODEC);
        s2c(AnimationSignalPacket.TYPE, AnimationSignalPacket.STREAM_CODEC);
        s2c(ClientboundAnimationControlPacket.TYPE, ClientboundAnimationControlPacket.STREAM_CODEC);
        s2c(ClientboundCarryPacket.TYPE, ClientboundCarryPacket.STREAM_CODEC);
        s2c(ClientboundHandholdPacket.TYPE, ClientboundHandholdPacket.STREAM_CODEC);
        s2c(ClientboundInteractionControlPacket.TYPE, ClientboundInteractionControlPacket.STREAM_CODEC);
        s2c(ClientboundSyncAnimationControllerPacket.TYPE, ClientboundSyncAnimationControllerPacket.STREAM_CODEC);
        s2c(ClientboundSyncBondPacket.TYPE, ClientboundSyncBondPacket.STREAM_CODEC);
        s2c(ExpressionControlPacket.TYPE, ExpressionControlPacket.STREAM_CODEC);
        s2c(NavigatePacket.TYPE, NavigatePacket.STREAM_CODEC);
        s2c(PlayerFriendshipControlPacket.TYPE, PlayerFriendshipControlPacket.STREAM_CODEC);
        s2c(PlayerFriendshipUiPacket.TYPE, PlayerFriendshipUiPacket.STREAM_CODEC);
        s2c(PlayerInteractionUiPacket.TYPE, PlayerInteractionUiPacket.STREAM_CODEC);
        s2c(SyncDatapackPacket.TYPE, SyncDatapackPacket.STREAM_CODEC);
        s2c(SyncYawPacket.TYPE, SyncYawPacket.STREAM_CODEC);

        // ==================== 客户端 → 服务端（6） ====================
        c2s(AnimationRequestPacket.TYPE, AnimationRequestPacket.STREAM_CODEC);
        c2s(CallRequestPacket.TYPE, CallRequestPacket.STREAM_CODEC);
        c2s(ExpressionRequestPacket.TYPE, ExpressionRequestPacket.STREAM_CODEC);
        c2s(InteractionRequestPacket.TYPE, InteractionRequestPacket.STREAM_CODEC);
        c2s(PlayerFriendshipRequestPacket.TYPE, PlayerFriendshipRequestPacket.STREAM_CODEC);
        c2s(UnholdRequestPacket.TYPE, UnholdRequestPacket.STREAM_CODEC);

        // ==================== 双向敏感包（2，两个方向各注册一次） ====================
        bi(ChangeVoicePacket.TYPE, ChangeVoicePacket.STREAM_CODEC);
        bi(UpdatePlayerBodyPacket.TYPE, UpdatePlayerBodyPacket.STREAM_CODEC);
    }

    private static <T extends IClientboundPacket> void s2c(
            final CustomPacketPayload.Type<T> type,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        dev.anvilcraft.lib.v2.network.codec.PayloadCodecs.register(type, codec);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            // 反射转发，避免服务端加载客户端专属类
            dev.anvilcraft.lib.v2.network.register.ClientReceiverHook.register(type, codec);
        }
    }

    private static <T extends IServerboundPacket> void c2s(
            final CustomPacketPayload.Type<T> type,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        dev.anvilcraft.lib.v2.network.codec.PayloadCodecs.register(type, codec);
        // 1.20.1 按 channel id 收包，自行解码后再切回主线程
        ServerPlayNetworking.registerGlobalReceiver(type.id(), (server, player, handler, buf, sender) -> {
            final T payload = codec.decode(dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf.of(buf));
            server.execute(() -> payload.handleOnServer(player));
        });
    }

    private static <T extends IClientboundPacket & IServerboundPacket> void bi(
            final CustomPacketPayload.Type<T> type,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        c2s(type, codec);
        s2c(type, codec);
    }
}
