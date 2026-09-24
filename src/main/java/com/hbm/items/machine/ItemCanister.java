package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids.CD_Canister;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Canister for liquid fuels, "Canister: [fluid]". Overlay tinted with the canister color of the fluid. */
public class ItemCanister extends ItemFluidContainerBase {

	public ItemCanister(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean showInCreative(FluidType type) {
		return type.getContainer(CD_Canister.class) != null;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack)).append(" ").append(fluidName(getFluid(stack)));
	}

	public static int getColor(ItemStack stack, int tintIndex) {
		if(tintIndex == 0) return 0xFFFFFF;
		CD_Canister canister = getFluid(stack).getContainer(CD_Canister.class);
		return canister == null ? 0xFFFFFF : canister.color;
	}
}
