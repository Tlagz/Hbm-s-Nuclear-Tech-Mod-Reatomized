package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class ContainerMachineRotaryFurnace extends ContainerBase<TileEntityMachineRotaryFurnace> {

	public ContainerMachineRotaryFurnace(int id, Inventory invPlayer, TileEntityMachineRotaryFurnace tile) {
		super(ModMenus.ROTARY_FURNACE.get(), id, tile);

		//Inputs
		this.addSlot(new Slot(tile, 0, 8, 18));
		this.addSlot(new Slot(tile, 1, 26, 18));
		this.addSlot(new Slot(tile, 2, 44, 18));
		//Fluid ID
		this.addSlot(new Slot(tile, 3, 8, 54));
		//Fuel
		this.addSlot(new Slot(tile, 4, 44, 54));

		this.addPlayerInventory(invPlayer, 8, 104);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 4) {
				if(!this.mergeItemStack(stack, 5, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(stack.getBurnTime(RecipeType.SMELTING) > 0) {
				if(!this.mergeItemStack(stack, 4, 5, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
				if(!this.mergeItemStack(stack, 3, 4, false)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 0, 3, false)) {
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
