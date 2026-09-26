package com.hbm.inventory.recipes;

import java.util.HashMap;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.util.Tuple.Triplet;

/**
 * The original's VacuumRefineryRecipes, ReformingRecipes and HydrotreatingRecipes: 100mB of the input fluid into
 * the listed outputs (the hydrotreater's first stack is the hydrogen it uses).
 *
 * TODO JSON config (SerializableRecipe), JEI
 */
public class OilProcessingRecipes {

	/// VACUUM REFINERY ///

	public static final int vac_frac_heavy = 40;
	public static final int vac_frac_reform = 25;
	public static final int vac_frac_light = 20;
	public static final int vac_frac_sour = 15;

	public static HashMap<FluidType, FluidStack[]> vacuum = new HashMap<>();

	/// CATALYTIC REFORMER ///

	public static HashMap<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> reforming = new HashMap<>();

	/// HYDROTREATER ///

	public static HashMap<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> hydrotreating = new HashMap<>();

	public static void registerDefaults() {

		vacuum.clear();
		vacuum.put(Fluids.OIL, new FluidStack[] {
				new FluidStack(Fluids.HEAVYOIL_VACUUM,	vac_frac_heavy),
				new FluidStack(Fluids.REFORMATE,		vac_frac_reform),
				new FluidStack(Fluids.LIGHTOIL_VACUUM,	vac_frac_light),
				new FluidStack(Fluids.SOURGAS,			vac_frac_sour)
		});
		vacuum.put(Fluids.OIL_DS, new FluidStack[] {
				new FluidStack(Fluids.HEAVYOIL_VACUUM,	vac_frac_heavy),
				new FluidStack(Fluids.REFORMATE,		vac_frac_reform),
				new FluidStack(Fluids.LIGHTOIL_VACUUM,	vac_frac_light),
				new FluidStack(Fluids.REFORMGAS,		vac_frac_sour)
		});

		reforming.clear();
		reform(Fluids.HEATINGOIL,		Fluids.NAPHTHA, 50,			Fluids.PETROLEUM, 15,	Fluids.HYDROGEN, 10);
		reform(Fluids.NAPHTHA,			Fluids.REFORMATE, 50,		Fluids.PETROLEUM, 15,	Fluids.HYDROGEN, 10);
		reform(Fluids.NAPHTHA_CRACK,	Fluids.REFORMATE, 50,		Fluids.AROMATICS, 10,	Fluids.HYDROGEN, 5);
		reform(Fluids.NAPHTHA_COKER,	Fluids.REFORMATE, 50,		Fluids.REFORMGAS, 10,	Fluids.HYDROGEN, 5);
		reform(Fluids.LIGHTOIL,			Fluids.AROMATICS, 50,		Fluids.REFORMGAS, 10,	Fluids.HYDROGEN, 15);
		reform(Fluids.LIGHTOIL_CRACK,	Fluids.AROMATICS, 50,		Fluids.REFORMGAS, 5,	Fluids.HYDROGEN, 20);
		reform(Fluids.PETROLEUM,		Fluids.UNSATURATEDS, 85,	Fluids.REFORMGAS, 10,	Fluids.HYDROGEN, 5);
		reform(Fluids.SOURGAS,			Fluids.SULFURIC_ACID, 75,	Fluids.PETROLEUM, 10,	Fluids.HYDROGEN, 15);
		reform(Fluids.CHOLESTEROL,		Fluids.ESTRADIOL, 50,		Fluids.REFORMGAS, 35,	Fluids.HYDROGEN, 15);

		hydrotreating.clear();
		hydrotreat(Fluids.OIL,					5,	Fluids.OIL_DS, 90,			Fluids.SOURGAS, 15);
		hydrotreat(Fluids.CRACKOIL,				5,	Fluids.CRACKOIL_DS, 90,		Fluids.SOURGAS, 15);
		hydrotreat(Fluids.GAS,					5,	Fluids.PETROLEUM, 80,		Fluids.SOURGAS, 15);
		hydrotreat(Fluids.DIESEL_CRACK,			10,	Fluids.DIESEL, 80,			Fluids.SOURGAS, 30);
		hydrotreat(Fluids.DIESEL_CRACK_REFORM,	10,	Fluids.DIESEL_REFORM, 80,	Fluids.SOURGAS, 30);
		hydrotreat(Fluids.COALOIL,				10,	Fluids.COALGAS, 80,			Fluids.SOURGAS, 15);
	}

	private static void reform(FluidType in, FluidType a, int am, FluidType b, int bm, FluidType c, int cm) {
		reforming.put(in, new Triplet<>(new FluidStack(a, am), new FluidStack(b, bm), new FluidStack(c, cm)));
	}

	/** The hydrogen is pressurized (1 PU) */
	private static void hydrotreat(FluidType in, int hydrogen, FluidType a, int am, FluidType b, int bm) {
		hydrotreating.put(in, new Triplet<>(new FluidStack(Fluids.HYDROGEN, hydrogen, 1), new FluidStack(a, am), new FluidStack(b, bm)));
	}
}
