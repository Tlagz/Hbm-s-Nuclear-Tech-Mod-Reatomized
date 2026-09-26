package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityHeaterOilburner;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerOilburner extends ContainerBase<TileEntityHeaterOilburner> {

	public ContainerOilburner(int id, Inventory invPlayer, TileEntityHeaterOilburner heater) {
		super(ModMenus.OILBURNER.get(), id, heater);

		//In
		this.addSlot(new Slot(heater, 0, 26, 17));
		//Out
		this.addSlot(new SlotTakeOnly(heater, 1, 26, 53));
		//Fluid ID
		this.addSlot(new Slot(heater, 2, 44, 71));

		this.addPlayerInventory(invPlayer, 8, 121);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack originalStack = slot.getItem();
			stack = originalStack.copy();

			if(index <= 2) {
				if(!this.mergeItemStack(originalStack, 3, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {

				if(stack.getItem() instanceof IItemFluidIdentifier) {
					if(!this.mergeItemStack(originalStack, 2, 3, false)) {
						return ItemStack.EMPTY;
					}
				} else {
					if(!this.mergeItemStack(originalStack, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if(originalStack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return stack;
	}
}
