package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityElectrolyser;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerElectrolyserFluid extends ContainerBase<TileEntityElectrolyser> {

	public ContainerElectrolyserFluid(int id, Inventory invPlayer, TileEntityElectrolyser tile) {
		super(ModMenus.ELECTROLYSER_FLUID.get(), id, tile);

		//Battery
		this.addSlot(new Slot(tile, 0, 186, 109));
		//Upgrades
		this.addSlot(new SlotUpgrade(tile, 1, 186, 140));
		this.addSlot(new SlotUpgrade(tile, 2, 186, 158));
		//Fluid ID
		this.addSlot(new Slot(tile, 3, 6, 18));
		this.addSlot(new SlotTakeOnly(tile, 4, 6, 54));
		//Input
		this.addSlot(new Slot(tile, 5, 24, 18));
		this.addSlot(new SlotTakeOnly(tile, 6, 24, 54));
		//Output
		this.addSlot(new Slot(tile, 7, 78, 18));
		this.addSlot(new SlotTakeOnly(tile, 8, 78, 54));
		this.addSlot(new Slot(tile, 9, 134, 18));
		this.addSlot(new SlotTakeOnly(tile, 10, 134, 54));
		//Byproducts
		this.addSlot(new SlotTakeOnly(tile, 11, 154, 18));
		this.addSlot(new SlotTakeOnly(tile, 12, 154, 36));
		this.addSlot(new SlotTakeOnly(tile, 13, 154, 54));

		this.addPlayerInventory(invPlayer, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 13) {
				if(!this.mergeItemStack(stack, 14, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
				if(!this.mergeItemStack(stack, 1, 3, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
				if(!this.mergeItemStack(stack, 3, 4, false)) return ItemStack.EMPTY;
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
