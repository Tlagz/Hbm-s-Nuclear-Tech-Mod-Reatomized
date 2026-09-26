package com.hbm.inventory.recipes;

import static com.hbm.inventory.OreDictManager.*;

import java.util.HashMap;

import com.hbm.blocks.BlockEnums.EnumStoneType;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Mixer recipes, keyed by the output fluid (the fluid identifier picks it). Some outputs have several recipes the
 * GUI cycles through.
 *
 * TODO PHEROMONE_M (pill_herbal isn't ported), JSON config, JEI
 */
public class MixerRecipes {

	public static HashMap<FluidType, MixerRecipe[]> recipes = new HashMap<>();

	public static void registerDefaults() {
		recipes.clear();

		register(Fluids.COOLANT, new MixerRecipe(2_000, 50).setStack1(new FluidStack(Fluids.WATER, 1_800)).setSolid(new OreDictStack(KNO.dust())));
		register(Fluids.CRYOGEL, new MixerRecipe(2_000, 50).setStack1(new FluidStack(Fluids.COOLANT, 1_800)).setSolid(new ComparableStack(ModItems.powder_ice.get())));
		register(Fluids.NITAN, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.KEROSENE, 600)).setStack2(new FluidStack(Fluids.MERCURY, 200)).setSolid(new ComparableStack(ModItems.powder_nitan_mix.get())));
		register(Fluids.FRACKSOL,
				new MixerRecipe(1_000, 20).setStack1(new FluidStack(Fluids.SULFURIC_ACID, 900)).setStack2(new FluidStack(Fluids.PETROLEUM, 100)),
				new MixerRecipe(1_000, 20).setStack1(new FluidStack(Fluids.WATER, 1000)).setStack2(new FluidStack(Fluids.PETROLEUM, 100)).setSolid(new OreDictStack(S.dust())));
		register(Fluids.ENDERJUICE, new MixerRecipe(100, 100).setStack1(new FluidStack(Fluids.XPJUICE, 500)).setSolid(new OreDictStack(DIAMOND.dust())));
		register(Fluids.SALIENT, new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.SEEDSLURRY, 500)).setStack2(new FluidStack(Fluids.BLOOD, 500)));
		register(Fluids.COLLOID, new MixerRecipe(500, 20).setStack1(new FluidStack(Fluids.WATER, 500)).setSolid(new ComparableStack(ModItems.dust.get())));
		register(Fluids.PHOSGENE, new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.UNSATURATEDS, 500)).setStack2(new FluidStack(Fluids.CHLORINE, 500)));
		register(Fluids.MUSTARDGAS, new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.REFORMGAS, 750)).setStack2(new FluidStack(Fluids.CHLORINE, 250)).setSolid(new OreDictStack(S.dust())));
		register(Fluids.IONGEL, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.WATER, 1000)).setStack2(new FluidStack(Fluids.HYDROGEN, 200)).setSolid(new ComparableStack(ModItems.pellet_charged.get())));
		register(Fluids.EGG, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.RADIOSOLVENT, 500)).setSolid(new ComparableStack(Items.EGG)));
		register(Fluids.FISHOIL, new MixerRecipe(100, 50).setSolid(new TagStack(net.minecraft.tags.ItemTags.FISHES)));
		register(Fluids.SUNFLOWEROIL, new MixerRecipe(100, 50).setSolid(new ComparableStack(Blocks.SUNFLOWER)));
		register(Fluids.FULLERENE, new MixerRecipe(250, 50).setStack1(new FluidStack(Fluids.RADIOSOLVENT, 500)).setSolid(new ComparableStack(ModItems.powder_ash.get(EnumAshType.SOOT).get())));

		register(Fluids.SOLVENT,
				new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.NAPHTHA, 500)).setStack2(new FluidStack(Fluids.AROMATICS, 500)),
				new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.NAPHTHA_CRACK, 500)).setStack2(new FluidStack(Fluids.AROMATICS, 500)),
				new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.NAPHTHA_DS, 500)).setStack2(new FluidStack(Fluids.AROMATICS, 500)),
				new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.NAPHTHA_COKER, 500)).setStack2(new FluidStack(Fluids.AROMATICS, 500)));
		register(Fluids.SULFURIC_ACID, new MixerRecipe(500, 50).setStack1(new FluidStack(Fluids.PEROXIDE, 800)).setSolid(new OreDictStack(S.dust())));
		register(Fluids.NITRIC_ACID, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.SULFURIC_ACID, 500)).setSolid(new OreDictStack(KNO.dust())));
		register(Fluids.RADIOSOLVENT, new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.REFORMGAS, 750)).setStack2(new FluidStack(Fluids.CHLORINE, 250)));
		register(Fluids.SCHRABIDIC, new MixerRecipe(16_000, 100).setStack1(new FluidStack(Fluids.SAS3, 8_000)).setStack2(new FluidStack(Fluids.PEROXIDE, 6_000)).setSolid(new ComparableStack(ModItems.pellet_charged.get())));

		register(Fluids.PETROIL, new MixerRecipe(1_000, 30).setStack1(new FluidStack(Fluids.RECLAIMED, 800)).setStack2(new FluidStack(Fluids.LUBRICANT, 200)));
		register(Fluids.LUBRICANT,
				new MixerRecipe(1_000, 20).setStack1(new FluidStack(Fluids.HEATINGOIL, 500)).setStack2(new FluidStack(Fluids.UNSATURATEDS, 500)),
				new MixerRecipe(1_000, 20).setStack1(new FluidStack(Fluids.FISHOIL, 800)).setStack2(new FluidStack(Fluids.ETHANOL, 200)),
				new MixerRecipe(1_000, 20).setStack1(new FluidStack(Fluids.SUNFLOWEROIL, 800)).setStack2(new FluidStack(Fluids.ETHANOL, 200)));
		register(Fluids.BIOFUEL,
				new MixerRecipe(250, 20).setStack1(new FluidStack(Fluids.FISHOIL, 500)).setStack2(new FluidStack(Fluids.WOODOIL, 500)),
				new MixerRecipe(200, 20).setStack1(new FluidStack(Fluids.SUNFLOWEROIL, 500)).setStack2(new FluidStack(Fluids.WOODOIL, 500)));
		register(Fluids.NITROGLYCERIN,
				new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.PETROLEUM, 1_000)).setStack2(new FluidStack(Fluids.NITRIC_ACID, 1_000)),
				new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.FISHOIL, 500)).setStack2(new FluidStack(Fluids.NITRIC_ACID, 500)));

		register(Fluids.THORIUM_SALT, new MixerRecipe(1_000, 30).setStack1(new FluidStack(Fluids.CHLORINE, 1000)).setSolid(new OreDictStack(TH232.dust())));

		register(Fluids.SYNGAS, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.COALOIL, 500)).setStack2(new FluidStack(Fluids.STEAM, 500)));
		register(Fluids.OXYHYDROGEN,
				new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.HYDROGEN, 500)).setStack2(new FluidStack(Fluids.AIR, 2_000)),
				new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.HYDROGEN, 500)).setStack2(new FluidStack(Fluids.OXYGEN, 500)));

		register(Fluids.PETROIL_LEADED, new MixerRecipe(12_000, 40).setStack1(new FluidStack(Fluids.PETROIL, 10_000)).setSolid(new ComparableStack(ModItems.fuel_additive.get(com.hbm.items.ItemEnums.EnumFuelAdditive.values()[0]).get())));
		register(Fluids.GASOLINE_LEADED, new MixerRecipe(12_000, 40).setStack1(new FluidStack(Fluids.GASOLINE, 10_000)).setSolid(new ComparableStack(ModItems.fuel_additive.get(com.hbm.items.ItemEnums.EnumFuelAdditive.values()[0]).get())));
		register(Fluids.COALGAS_LEADED, new MixerRecipe(12_000, 40).setStack1(new FluidStack(Fluids.COALGAS, 10_000)).setSolid(new ComparableStack(ModItems.fuel_additive.get(com.hbm.items.ItemEnums.EnumFuelAdditive.values()[0]).get())));

		register(Fluids.DIESEL_REFORM, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.DIESEL, 900)).setStack2(new FluidStack(Fluids.REFORMATE, 100)));
		register(Fluids.DIESEL_CRACK_REFORM, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.DIESEL_CRACK, 900)).setStack2(new FluidStack(Fluids.REFORMATE, 100)));
		register(Fluids.KEROSENE_REFORM, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.KEROSENE, 900)).setStack2(new FluidStack(Fluids.REFORMATE, 100)));

		register(Fluids.CHLOROCALCITE_SOLUTION, new MixerRecipe(500, 50).setStack1(new FluidStack(Fluids.WATER, 250)).setStack2(new FluidStack(Fluids.NITRIC_ACID, 250)).setSolid(new OreDictStack(CHLOROCALCITE.dust())));
		register(Fluids.CHLOROCALCITE_MIX, new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.CHLOROCALCITE_SOLUTION, 500)).setStack2(new FluidStack(Fluids.SULFURIC_ACID, 500)).setSolid(new ComparableStack(ModItems.powder_flux.get())));

		register(Fluids.BAUXITE_SOLUTION, new MixerRecipe(300, 80).setStack1(new FluidStack(Fluids.LYE, 50)).setSolid(new ComparableStack(ModBlocks.stone_resource.get(EnumStoneType.BAUXITE).get())));
		register(Fluids.LYE, new MixerRecipe(100, 100).setStack1(new FluidStack(Fluids.WATER, 100)).setSolid(new ComparableStack(ModItems.powder_ash.get(EnumAshType.WOOD).get())));
		register(Fluids.ALUMINA, new MixerRecipe(200, 40).setStack1(new FluidStack(Fluids.SODIUM_ALUMINATE, 150)).setSolid(new OreDictStack(F.dust(), 3)),
				new MixerRecipe(300, 40).setStack1(new FluidStack(Fluids.SODIUM_ALUMINATE, 150)).setSolid(new ComparableStack(ModItems.chunk_ore.get(EnumChunkType.CRYOLITE).get())));

		register(Fluids.PERFLUOROMETHYL, new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.PETROLEUM, 1000)).setStack2(new FluidStack(Fluids.UNSATURATEDS, 500)).setSolid(new OreDictStack(F.dust())));

		register(Fluids.BITUMEN, new MixerRecipe(50, 20).setSolid(new OreDictStack(ANY_TAR.any())));

		MainRegistry.logger.info("Mixer recipes: " + recipes.size() + " outputs");
	}

	public static void register(FluidType type, MixerRecipe... rec) {
		recipes.put(type, rec);
	}

	public static MixerRecipe[] getOutput(FluidType type) {
		return recipes.get(type);
	}

	public static MixerRecipe getOutput(FluidType type, int index) {
		MixerRecipe[] recs = recipes.get(type);
		if(recs == null) return null;
		return recs[index % recs.length];
	}

	/** An ingredient from a vanilla item tag (the original used wildcard metadata, e.g. for all fish) */
	public static class TagStack extends OreDictStack {

		private final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> key;

		public TagStack(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> key) {
			super(key.location().toString());
			this.key = key;
		}

		@Override
		public net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag() {
			return key;
		}
	}

	public static class MixerRecipe {
		public FluidStack input1;
		public FluidStack input2;
		public AStack solidInput;
		public int processTime;
		public int output;

		protected MixerRecipe(int output, int processTime) {
			this.output = output;
			this.processTime = processTime;
		}

		protected MixerRecipe setStack1(FluidStack stack) { input1 = stack; return this; }
		protected MixerRecipe setStack2(FluidStack stack) { input2 = stack; return this; }
		protected MixerRecipe setSolid(AStack stack) { solidInput = stack; return this; }
	}
}
