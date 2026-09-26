package com.hbm.inventory.recipes;

import static com.hbm.inventory.OreDictManager.*;
import static com.hbm.inventory.fluid.Fluids.*;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * Pyrolysis oven recipes: an optional fluid and an optional item into an optional fluid and an optional item. Solid
 * fuel amounts are calculated from the runtime heat energy (at double the solidifier's efficiency).
 *
 * TODO the bedrock ore roasting recipes, JSON config, JEI
 */
public class PyroOvenRecipes {

	public static List<PyroOvenRecipe> recipes = new ArrayList<>();

	public static void registerDefaults() {
		recipes.clear();

		//solid fuel
		for(FluidType fuel : new FluidType[] {SMEAR, HEATINGOIL, HEATINGOIL_VACUUM, RECLAIMED, PETROIL, NAPHTHA, NAPHTHA_CRACK, DIESEL, DIESEL_REFORM,
				DIESEL_CRACK, DIESEL_CRACK_REFORM, LIGHTOIL, LIGHTOIL_CRACK, LIGHTOIL_VACUUM, KEROSENE, KEROSENE_REFORM, SOURGAS, REFORMGAS, SYNGAS,
				PETROLEUM, LPG, BIOFUEL, AROMATICS, UNSATURATEDS, REFORMATE, XYLENE}) {
			registerSFAuto(fuel, 1_440_000L, ModItems.solid_fuel.get()); //3200 burntime * 1.5 burntime bonus * 300 TU/t
		}
		registerSFAuto(BALEFIRE, 24_000_000L, ModItems.solid_fuel_bf.get());

		// steam to syngas is 1:2, syngas to LPS in this recipe is 2:1, so you can actually cycle this
		recipes.add(new PyroOvenRecipe(300).in(new FluidStack(SYNGAS, 2_000)).in(new OreDictStack(W.dust())).out(new FluidStack(SPENTSTEAM, 1_000)).out(new ItemStack(ModItems.ingot_tungsten_carbide.get())));

		//syngas from coal
		recipes.add(new PyroOvenRecipe(100).in(new FluidStack(STEAM, 500)).in(new OreDictStack(COAL.gem())).out(new FluidStack(SYNGAS, 1_000)));
		recipes.add(new PyroOvenRecipe(100).in(new FluidStack(STEAM, 500)).in(new OreDictStack(COAL.dust())).out(new FluidStack(SYNGAS, 1_000)));
		recipes.add(new PyroOvenRecipe(100).in(new FluidStack(STEAM, 250)).in(new OreDictStack(ANY_COKE.gem())).out(new FluidStack(SYNGAS, 1_000)));
		//syngas from biomass
		recipes.add(new PyroOvenRecipe(100).in(new ComparableStack(ModItems.biomass.get(), 4)).out(new FluidStack(SYNGAS, 1_000)).out(new ItemStack(Items.CHARCOAL)));
		//soot from tar (the original's hydrogen input is overwritten by the CO2 output, so it takes no fluid)
		recipes.add(new PyroOvenRecipe(40).in(new OreDictStack(ANY_TAR.any(), 4)).out(new FluidStack(CARBONDIOXIDE, 1_000)).out(new ItemStack(ModItems.powder_ash.get(EnumAshType.SOOT).get())));
		//heavyoil from coal
		recipes.add(new PyroOvenRecipe(100).in(new FluidStack(HYDROGEN, 500)).in(new OreDictStack(COAL.gem())).out(new FluidStack(HEAVYOIL, 1_000)));
		recipes.add(new PyroOvenRecipe(100).in(new FluidStack(HYDROGEN, 500)).in(new OreDictStack(COAL.dust())).out(new FluidStack(HEAVYOIL, 1_000)));
		//coalgas from coal
		recipes.add(new PyroOvenRecipe(50).in(new FluidStack(HEAVYOIL, 500)).in(new OreDictStack(COAL.gem())).out(new FluidStack(COALGAS, 1_000)));
		recipes.add(new PyroOvenRecipe(50).in(new FluidStack(HEAVYOIL, 500)).in(new OreDictStack(COAL.dust())).out(new FluidStack(COALGAS, 1_000)));
		recipes.add(new PyroOvenRecipe(50).in(new FluidStack(HEAVYOIL, 500)).in(new OreDictStack(ANY_COKE.gem())).out(new FluidStack(COALGAS, 1_000)));
		//refgas from coker gas
		recipes.add(new PyroOvenRecipe(60).in(new FluidStack(GAS_COKER, 4_000)).out(new FluidStack(REFORMGAS, 100)));
		//hydrogen and carbon from natgas
		recipes.add(new PyroOvenRecipe(60).in(new FluidStack(GAS, 12_000)).out(new FluidStack(HYDROGEN, 8_000)).out(new ItemStack(ModItems.ingot_graphite.get(), 1)));

		MainRegistry.logger.info("Pyrolysis oven recipes: " + recipes.size());
	}

	private static void registerSFAuto(FluidType fluid, long tuPerSF, ItemLike fuel) {
		FT_Flammable flammable = fluid.getTrait(FT_Flammable.class);
		if(flammable == null || flammable.getHeatEnergy() <= 0) return;

		long tuPerBucket = flammable.getHeatEnergy();
		double bonus = 0.5D; //double efficiency!!

		int mB = (int) (tuPerSF * 1000L * bonus / tuPerBucket);

		if(mB > 10_000) mB -= (mB % 1000);
		else if(mB > 1_000) mB -= (mB % 100);
		else if(mB > 100) mB -= (mB % 10);

		mB = Math.max(mB, 1);

		recipes.add(new PyroOvenRecipe(60).in(new FluidStack(fluid, mB)).out(new ItemStack(fuel)));
	}

	public static class PyroOvenRecipe {
		public FluidStack inputFluid;
		public AStack inputItem;
		public FluidStack outputFluid;
		public ItemStack outputItem;
		public int duration;

		public PyroOvenRecipe(int duration) {
			this.duration = duration;
		}

		public PyroOvenRecipe in(FluidStack stack) { this.inputFluid = stack; return this; }
		public PyroOvenRecipe in(AStack stack) { this.inputItem = stack; return this; }
		public PyroOvenRecipe out(FluidStack stack) { this.outputFluid = stack; return this; }
		public PyroOvenRecipe out(ItemStack stack) { this.outputItem = stack; return this; }
	}
}
