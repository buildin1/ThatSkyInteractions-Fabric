package dev.anvilcraft.lib.v2.input;

/**
 * 1.20.1 兼容层：{@code dev.anvilcraft.lib.v2.input.MouseButtonEvent} 的等价记录。
 *
 * <p>原版在 26.x 才把鼠标点击参数打包成记录，1.20.1 仍是散开的
 * {@code (double x, double y, int button)}。这里保持记录形态，
 * 让 GUI 组件树的签名不用改，只在与原版 Screen 交界处构造。
 */
public record MouseButtonEvent(double x, double y, int button) {
}
