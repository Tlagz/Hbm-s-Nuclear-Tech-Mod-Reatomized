package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ModItems;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Universal fluid tank, hazardous material tank and fluid barrel: "Universal Fluid Tank: %s".
 * Rendered as base texture + overlay tinted with the fluid color (tint index 1).
 */
public class ItemFluidTank extends ItemFluidContainerBase {

	public ItemFluidTank(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean showInCreative(FluidType type) {
		if(type.hasNoContainer()) return false;
		// lead-only fluids only exist in the lead tank
		return !type.needsLeadContainer() || this == ModItems.fluid_tank_lead_full.get();
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack), fluidName(getFluid(stack)));
	}

	/** Color for the tint layers, the original's getColorFromItemStack */
	public static int getColor(ItemStack stack, int tintIndex) {
		if(tintIndex == 0) return 0xFFFFFF;
		int j = getFluid(stack).getColor();
		return j < 0 ? 0xFFFFFF : j;
	}
}
