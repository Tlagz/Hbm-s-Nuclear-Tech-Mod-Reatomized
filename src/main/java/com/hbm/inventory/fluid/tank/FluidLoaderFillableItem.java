package com.hbm.inventory.fluid.tank;

import java.util.List;

import com.hbm.inventory.fluid.FluidType;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.world.item.ItemStack;

/** Items with their own fluid storage (jetpacks, fluid tools), IFillableItem. TODO armor mods (ArmorModHandler) */
public class FluidLoaderFillableItem extends FluidLoadingHandler {

	@Override
	public boolean fillItem(List<ItemStack> slots, int in, int out, FluidTank tank) {
		return fill(slots.get(in), tank);
	}

	public boolean fill(ItemStack stack, FluidTank tank) {

		if(tank.pressure != 0) return false;
		if(stack.isEmpty()) return false;

		FluidType type = tank.getTankType();

		if(!(stack.getItem() instanceof IFillableItem fillable)) return false;

		if(fillable.acceptsFluid(type, stack)) {
			tank.setFill(fillable.tryFill(type, tank.getFill(), stack));
		}

		return true;
	}

	@Override
	public boolean emptyItem(List<ItemStack> slots, int in, int out, FluidTank tank) {
		return empty(slots.get(in), tank);
	}

	public boolean empty(ItemStack stack, FluidTank tank) {

		FluidType type = tank.getTankType();

		if(!(stack.getItem() instanceof IFillableItem fillable)) return false;

		if(fillable.providesFluid(type, stack)) {
			tank.setFill(tank.getFill() + fillable.tryEmpty(type, tank.getMaxFill() - tank.getFill(), stack));
		}

		return tank.getFill() == tank.getMaxFill();
	}
}
