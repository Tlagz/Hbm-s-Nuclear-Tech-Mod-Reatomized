package com.hbm.inventory.recipes;

import static com.hbm.inventory.OreDictManager.*;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/**
 * Liquefactor recipes: one item into a fluid. Exact items first, then tags, and any food not listed turns into
 * salient green.
 *
 * TODO plant_flower (hemp/tobacco), JSON config, JEI
 */
public class LiquefactionRecipes {

	public static LinkedHashMap<ComparableStack, FluidStack> stacks = new LinkedHashMap<>();
	public static LinkedHashMap<TagKey<Item>, FluidStack> tags = new LinkedHashMap<>();

	public static void registerDefaults() {
		stacks.clear();
		tags.clear();

		//oil processing
		put(COAL.gem(),						new FluidStack(Fluids.COALOIL, 250));
		put(COAL.dust(),					new FluidStack(Fluids.COALOIL, 250));
		put(LIGNITE.gem(),					new FluidStack(Fluids.COALOIL, 150));
		put(LIGNITE.dust(),					new FluidStack(Fluids.COALOIL, 150));
		put(KEY_OIL_TAR,					new FluidStack(Fluids.BITUMEN, 75));
		put(KEY_CRACK_TAR,					new FluidStack(Fluids.BITUMEN, 100));
		put(KEY_COAL_TAR,					new FluidStack(Fluids.BITUMEN, 50));
		tags.put(ItemTags.LOGS,				new FluidStack(Fluids.MUG, 100));
		put(NA.dust(),						new FluidStack(Fluids.SODIUM, 100));
		put(PB.ingot(),						new FluidStack(Fluids.LEAD, 100));
		put(PB.dust(),						new FluidStack(Fluids.LEAD, 100));
		put(PB.block(),						new FluidStack(Fluids.LEAD, 900));

		// general utility recipes because why not
		put(Blocks.NETHERRACK,				new FluidStack(Fluids.LAVA, 250));
		put(Blocks.COBBLESTONE,				new FluidStack(Fluids.LAVA, 250));
		put(Blocks.STONE,					new FluidStack(Fluids.LAVA, 250));
		put(Blocks.OBSIDIAN,				new FluidStack(Fluids.LAVA, 500));
		put(Items.SNOWBALL,					new FluidStack(Fluids.WATER, 125));
		put(Blocks.SNOW_BLOCK,				new FluidStack(Fluids.WATER, 500));
		put(Blocks.ICE,						new FluidStack(Fluids.WATER, 1000));
		put(Blocks.PACKED_ICE,				new FluidStack(Fluids.WATER, 1000));
		put(Items.ENDER_PEARL,				new FluidStack(Fluids.ENDERJUICE, 100));
		put(ModBlocks.ore_oil_sand.get(),	new FluidStack(Fluids.BITUMEN, 100));

		// biofuel
		put(Items.SUGAR,					new FluidStack(Fluids.ETHANOL, 100));
		put(Items.MELON_SLICE,				new FluidStack(Fluids.ETHANOL, 100));
		put(ModItems.biomass.get(),			new FluidStack(Fluids.BIOGAS, 125));
		put(ModItems.glyphid_gland_empty.get(), new FluidStack(Fluids.BIOGAS, 2000));
		tags.put(ItemTags.FISHES,			new FluidStack(Fluids.FISHOIL, 100));
		put(Blocks.SUNFLOWER,				new FluidStack(Fluids.SUNFLOWEROIL, 100));

		// plant gunk
		put(Items.WHEAT_SEEDS,				new FluidStack(Fluids.SEEDSLURRY, 50));
		put(Blocks.SHORT_GRASS,				new FluidStack(Fluids.SEEDSLURRY, 100));
		put(Blocks.FERN,					new FluidStack(Fluids.SEEDSLURRY, 100));
		put(Blocks.VINE,					new FluidStack(Fluids.SEEDSLURRY, 100));

		MainRegistry.logger.info("Liquefaction recipes: " + (stacks.size() + tags.size()));
	}

	private static void put(String dict, FluidStack out) {
		tags.put(OreDictManager.tag(dict), out);
	}

	private static void put(ItemLike item, FluidStack out) {
		stacks.put(new ComparableStack(item), out);
	}

	public static FluidStack getOutput(ItemStack stack) {
		if(stack == null || stack.isEmpty()) return null;

		FluidStack exact = stacks.get(new ComparableStack(stack).makeSingular());
		if(exact != null) return exact;

		for(Entry<TagKey<Item>, FluidStack> entry : tags.entrySet()) {
			if(stack.is(entry.getKey())) return entry.getValue();
		}

		FoodProperties food = stack.get(DataComponents.FOOD);
		if(food != null) {
			// the original's food val * saturation mod * 2 (constant) * 10 (quanta), saturation already has the first three
			int amount = (int) (food.saturation() * 10);
			if(amount > 0) return new FluidStack(Fluids.SALIENT, amount);
		}

		return null;
	}
}
