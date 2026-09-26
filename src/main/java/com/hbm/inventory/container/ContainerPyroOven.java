package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerPyroOven extends ContainerBase<TileEntityMachinePyroOven> {

	public ContainerPyroOven(int id, Inventory invPlayer, TileEntityMachinePyroOven tile) {
		super(ModMenus.PYRO_OVEN.get(), id, tile);

		//Battery
		this.addSlot(new Slot(tile, 0, 152, 72));
		//Input
		this.addSlot(new Slot(tile, 1, 35, 45));
		//Output
		this.addSlot(new SlotTakeOnly(tile, 2, 89, 45));
		//Fluid ID
		this.addSlot(new Slot(tile, 3, 8, 72));
		//Upgrades
		this.addSlot(new SlotUpgrade(tile, 4, 71, 72));
		this.addSlot(new SlotUpgrade(tile, 5, 89, 72));

		this.addPlayerInventory(invPlayer, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 5) {
				if(!this.mergeItemStack(stack, 6, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
				if(!this.mergeItemStack(stack, 3, 4, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
				if(!this.mergeItemStack(stack, 4, 6, false)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 1, 2, false)) {
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
