package dev.anvilcraft.lib.v2.network.register;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnvilLib API 兼容层：网络包注册入口。
 * <p>
 * 原版 AnvilLib 用 FML 的 ASM 扫描（{@code ModFileScanData} + 包级 {@code @Network}）自动发现包类；
 * Fabric Loader 不提供等价 API，运行时扫描又存在“生产 jar 扫不到”“服务端误加载客户端类”两个风险，
 * 因此这里改为调用显式的注册清单 {@code net.quepierts.thatskyinteractions.internal.TsiPackets}。
 * <p>
 * 运行时行为（包 ID、编解码器、方向、处理器、发送语义）与原实现完全一致。
 */
public final class NetworkRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger("anvillib-network-compat");

    private NetworkRegistrar() {}

    public static void register(PayloadRegistrar registrar, String modId) {
        try {
            net.quepierts.thatskyinteractions.internal.TsiPackets.register();
            LOGGER.info("Registered packets via explicit list");
        } catch (Throwable t) {
            LOGGER.error("Failed to register packets", t);
        }
    }
}
