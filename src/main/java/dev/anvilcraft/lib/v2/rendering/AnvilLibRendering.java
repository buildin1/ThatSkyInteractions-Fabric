package dev.anvilcraft.lib.v2.rendering;

import net.minecraft.resources.Identifier;

/**
 * AnvilLib API 兼容层：仅保留 SDF 渲染所需的位置工具。
 * （原类为 NeoForge mod 入口，其余功能未在本 mod 使用范围内）
 */
public final class AnvilLibRendering {

    public static final boolean DEBUG = System.getProperty("anvillib.rendering.debugMode") != null;
    public static final String MODID = "anvillib_rendering";

    private AnvilLibRendering() {}

    public static Identifier location(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
