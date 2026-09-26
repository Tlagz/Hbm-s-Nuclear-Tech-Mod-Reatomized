package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineGasFlare extends ContainerBase<TileEntityMachineGasFlare> {

	public ContainerMachineGasFlare(int id, Inventory invPlayer, TileEntityMachineGasFlare flare) {
		super(ModMenus.GAS_FLARE.get(), id, flare);

		//Battery
		this.addSlot(new Slot(flare, 0, 143, 71));
		//Fluid in
		this.addSlot(new Slot(flare, 1, 17, 17));
		//Fluid out
		this.addSlot(new SlotTakeOnly(flare, 2, 17, 53));
		//Fluid ID
		this.addSlot(new Slot(flare, 3, 35, 71));
		//Upgrades
		this.addSlot(new SlotUpgrade(flare, 4, 80, 71));
		this.addSlot(new SlotUpgrade(flare, 5, 98, 71));

		this.addPlayerInventory(invPlayer, 8, 121);
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
			} else {
				if(rStack.getItem() instanceof IBatteryItem) {
					if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
					if(!this.mergeItemStack(stack, 3, 4, false)) return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(stack, 4, 6, false)) return ItemStack.EMPTY;
				} else if(!this.mergeItemStack(stack, 1, 2, false)) {
					return ItemStack.EMPTY;
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
