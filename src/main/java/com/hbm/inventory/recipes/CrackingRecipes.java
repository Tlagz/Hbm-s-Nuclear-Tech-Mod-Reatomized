package com.hbm.inventory.recipes;

import java.util.HashMap;
import java.util.Map;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.util.Tuple.Pair;

/**
 * Catalytic cracker recipes: 100mB of the input (and 200mB steam) into two products, in percent.
 * TODO JSON config (SerializableRecipe), JEI
 */
public class CrackingRecipes {

	//cracking in percent
	public static final int oil_crack_oil = 80;
	public static final int oil_crack_petro = 20;
	public static final int bitumen_crack_oil = 80;
	public static final int bitumen_crack_aroma = 20;
	public static final int smear_crack_napht = 60;
	public static final int smear_crack_petro = 40;
	public static final int gas_crack_petro = 30;
	public static final int gas_crack_unsat = 20;
	public static final int diesel_crack_kero = 40;
	public static final int diesel_crack_petro = 30;
	public static final int kero_crack_petro = 60;
	public static final int wood_crack_aroma = 10;
	public static final int wood_crack_heat = 40;
	public static final int xyl_crack_aroma = 80;
	public static final int xyl_crack_petro = 20;

	public static Map<FluidType, Pair<FluidStack, FluidStack>> cracking = new HashMap<>();

	public static void registerDefaults() {
		cracking.clear();
		put(Fluids.OIL,					Fluids.CRACKOIL,		oil_crack_oil,		Fluids.PETROLEUM,		oil_crack_petro);
		put(Fluids.BITUMEN,				Fluids.OIL,				bitumen_crack_oil,	Fluids.AROMATICS,		bitumen_crack_aroma);
		put(Fluids.SMEAR,				Fluids.NAPHTHA,			smear_crack_napht,	Fluids.PETROLEUM,		smear_crack_petro);
		put(Fluids.GAS,					Fluids.PETROLEUM,		gas_crack_petro,	Fluids.UNSATURATEDS,	gas_crack_unsat);
		put(Fluids.DIESEL,				Fluids.KEROSENE,		diesel_crack_kero,	Fluids.PETROLEUM,		diesel_crack_petro);
		put(Fluids.DIESEL_CRACK,		Fluids.KEROSENE,		diesel_crack_kero,	Fluids.PETROLEUM,		diesel_crack_petro);
		put(Fluids.KEROSENE,			Fluids.PETROLEUM,		kero_crack_petro,	Fluids.NONE,			0);
		put(Fluids.WOODOIL,				Fluids.HEATINGOIL,		wood_crack_heat,	Fluids.AROMATICS,		wood_crack_aroma);
		put(Fluids.XYLENE,				Fluids.AROMATICS,		xyl_crack_aroma,	Fluids.PETROLEUM,		xyl_crack_petro);
		put(Fluids.HEATINGOIL_VACUUM,	Fluids.HEATINGOIL,		80,					Fluids.REFORMGAS,		20);
		put(Fluids.REFORMATE,			Fluids.UNSATURATEDS,	40,					Fluids.REFORMGAS,		60);
		put(Fluids.BIOGAS,				Fluids.PETROLEUM,		20,					Fluids.AROMATICS,		20);
	}

	private static void put(FluidType in, FluidType left, int leftAmount, FluidType right, int rightAmount) {
		cracking.put(in, new Pair<>(new FluidStack(left, leftAmount), new FluidStack(right, rightAmount)));
	}

	public static Pair<FluidStack, FluidStack> getCracking(FluidType oil) {
		return cracking.get(oil);
	}
}
