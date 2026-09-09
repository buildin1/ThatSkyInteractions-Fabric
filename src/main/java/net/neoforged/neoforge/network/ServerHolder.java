package net.neoforged.neoforge.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * 兼容层：持有当前 MinecraftServer（等价 NeoForge ServerLifecycleHooks）。
 */
public final class ServerHolder {

    private static volatile MinecraftServer server;

    private ServerHolder() {}

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
            if (server == s) server = null;
        });
    }

    public static MinecraftServer get() {
        return server;
    }
}
