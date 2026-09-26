package com.hbm.inventory.recipes;

import static com.hbm.inventory.fluid.Fluids.*;

import java.util.HashMap;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.items.ItemEnums.EnumTarType;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple.Pair;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/**
 * Solidifier recipes: an amount of a fluid into an item. Fuels without a fixed recipe turn into solid fuel, the
 * amount calculated from their heat energy, so this has to run after the fuel values were recalculated.
 *
 * TODO bio_wafer (salient), JSON config, JEI
 */
public class SolidificationRecipes {

	public static HashMap<FluidType, Pair<Integer, ItemStack>> recipes = new HashMap<>();

	public static void registerDefaults() {
		recipes.clear();

		registerRecipe(WATER,		1000,			Blocks.ICE);
		registerRecipe(LAVA,		1000,			Blocks.OBSIDIAN);
		registerRecipe(MERCURY,		125,			ModItems.ingot_mercury.get());
		registerRecipe(BIOGAS,		250,			new ItemStack(ModItems.biomass_compressed.get(), 4));
		registerRecipe(ENDERJUICE,	100,			Items.ENDER_PEARL);
		registerRecipe(WATZ,		1000,			ModItems.ingot_mud.get());
		registerRecipe(REDMUD,		450,			Items.IRON_INGOT);
		registerRecipe(SODIUM,		100,			ModItems.powder_sodium.get());
		registerRecipe(LEAD,		100,			ModItems.ingot_lead.get());
		registerRecipe(SLOP,		250,			ModBlocks.ore_oil_sand.get());

		registerRecipe(OIL,				200,	tar(EnumTarType.CRUDE));
		registerRecipe(CRACKOIL,		200,	tar(EnumTarType.CRACK));
		registerRecipe(COALOIL,			200,	tar(EnumTarType.COAL));
		registerRecipe(HEAVYOIL,		150,	tar(EnumTarType.CRUDE));
		registerRecipe(HEAVYOIL_VACUUM,	150,	tar(EnumTarType.CRUDE));
		registerRecipe(BITUMEN,			100,	tar(EnumTarType.CRUDE));
		registerRecipe(COALCREOSOTE,	200,	tar(EnumTarType.COAL));
		registerRecipe(WOODOIL,			1000,	tar(EnumTarType.WOOD));
		registerRecipe(LUBRICANT,		100,	tar(EnumTarType.PARAFFIN));

		registerRecipe(BALEFIRE,		250,	ModItems.solid_fuel_bf.get());

		for(FluidType fuel : new FluidType[] {SMEAR, HEATINGOIL, HEATINGOIL_VACUUM, RECLAIMED, PETROIL, NAPHTHA, NAPHTHA_CRACK, DIESEL, DIESEL_REFORM,
				DIESEL_CRACK, DIESEL_CRACK_REFORM, LIGHTOIL, LIGHTOIL_CRACK, LIGHTOIL_VACUUM, KEROSENE, KEROSENE_REFORM, SOURGAS, REFORMGAS, SYNGAS,
				PETROLEUM, LPG, BIOFUEL, AROMATICS, UNSATURATEDS, REFORMATE, XYLENE}) {
			registerSFAuto(fuel, 1_440_000L, ModItems.solid_fuel.get()); //3200 burntime * 1.5 burntime bonus * 300 TU/t
		}
		registerSFAuto(BALEFIRE, 24_000_000L, ModItems.solid_fuel_bf.get()); //holy shit this is energy dense

		MainRegistry.logger.info("Solidification recipes: " + recipes.size());
	}

	private static ItemStack tar(EnumTarType type) {
		return new ItemStack(ModItems.oil_tar.get(type).get());
	}

	private static void registerSFAuto(FluidType fluid, long tuPerSF, ItemLike fuel) {
		FT_Flammable flammable = fluid.getTrait(FT_Flammable.class);
		if(flammable == null || flammable.getHeatEnergy() <= 0) return;

		long tuPerBucket = flammable.getHeatEnergy();
		double penalty = 1.25D;

		int mB = (int) (tuPerSF * 1000L * penalty / tuPerBucket);

		if(mB > 10_000) mB -= (mB % 1000);
		else if(mB > 1_000) mB -= (mB % 100);
		else if(mB > 100) mB -= (mB % 10);

		mB = Math.max(mB, 1);

		registerRecipe(fluid, mB, fuel);
	}

	private static void registerRecipe(FluidType type, int quantity, ItemLike output) { registerRecipe(type, quantity, new ItemStack(output)); }
	private static void registerRecipe(FluidType type, int quantity, ItemStack output) {
		recipes.put(type, new Pair<>(quantity, output));
	}

	public static Pair<Integer, ItemStack> getOutput(FluidType type) {
		return recipes.get(type);
	}
}
