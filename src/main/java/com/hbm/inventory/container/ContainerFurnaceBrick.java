package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityFurnaceBrick;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerFurnaceBrick extends ContainerBase<TileEntityFurnaceBrick> {

	public ContainerFurnaceBrick(int id, Inventory invPlayer, TileEntityFurnaceBrick tile) {
		super(ModMenus.FURNACE_BRICK.get(), id, tile);

		//input
		this.addSlot(new Slot(tile, 0, 62, 35));
		//fuel
		this.addSlot(new Slot(tile, 1, 35, 17));
		//output
		this.addSlot(new SlotTakeOnly(tile, 2, 116, 35));
		//ash
		this.addSlot(new SlotTakeOnly(tile, 3, 35, 53));

		this.addPlayerInventory(invPlayer, 8, 84);
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
			} else if(tile.isItemValidForSlot(1, stack)) {
				if(!this.mergeItemStack(stack, 1, 2, false) && !this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
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
