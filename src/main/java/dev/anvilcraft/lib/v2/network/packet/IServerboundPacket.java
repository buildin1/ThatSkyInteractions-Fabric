package dev.anvilcraft.lib.v2.network.packet;

import net.minecraft.world.entity.player.Player;

public interface IServerboundPacket extends IPacket {

    void handleOnServer(Player player);
}
