package com.hbm.inventory.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Slot that only accepts what the container accepts for that slot (the tile's isItemValidForSlot),
 * vanilla slots accept anything.
 */
public class SlotNonRetarded extends Slot {

	public SlotNonRetarded(Container inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return container.canPlaceItem(this.getContainerSlot(), stack);
	}
}
