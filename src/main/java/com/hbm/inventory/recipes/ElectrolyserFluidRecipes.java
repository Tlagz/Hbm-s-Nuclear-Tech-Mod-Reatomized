package com.hbm.inventory.recipes;

import java.util.HashMap;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import net.minecraft.world.item.ItemStack;

/**
 * Electrolyser fluid mode: an amount of a fluid into up to two fluids and byproduct items.
 *
 * TODO JSON config, JEI
 */
public class ElectrolyserFluidRecipes {

	public static HashMap<FluidType, ElectrolysisRecipe> recipes = new HashMap<>();

	public static void registerDefaults() {
		recipes.clear();

		recipes.put(Fluids.WATER, new ElectrolysisRecipe(2_000, new FluidStack(Fluids.HYDROGEN, 200), new FluidStack(Fluids.OXYGEN, 200), 10));
		recipes.put(Fluids.HEAVYWATER, new ElectrolysisRecipe(2_000, new FluidStack(Fluids.DEUTERIUM, 200), new FluidStack(Fluids.OXYGEN, 200), 10));
		recipes.put(Fluids.VITRIOL, new ElectrolysisRecipe(1_000, new FluidStack(Fluids.SULFURIC_ACID, 500), new FluidStack(Fluids.CHLORINE, 500), new ItemStack(ModItems.powder_iron.get()), new ItemStack(ModItems.ingot_mercury.get())));
		recipes.put(Fluids.SLOP, new ElectrolysisRecipe(1_000, new FluidStack(Fluids.MERCURY, 250), new FluidStack(Fluids.NONE, 0), new ItemStack(ModItems.niter.get(), 2), new ItemStack(ModItems.powder_limestone.get(), 2), new ItemStack(ModItems.sulfur.get())));
		recipes.put(Fluids.REDMUD, new ElectrolysisRecipe(450, new FluidStack(Fluids.MERCURY, 150), new FluidStack(Fluids.LYE, 50), new ItemStack(ModItems.powder_titanium.get(), 3), new ItemStack(ModItems.powder_iron.get(), 3), new ItemStack(ModItems.powder_aluminium.get(), 2)));
		recipes.put(Fluids.ALUMINA, new ElectrolysisRecipe(200, new FluidStack(Fluids.CARBONDIOXIDE, 100), new FluidStack(Fluids.NONE, 0), 40, new ItemStack(ModItems.powder_aluminium.get(), 7), new ItemStack(ModItems.fluorite.get(), 2)));

		recipes.put(Fluids.POTASSIUM_CHLORIDE, new ElectrolysisRecipe(250, new FluidStack(Fluids.CHLORINE, 125), new FluidStack(Fluids.NONE, 0), new ItemStack(ModItems.dust.get())));
		recipes.put(Fluids.CALCIUM_CHLORIDE, new ElectrolysisRecipe(250, new FluidStack(Fluids.CHLORINE, 125), new FluidStack(Fluids.CALCIUM_SOLUTION, 125)));

		MainRegistry.logger.info("Electrolyser fluid recipes: " + recipes.size());
	}

	public static ElectrolysisRecipe getRecipe(FluidType type) {
		if(type == null) return null;
		return recipes.get(type);
	}

	public static class ElectrolysisRecipe {
		public FluidStack output1;
		public FluidStack output2;
		public int amount;
		public ItemStack[] byproduct;
		public int duration;

		public ElectrolysisRecipe(int amount, FluidStack output1, FluidStack output2, ItemStack... byproduct) {
			this(amount, output1, output2, 20, byproduct);
		}

		public ElectrolysisRecipe(int amount, FluidStack output1, FluidStack output2, int duration, ItemStack... byproduct) {
			this.output1 = output1;
			this.output2 = output2;
			this.amount = amount;
			this.byproduct = byproduct;
			this.duration = duration;
		}
	}
}
