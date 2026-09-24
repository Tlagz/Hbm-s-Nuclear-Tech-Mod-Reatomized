package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids.CD_Gastank;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Gas tank, "Gas Tank: [fluid]". Three layers: base, bottle (tint 1) and label (tint 2). */
public class ItemGasTank extends ItemFluidContainerBase {

	public ItemGasTank(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean showInCreative(FluidType type) {
		return type.getContainer(CD_Gastank.class) != null;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack)).append(" ").append(fluidName(getFluid(stack)));
	}

	public static int getColor(ItemStack stack, int tintIndex) {
		if(tintIndex == 0) return 0xFFFFFF;
		CD_Gastank tank = getFluid(stack).getContainer(CD_Gastank.class);
		if(tank == null) return 0xFFFFFF;
		return tintIndex == 1 ? tank.bottleColor : tank.labelColor;
	}
}
