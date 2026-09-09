package dev.anvilcraft.lib.v2.network.packet;

import net.minecraft.world.entity.player.Player;

public interface IClientboundPacket extends IPacket {

    void handleOnClient(Player player);
}
