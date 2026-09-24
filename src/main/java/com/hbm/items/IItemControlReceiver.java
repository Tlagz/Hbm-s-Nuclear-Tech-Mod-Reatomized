package com.hbm.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Held items that accept settings from a client screen via NBTItemControlPacket */
public interface IItemControlReceiver {

	public void receiveControl(Player player, ItemStack stack, CompoundTag data);
}
