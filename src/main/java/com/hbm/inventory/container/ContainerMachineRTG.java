package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineRTG;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineRTG extends ContainerBase<TileEntityMachineRTG> {

	public ContainerMachineRTG(int id, Inventory invPlayer, TileEntityMachineRTG tedf) {
		super(ModMenus.RTG.get(), id, tedf);

		this.addSlots(tedf, 0, 16, 18, 3, 5);

		this.addPlayerInventory(invPlayer, 8, 106);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 14) {
				if(!this.mergeItemStack(var5, 15, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(var5, 0, 15, false)) {
				return ItemStack.EMPTY;
			}

			if(var5.isEmpty()) {
				var4.setByPlayer(ItemStack.EMPTY);
			} else {
				var4.setChanged();
			}
		}

		return var3;
	}
}
