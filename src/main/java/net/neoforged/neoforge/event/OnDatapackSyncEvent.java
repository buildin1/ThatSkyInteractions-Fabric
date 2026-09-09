package net.neoforged.neoforge.event;

import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

import java.util.stream.Stream;

public class OnDatapackSyncEvent extends Event {

    private final MinecraftServer server;
    private final ServerPlayer player;
    private final ReloadableServerRegistries.Holder registries;

    public OnDatapackSyncEvent(MinecraftServer server, ServerPlayer player, ReloadableServerRegistries.Holder registries) {
        this.server = server;
        this.player = player;
        this.registries = registries;
    }

    /** 相关玩家：指定玩家时仅该玩家，否则全部在线玩家（与 NeoForge 语义一致） */
    public Stream<ServerPlayer> getRelevantPlayers() {
        if (this.player != null) {
            return Stream.of(this.player);
        }
        return this.server.getPlayerList().getPlayers().stream();
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public ReloadableServerRegistries.Holder getRegistries() {
        return this.registries;
    }
}
