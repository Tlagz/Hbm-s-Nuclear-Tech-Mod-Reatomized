package com.hbm.inventory.fluid.tank;

import java.util.List;

import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Containers from the FluidContainerRegistry (canisters, cells, buckets...) */
public class FluidLoaderStandard extends FluidLoadingHandler {

	@Override
	public boolean fillItem(List<ItemStack> slots, int in, int out, FluidTank tank) {

		if(tank.pressure != 0) return false;
		if(slots.get(in).isEmpty()) return true;

		FluidType type = tank.getTankType();
		ItemStack full = FluidContainerRegistry.getFullContainer(slots.get(in), type);

		if(full != null && tank.getFill() - FluidContainerRegistry.getFluidContent(full, type) >= 0) {

			Component name = slots.get(in).get(DataComponents.CUSTOM_NAME);
			ItemStack output = slots.get(out);

			if(output.isEmpty()) {

				tank.setFill(tank.getFill() - FluidContainerRegistry.getFluidContent(full, type));
				ItemStack result = full.copy();
				if(name != null) result.set(DataComponents.CUSTOM_NAME, name);
				slots.set(out, result);
				slots.get(in).shrink(1);

			} else if(FluidContainerRegistry.isSame(output, full) && output.getCount() < output.getMaxStackSize()) {

				tank.setFill(tank.getFill() - FluidContainerRegistry.getFluidContent(full, type));
				slots.get(in).shrink(1);
				output.grow(1);
			}
		}

		return false;
	}

	@Override
	public boolean emptyItem(List<ItemStack> slots, int in, int out, FluidTank tank) {

		if(slots.get(in).isEmpty())
			return true;

		FluidType type = tank.getTankType();
		int amount = FluidContainerRegistry.getFluidContent(slots.get(in), type);

		if(amount > 0 && tank.getFill() + amount <= tank.maxFluid) {

			ItemStack emptyContainer = FluidContainerRegistry.getEmptyContainer(slots.get(in));
			Component name = slots.get(in).get(DataComponents.CUSTOM_NAME);
			ItemStack output = slots.get(out);

			if(output.isEmpty()) {

				tank.setFill(tank.getFill() + amount);
				if(emptyContainer != null) {
					if(name != null) emptyContainer.set(DataComponents.CUSTOM_NAME, name);
					slots.set(out, emptyContainer);
				}
				slots.get(in).shrink(1);

			} else if(emptyContainer == null || (FluidContainerRegistry.isSame(output, emptyContainer) && output.getCount() < output.getMaxStackSize())) {

				tank.setFill(tank.getFill() + amount);
				slots.get(in).shrink(1);

				if(emptyContainer != null) output.grow(1);
			}

			return true;
		}

		return false;
	}
}
