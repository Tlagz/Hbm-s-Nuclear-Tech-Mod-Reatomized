package com.hbm.inventory.recipes;

import java.util.HashMap;
import java.util.Map;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.util.Tuple.Pair;

/**
 * Fractioning tower recipes: 100mB of the input into two fractions.
 * TODO JSON config (SerializableRecipe), JEI
 */
public class FractionRecipes {

	public static Map<FluidType, Pair<FluidStack, FluidStack>> fractions = new HashMap<>();

	public static void registerDefaults() {
		fractions.clear();
		put(Fluids.HEAVYOIL,			Fluids.BITUMEN,					30,		Fluids.SMEAR,				70);
		put(Fluids.HEAVYOIL_VACUUM,		Fluids.SMEAR,					40,		Fluids.HEATINGOIL_VACUUM,	60);
		put(Fluids.SMEAR,				Fluids.HEATINGOIL,				60,		Fluids.LUBRICANT,			40);
		put(Fluids.NAPHTHA,				Fluids.HEATINGOIL,				40,		Fluids.DIESEL,				60);
		put(Fluids.NAPHTHA_DS,			Fluids.XYLENE,					60,		Fluids.DIESEL_REFORM,		40);
		put(Fluids.NAPHTHA_CRACK,		Fluids.HEATINGOIL,				30,		Fluids.DIESEL_CRACK,		70);
		put(Fluids.LIGHTOIL,			Fluids.DIESEL,					40,		Fluids.KEROSENE,			60);
		put(Fluids.LIGHTOIL_DS,			Fluids.DIESEL_REFORM,			60,		Fluids.KEROSENE_REFORM,		40);
		put(Fluids.LIGHTOIL_CRACK,		Fluids.KEROSENE,				70,		Fluids.PETROLEUM,			30);
		put(Fluids.COALOIL,				Fluids.COALGAS,					30,		Fluids.OIL,					70);
		put(Fluids.COALCREOSOTE,		Fluids.COALOIL,					10,		Fluids.BITUMEN,				90);
		put(Fluids.REFORMATE,			Fluids.AROMATICS,				40,		Fluids.XYLENE,				60);
		put(Fluids.LIGHTOIL_VACUUM,		Fluids.KEROSENE,				70,		Fluids.REFORMGAS,			30);
		put(Fluids.EGG,					Fluids.CHOLESTEROL,				50,		Fluids.RADIOSOLVENT,		50);
		put(Fluids.OIL_COKER,			Fluids.CRACKOIL,				30,		Fluids.HEATINGOIL,			70);
		put(Fluids.NAPHTHA_COKER,		Fluids.NAPHTHA_CRACK,			75,		Fluids.LIGHTOIL_CRACK,		25);
		put(Fluids.GAS_COKER,			Fluids.AROMATICS,				25,		Fluids.CARBONDIOXIDE,		75);
		put(Fluids.CHLOROCALCITE_MIX,	Fluids.CHLOROCALCITE_CLEANED,	50,		Fluids.COLLOID,				50);
		put(Fluids.BAUXITE_SOLUTION,	Fluids.REDMUD,					50,		Fluids.SODIUM_ALUMINATE,	50);
	}

	private static void put(FluidType in, FluidType left, int leftAmount, FluidType right, int rightAmount) {
		fractions.put(in, new Pair<>(new FluidStack(left, leftAmount), new FluidStack(right, rightAmount)));
	}

	public static Pair<FluidStack, FluidStack> getFractions(FluidType oil) {
		return fractions.get(oil);
	}
}
