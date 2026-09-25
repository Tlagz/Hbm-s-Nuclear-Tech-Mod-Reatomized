package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineArcWelder;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineArcWelder extends ContainerBase<TileEntityMachineArcWelder> {

	public ContainerMachineArcWelder(int id, Inventory playerInv, TileEntityMachineArcWelder tile) {
		super(ModMenus.ARC_WELDER.get(), id, tile);

		//Inputs
		this.addSlot(new Slot(tile, 0, 17, 36));
		this.addSlot(new Slot(tile, 1, 35, 36));
		this.addSlot(new Slot(tile, 2, 53, 36));
		//Output
		this.addSlot(new SlotTakeOnly(tile, 3, 107, 36));
		//Battery
		this.addSlot(new Slot(tile, 4, 152, 72));
		//Fluid ID
		this.addSlot(new Slot(tile, 5, 17, 63));
		//Upgrades
		this.addSlot(new SlotUpgrade(tile, 6, 89, 63));
		this.addSlot(new SlotUpgrade(tile, 7, 107, 63));

		this.addPlayerInventory(playerInv, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 7) {
				if(!this.mergeItemStack(stack, 8, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {

				if(rStack.getItem() instanceof IBatteryItem || rStack.is(ModItems.battery_creative.get())) {
					if(!this.mergeItemStack(stack, 4, 5, false)) return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
					if(!this.mergeItemStack(stack, 5, 6, false)) return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(stack, 6, 8, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(stack, 0, 3, false)) return ItemStack.EMPTY;
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
