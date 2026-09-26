package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineGasCent;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineGasCent extends ContainerBase<TileEntityMachineGasCent> {

	public ContainerMachineGasCent(int id, Inventory invPlayer, TileEntityMachineGasCent tile) {
		super(ModMenus.GAS_CENT.get(), id, tile);

		//Output
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 2; j++) {
				this.addSlot(new SlotTakeOnly(tile, j + i * 2, 71 + j * 18, 53 + i * 18));
			}
		}

		//Battery
		this.addSlot(new Slot(tile, 4, 182, 71));
		//Fluid ID IO
		this.addSlot(new Slot(tile, 5, 91, 15));
		//Upgrade
		this.addSlot(new Slot(tile, 6, 69, 15));

		this.addPlayerInventory(invPlayer, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 6) {
				if(!this.mergeItemStack(stack, 7, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 4, 5, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
				if(!this.mergeItemStack(stack, 5, 6, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
				if(!this.mergeItemStack(stack, 6, 7, false)) return ItemStack.EMPTY;
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
