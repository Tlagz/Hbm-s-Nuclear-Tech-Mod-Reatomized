package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerLiquefactor extends ContainerBase<TileEntityMachineLiquefactor> {

	public ContainerLiquefactor(int id, Inventory playerInv, TileEntityMachineLiquefactor tile) {
		super(ModMenus.LIQUEFACTOR.get(), id, tile);

		//Input
		this.addSlot(new Slot(tile, 0, 35, 54));
		//Battery
		this.addSlot(new Slot(tile, 1, 134, 72));
		//Upgrades
		this.addSlot(new SlotUpgrade(tile, 2, 98, 36));
		this.addSlot(new SlotUpgrade(tile, 3, 98, 54));

		this.addPlayerInventory(playerInv, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 3) {
				if(!this.mergeItemStack(stack, 4, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 1, 2, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
				if(!this.mergeItemStack(stack, 2, 4, false)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 0, 1, false)) {
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
