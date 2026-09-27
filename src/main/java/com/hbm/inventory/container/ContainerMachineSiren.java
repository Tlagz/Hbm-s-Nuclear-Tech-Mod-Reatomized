package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineSiren;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineSiren extends ContainerBase<TileEntityMachineSiren> {

	public ContainerMachineSiren(int id, Inventory invPlayer, TileEntityMachineSiren tedf) {
		super(ModMenus.SIREN.get(), id, tedf);

		// the tile rejects automation (like the original), but the player may put anything in by hand
		this.addSlot(new Slot(tedf, 0, 8, 35) {
			@Override public boolean mayPlace(ItemStack stack) { return true; }
		});

		this.addPlayerInventory(invPlayer, 8, 84);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index == 0) {
				if(!this.mergeItemStack(var5, 1, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(var5, 0, 1, false)) {
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
