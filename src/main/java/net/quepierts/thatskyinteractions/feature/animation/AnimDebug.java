package net.quepierts.thatskyinteractions.feature.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 临时动画链路埋点（诊断用，定位完成后移除）。
 */
public final class AnimDebug {

    public static final Logger LOG = LoggerFactory.getLogger("TSI-ANIM");

    private AnimDebug() {}

    public static String side() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? "C" : "S";
    }

    public static void log(final String format, final Object... args) {
        LOG.info("[{}] " + format, prepend(side(), args));
    }

    private static long lastNegativeLog = 0L;

    /** 动画时间倒退（delta < 0）是两端不同步/跳变的直接信号，限流 1 秒 */
    public static void negativeDelta(final float delta, final Object layer, final Object animation) {
        final long now = System.currentTimeMillis();
        if (now - lastNegativeLog < 1000L) {
            return;
        }
        lastNegativeLog = now;
        LOG.warn("[{}] NEGATIVE delta={} layer={} anim={}", side(), delta, layer, animation);
    }

    private static long lastMaskLog = 0L;

    /** 执行掩码（骑乘时下半身关闭）翻转会导致姿态抖动，限流 1 秒 */
    public static void maskFlip(final Object entity, final boolean sitting) {
        final long now = System.currentTimeMillis();
        if (now - lastMaskLog < 1000L) {
            return;
        }
        lastMaskLog = now;
        LOG.warn("[{}] executionMask LOWER_BODY {} for {}", side(), sitting ? "OFF" : "ON", entity);
    }

    private static Object[] prepend(final Object first, final Object[] rest) {
        final Object[] result = new Object[rest.length + 1];
        result[0] = first;
        System.arraycopy(rest, 0, result, 1, rest.length);
        return result;
    }
}
