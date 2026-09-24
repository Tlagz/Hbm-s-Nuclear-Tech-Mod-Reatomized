package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityFireboxBase;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** The firebox door opens while the menu is open (startOpen/stopOpen, the original's openInventory) */
public class ContainerFirebox extends ContainerBase<TileEntityFireboxBase> {

	public ContainerFirebox(int id, Inventory invPlayer, TileEntityFireboxBase furnace) {
		super(ModMenus.FIREBOX.get(), id, furnace);
		furnace.startOpen(invPlayer.player);

		this.addSlot(new Slot(furnace, 0, 44, 27));
		this.addSlot(new Slot(furnace, 1, 62, 27));

		this.addPlayerInventory(invPlayer, 8, 86);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 1) {
				if(!this.mergeItemStack(stack, 2, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.mergeItemStack(stack, 0, 2, false)) {
				return ItemStack.EMPTY;
			}

			if(stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return rStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.tile.stopOpen(player);
	}
}
