package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.oil.TileEntityOilDrillBase;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineOilWell extends ContainerBase<TileEntityOilDrillBase> {

	public ContainerMachineOilWell(int id, Inventory invPlayer, TileEntityOilDrillBase tedf) {
		super(ModMenus.OIL_WELL.get(), id, tedf);

		// Battery
		this.addSlot(new Slot(tedf, 0, 8, 58));
		// Canister Input
		this.addSlot(new Slot(tedf, 1, 94, 22));
		// Canister Output
		this.addSlot(new SlotTakeOnly(tedf, 2, 94, 58));
		// Gas Input
		this.addSlot(new Slot(tedf, 3, 130, 22));
		// Gas Output
		this.addSlot(new SlotTakeOnly(tedf, 4, 130, 58));
		//Upgrades
		this.addSlot(new Slot(tedf, 5, 156, 36));
		this.addSlot(new Slot(tedf, 6, 156, 54));

		this.addPlayerInventory(invPlayer, 12, 108);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			// the original assumed 8 machine slots, the container only has 7
			if(index <= 6) {
				if(!this.mergeItemStack(stack, 7, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {

				if(stack.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(stack, 5, 7, true)) {
						return ItemStack.EMPTY;
					}
				} else {
					if(!this.mergeItemStack(stack, 0, 2, false)) {
						if(!this.mergeItemStack(stack, 3, 4, false)) {
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
