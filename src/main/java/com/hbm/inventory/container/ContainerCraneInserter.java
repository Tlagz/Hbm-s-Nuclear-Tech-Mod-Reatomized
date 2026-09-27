package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.network.TileEntityCraneInserter;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerCraneInserter extends ContainerBase<TileEntityCraneInserter> {

	public ContainerCraneInserter(int id, Inventory invPlayer, TileEntityCraneInserter inserter) {
		super(ModMenus.CRANE_INSERTER.get(), id, inserter);
		addSlots(inserter, 0, 8, 17, 3, 7);
		this.addPlayerInventory(invPlayer, 8, 103);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 20) {
				if(!this.mergeItemStack(var5, 21, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(var5, 0, 21, false)) {
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
