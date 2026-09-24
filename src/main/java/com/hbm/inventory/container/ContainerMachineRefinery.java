package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.oil.TileEntityMachineRefinery;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineRefinery extends ContainerBase<TileEntityMachineRefinery> {

	public ContainerMachineRefinery(int id, Inventory invPlayer, TileEntityMachineRefinery tedf) {
		super(ModMenus.REFINERY.get(), id, tedf);

		//Battery
		this.addSlot(new Slot(tedf, 0, 158, 108));
		//Canister Input
		this.addSlot(new Slot(tedf, 1, 12, 90));
		//Canister Output
		this.addSlot(new SlotTakeOnly(tedf, 2, 12, 108));
		//Heavy Oil Input
		this.addSlot(new Slot(tedf, 3, 64, 90));
		//Heavy Oil Output
		this.addSlot(new SlotTakeOnly(tedf, 4, 64, 108));
		//Naphtha Input
		this.addSlot(new Slot(tedf, 5, 82, 90));
		//Naphtha Output
		this.addSlot(new SlotTakeOnly(tedf, 6, 82, 108));
		//Light Oil Input
		this.addSlot(new Slot(tedf, 7, 100, 90));
		//Light Oil Output
		this.addSlot(new SlotTakeOnly(tedf, 8, 100, 108));
		//Petroleum Input
		this.addSlot(new Slot(tedf, 9, 118, 90));
		//Petroleum Output
		this.addSlot(new SlotTakeOnly(tedf, 10, 118, 108));
		//Sulfur Output
		this.addSlot(new SlotTakeOnly(tedf, 11, 38, 90));
		//Fluid ID
		this.addSlot(new Slot(tedf, 12, 38, 108));

		this.addPlayerInventory(invPlayer, 11, 158);
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
			} else if(!this.mergeItemStack(stack, 0, 1, false))
				if(!this.mergeItemStack(stack, 1, 2, false))
					if(!this.mergeItemStack(stack, 3, 4, false))
						if(!this.mergeItemStack(stack, 5, 6, false))
							if(!this.mergeItemStack(stack, 7, 8, false))
								if(!this.mergeItemStack(stack, 9, 10, false)) {
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
