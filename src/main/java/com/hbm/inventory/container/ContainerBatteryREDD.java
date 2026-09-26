package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.storage.TileEntityBatteryREDD;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerBatteryREDD extends ContainerBase<TileEntityBatteryREDD> {

	public ContainerBatteryREDD(int id, Inventory invPlayer, TileEntityBatteryREDD tile) {
		super(ModMenus.BATTERY_REDD.get(), id, tile);

		//Discharge, charge
		this.addSlot(new Slot(tile, 0, 26, 53));
		this.addSlot(new Slot(tile, 1, 80, 53));

		this.addPlayerInventory(invPlayer, 8, 99);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 1) {
				if(!this.mergeItemStack(stack, 2, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(stack.getItem() instanceof IBatteryItem battery) {
				// full batteries go into the discharge slot, the rest gets charged
				int target = battery.getCharge(stack) > 0 ? 0 : 1;
				if(!this.mergeItemStack(stack, target, target + 1, false)) return ItemStack.EMPTY;
			} else {
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
