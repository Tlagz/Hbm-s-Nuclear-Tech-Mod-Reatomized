package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.function.Function;
import com.hbm.util.function.Function.FunctionLogarithmic;
import com.hbm.util.function.Function.FunctionSqrt;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** PWR fuel rods, one item per fuel (ModItems.pwr_fuel), hot and depleted rods are plain variants */
public class ItemPWRFuel extends ItemEnumMulti {

	public final EnumPWRFuel fuel;

	public ItemPWRFuel(Properties properties, String descriptionId, EnumPWRFuel fuel) {
		super(properties, descriptionId);
		this.fuel = fuel;
	}

	public static enum EnumPWRFuel {
		MEU(		05.0D,	new FunctionLogarithmic(20 * 30).withDiv(2_500)),
		HEU233(		07.5D,	new FunctionSqrt(25)),
		HEU235(		07.5D,	new FunctionSqrt(22.5)),
		MEN(		07.5D,	new FunctionLogarithmic(22.5 * 30).withDiv(2_500)),
		HEN237(		07.5D,	new FunctionSqrt(27.5)),
		MOX(		07.5D,	new FunctionLogarithmic(20 * 30).withDiv(2_500)),
		MEP(		07.5D,	new FunctionLogarithmic(22.5 * 30).withDiv(2_500)),
		HEP239(		10.0D,	new FunctionSqrt(22.5)),
		HEP241(		10.0D,	new FunctionSqrt(25)),
		MEA(		07.5D,	new FunctionLogarithmic(25 * 30).withDiv(2_500)),
		HEA242(		10.0D,	new FunctionSqrt(25)),
		HES326(		12.5D,	new FunctionSqrt(27.5)),
		HES327(		12.5D,	new FunctionSqrt(30)),
		BFB_AM_MIX(	2.5D,	new FunctionSqrt(15), 250_000_000),
		BFB_PU241(	2.5D,	new FunctionSqrt(15), 250_000_000);

		// the original never assigned the yield argument, every fuel yields 1B
		public double yield = 1_000_000_000;
		public double heatEmission;
		public Function function;

		private EnumPWRFuel(double heatEmission, Function function, double yield) {
			this.heatEmission = heatEmission;
			this.function = function;
		}

		private EnumPWRFuel(double heatEmission, Function function) {
			this(heatEmission, function, 1_000_000_000);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		String color = ChatFormatting.GOLD + "";
		String reset = ChatFormatting.RESET + "";

		list.add(Component.literal(color + "Heat per flux: " + reset + fuel.heatEmission + " TU"));
		list.add(Component.literal(color + "Reaction function: " + reset + fuel.function.getLabelForFuel()));
		list.add(Component.literal(color + "Fuel type: " + reset + fuel.function.getDangerFromFuel()));
	}
}
