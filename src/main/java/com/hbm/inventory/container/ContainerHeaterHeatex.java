package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityHeaterHeatex;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerHeaterHeatex extends ContainerBase<TileEntityHeaterHeatex> {

	public ContainerHeaterHeatex(int id, Inventory invPlayer, TileEntityHeaterHeatex heater) {
		super(ModMenus.HEATER_HEATEX.get(), id, heater);

		//Fluid ID
		this.addSlot(new Slot(heater, 0, 80, 72));

		this.addPlayerInventory(invPlayer, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack originalStack = slot.getItem();
			stack = originalStack.copy();

			if(index <= 0) {
				if(!this.mergeItemStack(originalStack, 1, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.mergeItemStack(originalStack, 0, 1, false)) {
				return ItemStack.EMPTY;
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
