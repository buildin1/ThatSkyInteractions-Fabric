package dev.anvilcraft.lib.v2.rendering;

/**
 * 1.20.1 兼容层：{@code Mth} 缺少的 float 重载。
 *
 * <p>26.x 的 {@code Mth} 有 {@code length(float,float)} 与 {@code sign(float)}，
 * 1.20.1 只有 double 版本，直接调用会在 SDF 距离函数里到处产生窄化错误。
 */
public final class MthF {

    private MthF() {}

    public static float length(final float x, final float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float lengthSquared(final float x, final float y) {
        return x * x + y * y;
    }

    public static float sign(final float value) {
        return Math.signum(value);
    }
}
