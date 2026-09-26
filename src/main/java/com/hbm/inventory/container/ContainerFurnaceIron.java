package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityFurnaceIron;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerFurnaceIron extends ContainerBase<TileEntityFurnaceIron> {

	public ContainerFurnaceIron(int id, Inventory invPlayer, TileEntityFurnaceIron furnace) {
		super(ModMenus.FURNACE_IRON.get(), id, furnace);

		//input
		this.addSlot(new Slot(furnace, 0, 53, 17));
		//fuel
		this.addSlot(new Slot(furnace, 1, 53, 53));
		this.addSlot(new Slot(furnace, 2, 71, 53));
		//output
		this.addSlot(new SlotTakeOnly(furnace, 3, 125, 35));
		//upgrade
		this.addSlot(new SlotUpgrade(furnace, 4, 17, 35));

		this.addPlayerInventory(invPlayer, 8, 84);
	}

	/** Shift clicking puts things wherever they are allowed (the tile's slot rules) */
	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 4) {
				if(!this.mergeItemStack(stack, 5, this.slots.size(), true)) return ItemStack.EMPTY;
				slot.onQuickCraft(stack, rStack);
			} else if(tile.isItemValidForSlot(0, stack)) {
				if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
			} else if(tile.isItemValidForSlot(1, stack)) {
				if(!this.mergeItemStack(stack, 1, 3, false)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 4, 5, false)) {
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
