package dev.anvilcraft.lib.v2.input;

/**
 * 1.20.1 兼容层：{@code dev.anvilcraft.lib.v2.input.KeyEvent} 的等价记录。
 * 1.20.1 的 KeyboardHandler 仍是散开的 (key, scancode, action, modifiers)。
 */
public record KeyEvent(int key, int scancode, int modifiers) {
}
