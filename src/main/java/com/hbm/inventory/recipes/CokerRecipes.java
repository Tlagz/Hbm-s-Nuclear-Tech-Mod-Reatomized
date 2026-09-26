package com.hbm.inventory.recipes;

import static com.hbm.inventory.fluid.Fluids.*;

import java.util.HashMap;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.items.ItemEnums.EnumCokeType;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple.Triplet;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Coker recipes: an amount of a fluid into an item and a byproduct fluid. Fuels turn into petroleum coke, the amount
 * calculated from their heat or combustion energy, so this has to run after the fuel values were recalculated.
 *
 * TODO JSON config, JEI
 */
public class CokerRecipes {

	public static HashMap<FluidType, Triplet<Integer, ItemStack, FluidStack>> recipes = new HashMap<>();

	public static void registerDefaults() {
		recipes.clear();

		registerAuto(HEAVYOIL,				OIL_COKER);
		registerAuto(HEAVYOIL_VACUUM,		REFORMATE);
		registerAuto(COALCREOSOTE,			NAPHTHA_COKER);
		registerAuto(SMEAR,					OIL_COKER);
		registerAuto(HEATINGOIL,			OIL_COKER);
		registerAuto(HEATINGOIL_VACUUM,		OIL_COKER);
		registerAuto(RECLAIMED,				NAPHTHA_COKER);
		registerAuto(NAPHTHA,				NAPHTHA_COKER);
		registerAuto(NAPHTHA_DS,			NAPHTHA_COKER);
		registerAuto(NAPHTHA_CRACK,			NAPHTHA_COKER);
		registerAuto(DIESEL,				NAPHTHA_COKER);
		registerAuto(DIESEL_REFORM,			NAPHTHA_COKER);
		registerAuto(DIESEL_CRACK,			GAS_COKER);
		registerAuto(DIESEL_CRACK_REFORM,	GAS_COKER);
		registerAuto(LIGHTOIL,				GAS_COKER);
		registerAuto(LIGHTOIL_DS,			GAS_COKER);
		registerAuto(LIGHTOIL_CRACK,		GAS_COKER);
		registerAuto(LIGHTOIL_VACUUM,		GAS_COKER);
		registerAuto(BIOFUEL,				GAS_COKER);
		registerAuto(AROMATICS,				GAS_COKER);
		registerAuto(REFORMATE,				GAS_COKER);
		registerAuto(XYLENE,				GAS_COKER);
		registerAuto(FISHOIL,				MERCURY);
		registerAuto(SUNFLOWEROIL,			GAS_COKER);

		registerSFAuto(WOODOIL, 340_000L, new ItemStack(Items.CHARCOAL), GAS_COKER);

		registerRecipe(WATZ, 4_000, new ItemStack(ModItems.ingot_mud.get(), 4), null);
		registerRecipe(REDMUD, 450, new ItemStack(Items.IRON_INGOT, 1), new FluidStack(MERCURY, 50));
		registerRecipe(BITUMEN, 16_000, petCoke(), new FluidStack(OIL_COKER, 1_600));
		registerRecipe(LUBRICANT, 12_000, petCoke(), new FluidStack(OIL_COKER, 1_200));
		registerRecipe(CALCIUM_SOLUTION, 125, new ItemStack(ModItems.powder_calcium.get()), new FluidStack(SPENTSTEAM, 100));
		//only cokable gas to extract sulfur content
		registerRecipe(SOURGAS, 1_000, new ItemStack(ModItems.sulfur.get()), new FluidStack(GAS_COKER, 150));
		registerRecipe(SLOP, 1000, new ItemStack(ModItems.powder_limestone.get()), new FluidStack(COLLOID, 250));
		registerRecipe(VITRIOL, 4000, new ItemStack(ModItems.powder_iron.get()), new FluidStack(SULFURIC_ACID, 500));

		MainRegistry.logger.info("Coker recipes: " + recipes.size());
	}

	private static ItemStack petCoke() {
		return new ItemStack(ModItems.coke.get(EnumCokeType.PETROLEUM).get());
	}

	public static void registerAuto(FluidType fluid, FluidType type) {
		registerSFAuto(fluid, 820_000L, petCoke(), type); //3200 burntime * 1.25 burntime bonus * 200 TU/t + 20000TU per operation
	}

	private static void registerSFAuto(FluidType fluid, long tuPerSF, ItemStack fuel, FluidType type) {
		long tuFlammable = fluid.hasTrait(FT_Flammable.class) ? fluid.getTrait(FT_Flammable.class).getHeatEnergy() : 0;
		long tuCombustible = fluid.hasTrait(FT_Combustible.class) ? fluid.getTrait(FT_Combustible.class).getCombustionEnergy() : 0;

		long tuPerBucket = Math.max(tuFlammable, tuCombustible);
		if(tuPerBucket <= 0) return;

		int mB = (int) (tuPerSF * 1000L / tuPerBucket);

		if(mB > 10_000) mB -= (mB % 1000);
		else if(mB > 1_000) mB -= (mB % 100);
		else if(mB > 100) mB -= (mB % 10);

		FluidStack byproduct = type == null ? null : new FluidStack(type, Math.max(10, mB / 10));

		registerRecipe(fluid, mB, fuel, byproduct);
	}

	private static void registerRecipe(FluidType type, int quantity, ItemStack output, FluidStack byproduct) {
		recipes.put(type, new Triplet<>(quantity, output, byproduct));
	}

	public static Triplet<Integer, ItemStack, FluidStack> getOutput(FluidType type) {
		return recipes.get(type);
	}
}
