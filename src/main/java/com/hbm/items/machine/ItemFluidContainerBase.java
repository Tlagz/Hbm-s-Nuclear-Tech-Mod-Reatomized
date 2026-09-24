package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ISubItems;
import com.hbm.items.ModDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Common base of the items that stored a fluid ID as their damage value in the original (canisters, gas tanks,
 * universal tanks, barrels). The ID is now the FLUID_TYPE data component, see {@link #getFluid(ItemStack)}.
 */
public abstract class ItemFluidContainerBase extends Item implements ISubItems {

	public ItemFluidContainerBase(Properties properties) {
		super(properties);
	}

	/** The original's Fluids.fromID(stack.getItemDamage()), NONE if the stack has no fluid */
	public static FluidType getFluid(ItemStack stack) {
		Integer id = stack.get(ModDataComponents.FLUID_TYPE);
		return id == null ? Fluids.NONE : Fluids.fromID(id);
	}

	/** The original's new ItemStack(item, 1, type.getID()) */
	public static ItemStack withFluid(ItemLike item, FluidType type) {
		ItemStack stack = new ItemStack(item);
		stack.set(ModDataComponents.FLUID_TYPE, type.getID());
		return stack;
	}

	/** The fluid's name as a component, translated on the client (conditional names may be literal overrides) */
	public static Component fluidName(FluidType type) {
		return Component.translatable(type.getConditionalName());
	}

	/** Which fluids get a creative tab entry */
	protected abstract boolean showInCreative(FluidType type);

	@Override
	public List<ItemStack> getSubItems() {
		List<ItemStack> list = new ArrayList<>();
		FluidType[] order = Fluids.getInNiceOrder();
		for(int i = 1; i < order.length; ++i) {
			if(showInCreative(order[i])) list.add(withFluid(this, order[i]));
		}
		return list;
	}
}
