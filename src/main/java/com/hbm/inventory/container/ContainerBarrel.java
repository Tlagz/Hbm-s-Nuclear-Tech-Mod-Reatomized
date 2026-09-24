package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerBarrel extends ContainerBase<TileEntityBarrel> {

	public ContainerBarrel(int id, Inventory invPlayer, TileEntityBarrel tedf) {
		super(ModMenus.BARREL.get(), id, tedf);

		//Fluid ID
		this.addSlot(new Slot(tedf, 0, 8, 17));
		this.addSlot(new SlotTakeOnly(tedf, 1, 8, 53));
		//Input IO
		this.addSlot(new Slot(tedf, 2, 53 - 18, 17));
		this.addSlot(new SlotTakeOnly(tedf, 3, 53 - 18, 53));
		//Output IO
		this.addSlot(new Slot(tedf, 4, 125, 17));
		this.addSlot(new SlotTakeOnly(tedf, 5, 125, 53));

		this.addPlayerInventory(invPlayer, 8, 84);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 5) {
				// the original started at 7, skipping the first player slot
				if(!this.mergeItemStack(stack, 6, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.mergeItemStack(stack, 0, 6, false)) {
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
}
