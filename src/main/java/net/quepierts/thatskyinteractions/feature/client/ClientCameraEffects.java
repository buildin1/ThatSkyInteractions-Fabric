package net.quepierts.thatskyinteractions.feature.client;

import net.minecraft.client.Minecraft;

/**
 * 互动镜头效果：互动开始时给一个短暂的 FOV 推进脉冲（纯客户端表现）。
 */
public final class ClientCameraEffects {

    /** 脉冲进度 1 → 0 */
    private static float pulse = 0.0F;

    /** 每次 tick 的衰减量（0.6 秒走完） */
    private static final float DECAY = 1.0F / 12.0F;

    /** 最大 FOV 变化比例 */
    private static final float STRENGTH = 0.05F;

    private ClientCameraEffects() {}

    public static void trigger() {
        pulse = 1.0F;
    }

    public static void tick(final Minecraft minecraft) {
        if (pulse > 0.0F) {
            pulse = Math.max(0.0F, pulse - DECAY);
        }
    }

    /** FOV 乘算因子：1 → 0.95 → 1 的平滑脉冲 */
    public static float fovFactor() {
        if (pulse <= 0.0F) {
            return 1.0F;
        }
        return 1.0F - STRENGTH * (float) Math.sin(pulse * Math.PI);
    }
}
