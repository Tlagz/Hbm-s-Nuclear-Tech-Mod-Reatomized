package com.hbm.inventory.recipes;

import java.util.HashMap;
import java.util.Map;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums.EnumTarType;
import com.hbm.items.ModItems;

import net.minecraft.world.item.ItemStack;

/**
 * Refinery: 100mB of a hot oil into four fractions plus a solid every 10 operations.
 *
 * TODO the original's SerializableRecipe (hbmRefinery.json in the config folder), JEI
 */
public class RefineryRecipes {

	/// fractions in percent ///
	public static final int oil_frac_heavy = 50;
	public static final int oil_frac_naph = 25;
	public static final int oil_frac_light = 15;
	public static final int oil_frac_petro = 10;
	public static final int crack_frac_naph = 40;
	public static final int crack_frac_light = 30;
	public static final int crack_frac_aroma = 15;
	public static final int crack_frac_unsat = 15;

	public static final int oilds_frac_heavy = 30;
	public static final int oilds_frac_naph = 35;
	public static final int oilds_frac_light = 20;
	public static final int oilds_frac_unsat = 15;
	public static final int crackds_frac_naph = 35;
	public static final int crackds_frac_light = 35;
	public static final int crackds_frac_aroma = 15;
	public static final int crackds_frac_unsat = 15;

	private static Map<FluidType, RefineryRecipe> recipes = new HashMap<>();

	/** Needs registered items and fluids, called during common setup */
	public static void registerDefaults() {
		recipes.clear();
		recipes.put(Fluids.HOTOIL, new RefineryRecipe(
				new FluidStack(Fluids.HEAVYOIL,		oil_frac_heavy),
				new FluidStack(Fluids.NAPHTHA,		oil_frac_naph),
				new FluidStack(Fluids.LIGHTOIL,		oil_frac_light),
				new FluidStack(Fluids.PETROLEUM,	oil_frac_petro),
				new ItemStack(ModItems.sulfur.get())
				));
		recipes.put(Fluids.HOTCRACKOIL, new RefineryRecipe(
				new FluidStack(Fluids.NAPHTHA_CRACK,	crack_frac_naph),
				new FluidStack(Fluids.LIGHTOIL_CRACK,	crack_frac_light),
				new FluidStack(Fluids.AROMATICS,		crack_frac_aroma),
				new FluidStack(Fluids.UNSATURATEDS,		crack_frac_unsat),
				ModItems.oil_tar.stack(EnumTarType.CRACK)
				));
		recipes.put(Fluids.HOTOIL_DS, new RefineryRecipe(
				new FluidStack(Fluids.HEAVYOIL,		oilds_frac_heavy),
				new FluidStack(Fluids.NAPHTHA_DS,	oilds_frac_naph),
				new FluidStack(Fluids.LIGHTOIL_DS,	oilds_frac_light),
				new FluidStack(Fluids.UNSATURATEDS,	oilds_frac_unsat),
				ModItems.oil_tar.stack(EnumTarType.PARAFFIN)
				));
		recipes.put(Fluids.HOTCRACKOIL_DS, new RefineryRecipe(
				new FluidStack(Fluids.NAPHTHA_DS,		crackds_frac_naph),
				new FluidStack(Fluids.LIGHTOIL_DS,		crackds_frac_light),
				new FluidStack(Fluids.AROMATICS,		crackds_frac_aroma),
				new FluidStack(Fluids.UNSATURATEDS,		crackds_frac_unsat),
				ModItems.oil_tar.stack(EnumTarType.PARAFFIN)
				));
	}

	public static RefineryRecipe getRefinery(FluidType oil) {
		return recipes.get(oil);
	}

	public static class RefineryRecipe {

		public FluidStack[] outputs;
		public ItemStack solid;

		public RefineryRecipe(FluidStack f0, FluidStack f1, FluidStack f2, FluidStack f3, ItemStack f4) {
			this.outputs = new FluidStack[] {f0, f1, f2, f3};
			this.solid = f4;
		}
	}
}
