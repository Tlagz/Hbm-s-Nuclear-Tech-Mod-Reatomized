package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineSolderingStation;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineSolderingStation extends ContainerBase<TileEntityMachineSolderingStation> {

	public ContainerMachineSolderingStation(int id, Inventory playerInv, TileEntityMachineSolderingStation tile) {
		super(ModMenus.SOLDERING_STATION.get(), id, tile);

		//Inputs: toppings, boards, solder
		this.addSlots(tile, 0, 17, 18, 2, 3);
		//Output
		this.addSlot(new SlotTakeOnly(tile, 6, 107, 27));
		//Battery
		this.addSlot(new Slot(tile, 7, 152, 72));
		//Fluid ID
		this.addSlot(new Slot(tile, 8, 17, 63));
		//Upgrades
		this.addSlot(new SlotUpgrade(tile, 9, 89, 63));
		this.addSlot(new SlotUpgrade(tile, 10, 107, 63));

		this.addPlayerInventory(playerInv, 8, 122);
	}

	private static boolean matchesAny(Iterable<AStack> ingredients, ItemStack stack) {
		for(AStack t : ingredients) if(t.matchesRecipe(stack, true)) return true;
		return false;
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 10) {
				if(!this.mergeItemStack(stack, 11, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 7, 8, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
				if(!this.mergeItemStack(stack, 8, 9, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
				if(!this.mergeItemStack(stack, 9, 11, false)) return ItemStack.EMPTY;
			} else if(matchesAny(SolderingRecipes.toppings, stack)) {
				if(!this.mergeItemStack(stack, 0, 3, false)) return ItemStack.EMPTY;
			} else if(matchesAny(SolderingRecipes.pcb, stack)) {
				if(!this.mergeItemStack(stack, 3, 5, false)) return ItemStack.EMPTY;
			} else if(matchesAny(SolderingRecipes.solder, stack)) {
				if(!this.mergeItemStack(stack, 5, 6, false)) return ItemStack.EMPTY;
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
