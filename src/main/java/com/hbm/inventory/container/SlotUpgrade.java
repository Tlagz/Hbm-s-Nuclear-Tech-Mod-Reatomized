package com.hbm.inventory.container;

import com.hbm.items.machine.ItemMachineUpgrade;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Machine upgrade slot, only takes upgrades */
public class SlotUpgrade extends Slot {

	public SlotUpgrade(Container inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() instanceof ItemMachineUpgrade;
	}
}
