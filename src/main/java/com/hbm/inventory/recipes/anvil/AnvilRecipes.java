package com.hbm.inventory.recipes.anvil;

import static com.hbm.inventory.OreDictManager.*;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.recipes.anvil.gen.GenAnvilRecipes;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * NTM anvil recipes: smithing (two inputs in the anvil's slots) and construction (ingredients taken from the
 * player's inventory, picked in the anvil's recipe list). The recipe lists are translated from the original by
 * tools/gen_anvil.py (GenAnvilRecipes), recipes with items that aren't ported yet are left out.
 *
 * TODO the special smithing recipes (hot smithing, molds, cyanide, renaming), JSON config (SerializableRecipe), JEI
 */
public class AnvilRecipes {

	public static List<AnvilSmithingRecipe> smithingRecipes = new ArrayList<>();
	public static List<AnvilConstructionRecipe> constructionRecipes = new ArrayList<>();

	/** Needs all items, called during common setup */
	/** Foundry molds pressed from a blank mold with any item of the shape as the template */
	private static void registerMolds() {
		smithingRecipes.add(new AnvilSmithingMold(0, new OreDictStack(GOLD.nugget()), "nugget", 1));
		smithingRecipes.add(new AnvilSmithingMold(1, new OreDictStack(U.billet()), "billet", 1));
		smithingRecipes.add(new AnvilSmithingMold(2, new OreDictStack(IRON.ingot()), "ingot", 1));
		smithingRecipes.add(new AnvilSmithingMold(3, new OreDictStack(IRON.plate()), "plate", 1));
		smithingRecipes.add(new AnvilSmithingMold(19, new OreDictStack(IRON.plateCast()), "plateTriple", 1));
		smithingRecipes.add(new AnvilSmithingMold(13, new OreDictStack(IRON.plateCast(), 3), "plateTriple", 3));
		smithingRecipes.add(new AnvilSmithingMold(4, new OreDictStack(CU.wireFine()), "wireFine", 1));
		smithingRecipes.add(new AnvilSmithingMold(5, new ComparableStack(ModItems.blade_titanium.get()),
				new ItemStack(ModItems.blade_titanium.get()), new ItemStack(ModItems.blade_tungsten.get())));
		smithingRecipes.add(new AnvilSmithingMold(6, new ComparableStack(ModItems.blades_steel.get()),
				new ItemStack(ModItems.blades_steel.get()), new ItemStack(ModItems.blades_titanium.get())));
		smithingRecipes.add(new AnvilSmithingMold(7, new ComparableStack(ModItems.stamp_iron_flat.get()),
				new ItemStack(ModItems.stamp_stone_flat.get()), new ItemStack(ModItems.stamp_iron_flat.get()), new ItemStack(ModItems.stamp_steel_flat.get()),
				new ItemStack(ModItems.stamp_titanium_flat.get()), new ItemStack(ModItems.stamp_obsidian_flat.get())));
		smithingRecipes.add(new AnvilSmithingMold(8, new OreDictStack(STEEL.shell()), "shell", 1));
		smithingRecipes.add(new AnvilSmithingMold(9, new OreDictStack(STEEL.pipe()), "pipe", 1));
		smithingRecipes.add(new AnvilSmithingMold(10, new OreDictStack(IRON.ingot(), 9), "ingot", 9));
		smithingRecipes.add(new AnvilSmithingMold(11, new OreDictStack(IRON.plate(), 9), "plate", 9));
		smithingRecipes.add(new AnvilSmithingMold(12, new OreDictStack(IRON.block()), "block", 1));
		smithingRecipes.add(new AnvilSmithingMold(20, new OreDictStack(MINGRADE.wireDense(), 1), "wireDense", 1));
		smithingRecipes.add(new AnvilSmithingMold(21, new OreDictStack(MINGRADE.wireDense(), 9), "wireDense", 9));
	}

	public static void register() {
		smithingRecipes.clear();
		constructionRecipes.clear();
		registerAnvilUpgrades();
		registerMolds();
		GenAnvilRecipes.registerSmithing();
		GenAnvilRecipes.registerConstruction();
		registerMaterialConstruction();
		MainRegistry.logger.info("Anvil recipes: " + smithingRecipes.size() + " smithing, " + constructionRecipes.size() + " construction");
	}

	/** Upgrading iron and lead anvils with 10 ingots (a loop in the original, so not translated by gen_anvil.py) */
	private static void registerAnvilUpgrades() {
		for(var anvil : List.of(ModBlocks.anvil_iron, ModBlocks.anvil_lead)) {
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_steel.get()), new ComparableStack(anvil), new OreDictStack(STEEL.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_desh.get()), new ComparableStack(anvil), new OreDictStack(DESH.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_saturnite.get()), new ComparableStack(anvil), new OreDictStack(BIGMT.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_ferrouranium.get()), new ComparableStack(anvil), new OreDictStack(FERRO.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_bismuth_bronze.get()), new ComparableStack(anvil), new OreDictStack(BBRONZE.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_arsenic_bronze.get()), new ComparableStack(anvil), new OreDictStack(ABRONZE.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_schrabidate.get()), new ComparableStack(anvil), new OreDictStack(SBD.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_dnt.get()), new ComparableStack(anvil), new OreDictStack(DNT.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_osmiridium.get()), new ComparableStack(anvil), new OreDictStack(OSMIRIDIUM.ingot(), 10)));
		}
	}

	/** The original's loops over the materials (wires, shells, pipes), not translated by gen_anvil.py */
	private static void registerMaterialConstruction() {
		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.autogen.contains(MaterialShapes.WIRE) && OreDictManager.exists(MaterialShapes.INGOT.make(mat))) {
				constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(MaterialShapes.INGOT.name() + mat.names[0]), new AnvilOutput(ModItems.wire_fine.stack(mat, 8))).setTier(4));
			}
		}

		for(NTMMaterial mat : Mats.orderedList) if(mat.autogen.contains(MaterialShapes.SHELL)) constructionRecipes.add(new AnvilConstructionRecipe(
				new OreDictStack(MaterialShapes.PLATE.name() + mat.names[0], 4),
				new AnvilOutput(ModItems.shell.stack(mat))).setTier(1));
		for(NTMMaterial mat : Mats.orderedList) if(mat.autogen.contains(MaterialShapes.PIPE)) {
			String key = (OreDictManager.exists(MaterialShapes.PLATE.name() + mat.names[0]) ?
					MaterialShapes.PLATE.name() + mat.names[0] : MaterialShapes.INGOT.name() + mat.names[0]);
			constructionRecipes.add(new AnvilConstructionRecipe(
					new OreDictStack(key, 3),
					new AnvilOutput(ModItems.pipe.stack(mat))).setTier(1));
		}
	}

	public static List<AnvilSmithingRecipe> getSmithing() {
		return smithingRecipes;
	}

	public static List<AnvilConstructionRecipe> getConstruction() {
		return constructionRecipes;
	}

	public static class AnvilConstructionRecipe {
		public List<AStack> input = new ArrayList<>();
		public List<AnvilOutput> output = new ArrayList<>();
		public int tierLower = 0;
		public int tierUpper = -1;
		OverlayType overlay = OverlayType.NONE;

		public AnvilConstructionRecipe(AStack input, AnvilOutput output) {
			this.input.add(input);
			this.output.add(output);
			this.setOverlay(OverlayType.SMITHING); //preferred overlay for 1:1 conversions is smithing
		}

		public AnvilConstructionRecipe(AStack[] input, AnvilOutput output) {
			for(AStack stack : input) this.input.add(stack);
			this.output.add(output);
			this.setOverlay(OverlayType.CONSTRUCTION); //preferred overlay for many:1 conversions is construction
		}

		public AnvilConstructionRecipe(AStack input, AnvilOutput[] output) {
			this.input.add(input);
			for(AnvilOutput out : output) this.output.add(out);
			this.setOverlay(OverlayType.RECYCLING); //preferred overlay for 1:many conversions is recycling
		}

		public AnvilConstructionRecipe(AStack[] input, AnvilOutput[] output) {
			for(AStack stack : input) this.input.add(stack);
			for(AnvilOutput out : output) this.output.add(out);
			this.setOverlay(OverlayType.NONE); //no preferred overlay for many:many conversions
		}

		public AnvilConstructionRecipe setTier(int tier) {
			this.tierLower = tier;
			return this;
		}

		public AnvilConstructionRecipe setTierRange(int lower, int upper) {
			this.tierLower = lower;
			this.tierUpper = upper;
			return this;
		}

		public boolean isTierValid(int tier) {

			if(this.tierUpper == -1)
				return tier >= this.tierLower;

			return tier >= this.tierLower && tier <= this.tierUpper;
		}

		public AnvilConstructionRecipe setOverlay(OverlayType overlay) {
			this.overlay = overlay;
			return this;
		}

		public OverlayType getOverlay() {
			return this.overlay;
		}

		public ItemStack getDisplay() {
			switch(this.overlay) {
			case NONE: return this.output.get(0).stack.copy();
			case CONSTRUCTION: return this.output.get(0).stack.copy();
			case SMITHING: return this.output.get(0).stack.copy();
			case RECYCLING:
				for(AStack stack : this.input) {
					if(stack instanceof ComparableStack comp)
						return comp.toStack();
				}
				return this.output.get(0).stack.copy();
			default: return new ItemStack(Items.IRON_PICKAXE);
			}
		}
	}

	public static class AnvilOutput {
		public ItemStack stack;
		public float chance;

		public AnvilOutput(ItemStack stack) {
			this(stack, 1F);
		}

		public AnvilOutput(ItemStack stack, float chance) {
			this.stack = stack;
			this.chance = chance;
		}
	}

	public static enum OverlayType {
		NONE,
		CONSTRUCTION,
		RECYCLING,
		SMITHING;
	}
}
