package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerFurnaceCombo extends ContainerBase<TileEntityFurnaceCombination> {

	public ContainerFurnaceCombo(int id, Inventory invPlayer, TileEntityFurnaceCombination furnace) {
		super(ModMenus.FURNACE_COMBINATION.get(), id, furnace);

		//input
		this.addSlot(new Slot(furnace, 0, 26, 36));
		//output
		this.addSlot(new SlotTakeOnly(furnace, 1, 89, 36));
		//fluid containers
		this.addSlot(new Slot(furnace, 2, 136, 18));
		this.addSlot(new SlotTakeOnly(furnace, 3, 136, 54));

		this.addPlayerInventory(invPlayer, 8, 104);
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
				slot.onQuickCraft(stack, rStack);
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
