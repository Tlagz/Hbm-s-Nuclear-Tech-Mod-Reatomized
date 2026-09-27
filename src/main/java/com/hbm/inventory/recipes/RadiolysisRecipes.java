package com.hbm.inventory.recipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.util.Tuple.Pair;

/** Radiolysis: water into peroxide and hydrogen, plus all cracking recipes (registered after the cracking recipes) */
public class RadiolysisRecipes {

	private static final Map<FluidType, Pair<FluidStack, FluidStack>> radiolysis = new HashMap<>();

	public static void registerRadiolysis() {
		radiolysis.clear();
		radiolysis.put(Fluids.WATER, new Pair<>(new FluidStack(Fluids.PEROXIDE, 80), new FluidStack(Fluids.HYDROGEN, 20)));

		// the cracking recipes are radiolysis recipes too, so the numbers stay consistent
		if(CrackingRecipes.cracking.isEmpty()) {
			throw new IllegalStateException("The cracking recipes are empty while registering the radiolysis recipes! Either the load order is broken or cracking recipes have been removed!");
		}

		for(Entry<FluidType, Pair<FluidStack, FluidStack>> recipe : CrackingRecipes.cracking.entrySet()) {
			radiolysis.put(recipe.getKey(), recipe.getValue());
		}
	}

	public static Pair<FluidStack, FluidStack> getRadiolysis(FluidType input) {
		return radiolysis.get(input);
	}

	public static Map<FluidType, Pair<FluidStack, FluidStack>> getRecipes() {
		return radiolysis;
	}
}
