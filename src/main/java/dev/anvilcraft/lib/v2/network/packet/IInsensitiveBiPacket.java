package dev.anvilcraft.lib.v2.network.packet;

import net.minecraft.world.entity.player.Player;

public interface IInsensitiveBiPacket extends IClientboundPacket, IServerboundPacket {

    void handleOnBothSide(Player player);
}
