package com.hbm.inventory.container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A filter slot showing a ghost copy of an item. It never takes or gives real items, the container's clicked
 * override sets the pattern and cycles its mode (see ContainerCraneExtractor).
 */
public class SlotPattern extends Slot {

	public SlotPattern(Container inv, int index, int x, int y) {
		super(inv, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
}
