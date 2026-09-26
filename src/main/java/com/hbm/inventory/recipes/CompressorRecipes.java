package com.hbm.inventory.recipes;

import java.util.HashMap;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

/**
 * Compressor recipes for a fluid at a pressure. Without a recipe the compressor just raises the pressure by one
 * (1000mB per operation).
 *
 * TODO JSON config, JEI
 */
public class CompressorRecipes {

	public record Key(FluidType type, int pressure) { }

	public static HashMap<Key, CompressorRecipe> recipes = new HashMap<>();

	public static void registerDefaults() {
		recipes.clear();

		recipes.put(new Key(Fluids.PETROLEUM, 0), new CompressorRecipe(2_000, new FluidStack(Fluids.PETROLEUM, 2_000, 1), 20));
		recipes.put(new Key(Fluids.PETROLEUM, 1), new CompressorRecipe(2_000, new FluidStack(Fluids.LPG, 1_000, 0), 20));

		recipes.put(new Key(Fluids.BLOOD, 3), new CompressorRecipe(1_000, new FluidStack(Fluids.HEAVYOIL, 250, 0), 200));

		recipes.put(new Key(Fluids.PERFLUOROMETHYL, 0), new CompressorRecipe(1_000, new FluidStack(Fluids.PERFLUOROMETHYL, 1_000, 1), 50));
		recipes.put(new Key(Fluids.PERFLUOROMETHYL, 1), new CompressorRecipe(1_000, new FluidStack(Fluids.PERFLUOROMETHYL_COLD, 1_000, 0), 50));
	}

	public static CompressorRecipe getRecipe(FluidType type, int pressure) {
		return recipes.get(new Key(type, pressure));
	}

	public static class CompressorRecipe {

		public FluidStack output;
		public int inputAmount;
		public int duration;

		public CompressorRecipe(int input, FluidStack output, int duration) {
			this.output = output;
			this.inputAmount = input;
			this.duration = duration;
		}

		public CompressorRecipe(int input, FluidStack output) {
			this(input, output, 100);
		}
	}
}
