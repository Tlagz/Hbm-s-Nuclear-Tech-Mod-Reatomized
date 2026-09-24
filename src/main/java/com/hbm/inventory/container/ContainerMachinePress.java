package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.ItemStamp;
import com.hbm.tileentity.machine.TileEntityMachinePress;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class ContainerMachinePress extends ContainerBase<TileEntityMachinePress> {

	public ContainerMachinePress(int id, Inventory invPlayer, TileEntityMachinePress tedf) {
		super(ModMenus.PRESS.get(), id, tedf);

		// Coal
		this.addSlot(new Slot(tedf, 0, 26, 53));
		// Stamp
		this.addSlot(new Slot(tedf, 1, 80, 17));
		// Input
		this.addSlot(new Slot(tedf, 2, 80, 53));
		// Output
		this.addSlot(new SlotTakeOnly(tedf, 3, 140, 35));
		// Stamp storage
		for(int i = 0; i < 9; i++) {
			this.addSlot(new Slot(tedf, 4 + i, 8 + i * 18, 84));
		}

		this.addPlayerInventory(invPlayer, 8, 132);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 12) {
				if(!this.mergeItemStack(stack, 13, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				if(stack.getBurnTime(RecipeType.SMELTING) > 0) {
					if(!this.mergeItemStack(stack, 0, 1, false)) {
						if(!this.mergeItemStack(stack, 4, 13, false)) {
							return ItemStack.EMPTY;
						}
					}
				} else if(rStack.getItem() instanceof ItemStamp) {
					if(!this.mergeItemStack(stack, 1, 2, false)) {
						if(!this.mergeItemStack(stack, 4, 13, false)) {
							return ItemStack.EMPTY;
						}
					}
				} else {
					if(!this.mergeItemStack(stack, 2, 3, false)) {
						if(!this.mergeItemStack(stack, 4, 13, false)) {
							return ItemStack.EMPTY;
						}
					}
				}
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
