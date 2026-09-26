package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityFurnaceSteel;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerFurnaceSteel extends ContainerBase<TileEntityFurnaceSteel> {

	public ContainerFurnaceSteel(int id, Inventory invPlayer, TileEntityFurnaceSteel furnace) {
		super(ModMenus.FURNACE_STEEL.get(), id, furnace);

		//input
		this.addSlot(new Slot(furnace, 0, 35, 17));
		this.addSlot(new Slot(furnace, 1, 35, 35));
		this.addSlot(new Slot(furnace, 2, 35, 53));
		//output
		this.addSlot(new SlotTakeOnly(furnace, 3, 125, 17));
		this.addSlot(new SlotTakeOnly(furnace, 4, 125, 35));
		this.addSlot(new SlotTakeOnly(furnace, 5, 125, 53));

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
				if(!this.mergeItemStack(stack, 6, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(stack, rStack);
			} else if(!this.mergeItemStack(stack, 0, 3, false)) {
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
