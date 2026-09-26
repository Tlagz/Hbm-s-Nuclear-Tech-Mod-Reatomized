package com.hbm.inventory.recipes;

import java.util.HashMap;
import java.util.Locale;
import java.util.function.Supplier;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.world.item.ItemStack;

/**
 * Gas centrifuge: real fluids are converted into pseudo fluids (enrichment stages) inside the machine, each stage
 * consumes some of the pseudo fluid for items and passes the rest on as the next stage.
 *
 * TODO the NEI/JEI recipe table
 */
public class GasCentrifugeRecipes {

	public static class PseudoFluidType {

		public static HashMap<String, PseudoFluidType> types = new HashMap<>();

		public static PseudoFluidType NONE		= new PseudoFluidType("NONE",		0,		0,		null,		false);

		public static PseudoFluidType HEUF6		= new PseudoFluidType("HEUF6",		300,	0,		NONE,		true,	() -> new ItemStack(ModItems.nugget_u238.get(), 2), () -> new ItemStack(ModItems.nugget_u235.get(), 1), () -> new ItemStack(ModItems.fluorite.get(), 1));
		public static PseudoFluidType MEUF6		= new PseudoFluidType("MEUF6",		200,	100,	HEUF6,		false,	() -> new ItemStack(ModItems.nugget_u238.get(), 1));
		public static PseudoFluidType LEUF6		= new PseudoFluidType("LEUF6",		300,	200,	MEUF6,		false,	() -> new ItemStack(ModItems.nugget_u238.get(), 1), () -> new ItemStack(ModItems.fluorite.get(), 1));
		public static PseudoFluidType NUF6		= new PseudoFluidType("NUF6",		400,	300,	LEUF6,		false,	() -> new ItemStack(ModItems.nugget_u238.get(), 1));

		public static PseudoFluidType PF6		= new PseudoFluidType("PF6",		300,	0,		NONE,		false,	() -> new ItemStack(ModItems.nugget_pu238.get(), 1), () -> new ItemStack(ModItems.nugget_pu_mix.get(), 2), () -> new ItemStack(ModItems.fluorite.get(), 1));

		public static PseudoFluidType MUD_HEAVY	= new PseudoFluidType("MUD_HEAVY",	500,	0,		NONE,		false,	() -> new ItemStack(ModItems.powder_iron.get(), 1), () -> new ItemStack(ModItems.dust.get(), 1), () -> new ItemStack(ModItems.nuclear_waste_tiny.get(), 1));
		public static PseudoFluidType MUD		= new PseudoFluidType("MUD",		1000,	500,	MUD_HEAVY,	false,	() -> new ItemStack(ModItems.powder_lead.get(), 1), () -> new ItemStack(ModItems.dust.get(), 1));

		public String name;
		int fluidConsumed;
		int fluidProduced;
		PseudoFluidType outputFluid;
		boolean isHighSpeed;
		/** Items are registered after this class loads, so the outputs are made on demand */
		Supplier<ItemStack>[] output;

		@SafeVarargs
		PseudoFluidType(String name, int fluidConsumed, int fluidProduced, PseudoFluidType outputFluid, boolean isHighSpeed, Supplier<ItemStack>... output) {
			this.name = name;
			this.fluidConsumed = fluidConsumed;
			this.fluidProduced = fluidProduced;
			this.outputFluid = outputFluid;
			this.isHighSpeed = isHighSpeed;
			this.output = output;
			types.put(name, this);
		}

		public int getFluidConsumed() {				return this.fluidConsumed; }
		public int getFluidProduced() {				return this.fluidProduced; }
		public PseudoFluidType getOutputType() {	return this.outputFluid; }
		public boolean getIfHighSpeed() {			return this.isHighSpeed; }
		public String getName() {					return I18nUtil.resolveKey("hbmpseudofluid.".concat(this.name.toLowerCase(Locale.US))); }

		/** Fresh copies of the outputs, null if the stage makes nothing */
		public ItemStack[] getOutput() {
			if(output == null || output.length == 0) return null;
			ItemStack[] stacks = new ItemStack[output.length];
			for(int i = 0; i < output.length; i++) stacks[i] = output[i].get();
			return stacks;
		}
	}

	public static HashMap<FluidType, PseudoFluidType> fluidConversions = new HashMap<>();

	public static void register() {
		fluidConversions.clear();
		fluidConversions.put(Fluids.UF6, PseudoFluidType.NUF6);
		fluidConversions.put(Fluids.PUF6, PseudoFluidType.PF6);
		fluidConversions.put(Fluids.WATZ, PseudoFluidType.MUD);
	}
}
