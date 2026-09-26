package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityCrucible;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerCrucible extends ContainerBase<TileEntityCrucible> {

	public ContainerCrucible(int id, Inventory playerInv, TileEntityCrucible crucible) {
		super(ModMenus.CRUCIBLE.get(), id, crucible);

		//input
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlot(new SlotNonRetarded(crucible, j + i * 3 + 1, 107 + j * 18, 18 + i * 18));
			}
		}

		this.addPlayerInventory(playerInv, 8, 132);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 8) {
				if(!this.mergeItemStack(stack, 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				if(!tile.isItemSmeltable(stack)) return ItemStack.EMPTY;
				// one item per slot
				boolean moved = false;
				for(int i = 0; i < 9 && !stack.isEmpty(); i++) {
					Slot target = this.slots.get(i);
					if(!target.hasItem()) {
						target.set(stack.split(1));
						moved = true;
					}
				}
				if(!moved) return ItemStack.EMPTY;
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
