package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Stand-in item for fluids in recipe displays (recipe icons, NEI/JEI), tinted with the fluid's color.
 * Fluid, amount and pressure are data components.
 */
public class ItemFluidIcon extends ItemFluidContainerBase {

	public ItemFluidIcon(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean showInCreative(FluidType type) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		if(getQuantity(stack) > 0) list.add(Component.literal(getQuantity(stack) + "mB"));
		if(getPressure(stack) > 0) {
			list.add(Component.literal(getPressure(stack) + "PU").withStyle(ChatFormatting.RED));
			list.add(Component.literal("Pressurized, use compressor!").withStyle(BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_RED));
		}

		List<String> info = new ArrayList<>();
		getFluid(stack).addInfo(info);
		for(String line : info) list.add(Component.literal(line));
	}

	public static ItemStack addQuantity(ItemStack stack, int i) {
		if(i > 0) stack.set(ModDataComponents.FLUID_FILL.get(), i);
		return stack;
	}

	public static ItemStack addPressure(ItemStack stack, int i) {
		if(i > 0) stack.set(ModDataComponents.FLUID_PRESSURE.get(), i);
		return stack;
	}

	public static ItemStack make(FluidStack stack) {
		return make(stack.type, stack.fill, stack.pressure);
	}

	public static ItemStack make(FluidType fluid, int i) {
		return make(fluid, i, 0);
	}

	public static ItemStack make(FluidType fluid, int i, int pressure) {
		return addPressure(addQuantity(withFluid(ModItems.fluid_icon.get(), fluid), i), pressure);
	}

	public static int getQuantity(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.FLUID_FILL.get(), 0);
	}

	public static int getPressure(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.FLUID_PRESSURE.get(), 0);
	}

	@Override
	public Component getName(ItemStack stack) {
		return fluidName(getFluid(stack));
	}

	public static int getColor(ItemStack stack) {
		int j = getFluid(stack).getColor();
		return j < 0 ? 0xFFFFFF : j;
	}
}
