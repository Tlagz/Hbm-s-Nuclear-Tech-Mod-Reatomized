package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.inventory.recipes.ArcFurnaceRecipes.ArcFurnaceRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemArcElectrode;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineArcFurnaceLarge extends ContainerBase<TileEntityMachineArcFurnaceLarge> {

	public ContainerMachineArcFurnaceLarge(int id, Inventory playerInv, TileEntityMachineArcFurnaceLarge tile) {
		super(ModMenus.ARC_FURNACE.get(), id, tile);

		//Electrodes
		for(int i = 0; i < 3; i++) this.addSlot(new SlotNonRetarded(tile, i, 62 + i * 18, 22));
		//Battery
		this.addSlot(new Slot(tile, 3, 8, 108));
		//Upgrade
		this.addSlot(new SlotUpgrade(tile, 4, 152, 108));
		//Inputs
		for(int i = 0; i < 4; i++) for(int j = 0; j < 5; j++) this.addSlot(new SlotArcFurnace(tile, 5 + j + i * 5, 44 + j * 18, 54 + i * 18));
		//IO
		for(int i = 0; i < 5; i++) this.addSlot(new SlotNonRetarded(tile, i + 25, 44 + i * 18, 129));

		this.addPlayerInventory(playerInv, 8, 174);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 29) {
				if(!this.mergeItemStack(stack, 30, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {

				if(rStack.getItem() instanceof IBatteryItem || rStack.is(ModItems.battery_creative.get())) {
					if(!this.mergeItemStack(stack, 3, 4, false)) return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof ItemArcElectrode) {
					if(!this.mergeItemStack(stack, 0, 3, false)) return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(stack, 4, 5, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(stack, 25, 30, false)) return ItemStack.EMPTY;
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

	/** Grid slot: only items with a recipe in the current mode, no more than the upgrade allows per slot */
	public static class SlotArcFurnace extends SlotNonRetarded {

		private final TileEntityMachineArcFurnaceLarge furnace;

		public SlotArcFurnace(TileEntityMachineArcFurnaceLarge furnace, int id, int x, int y) {
			super(furnace, id, x, y);
			this.furnace = furnace;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			if(furnace.liquidMode) return true;
			ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(stack, furnace.liquidMode, furnace.getLevel());
			if(recipe != null && recipe.solidOutput != null) {
				return recipe.solidOutput.getCount() * stack.getCount() <= recipe.solidOutput.getMaxStackSize() && stack.getCount() <= furnace.getMaxInputSize();
			}
			return false;
		}

		@Override
		public int getMaxStackSize() {
			return this.hasItem() ? furnace.getMaxInputSize() : 1;
		}
	}
}
