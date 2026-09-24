package com.hbm.inventory.fluid.tank;

import java.util.List;

import net.minecraft.world.item.ItemStack;

public abstract class FluidLoadingHandler {

	/** Fills the item in slot "in" from the tank, results go to "out". True if this handler was responsible */
	public abstract boolean fillItem(List<ItemStack> slots, int in, int out, FluidTank tank);
	/** Empties the item in slot "in" into the tank, the empty container goes to "out". True if this handler was responsible */
	public abstract boolean emptyItem(List<ItemStack> slots, int in, int out, FluidTank tank);
}
