package com.hbm.interfaces;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Tiles that react to GUI buttons (NBTControlPacket) */
public interface IControlReceiver {

	public boolean hasPermission(Player player);

	public void receiveControl(CompoundTag data);
	/* this was the easiest way of doing this without needing to change all 7 quadrillion implementors */
	public default void receiveControl(Player player, CompoundTag data) { }
}
