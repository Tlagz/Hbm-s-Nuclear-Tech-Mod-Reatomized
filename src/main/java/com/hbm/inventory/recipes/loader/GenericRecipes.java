package com.hbm.inventory.recipes.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

/**
 * Fully genericized recipes.
 * Features:
 * * Fluid in and output
 * * AStack intput
 * * Chance-based outputs, for selecting items and for selecting items are produced in the first place
 * * Duration
 * * Tags for identification
 *
 * TODO the JSON recipe config (SerializableRecipe: readRecipe/writeRecipe)
 *
 * @author hbm
 */
public abstract class GenericRecipes<T extends GenericRecipe> {

	public static final Random RNG = new Random();

	/** Alternate recipes, i.e. obtainable otherwise */
	public static final String POOL_PREFIX_ALT = "alt.";
	/** Discoverable recipes, i.e. not obtainable otherwise */
	public static final String POOL_PREFIX_DISCOVER = "discover.";
	/** Secret recipes, self-explantory. Why even have this comment? */
	public static final String POOL_PREFIX_SECRET = "secret.";
	/** 528 greyprints */
	public static final String POOL_PREFIX_528 = "528.";

	public List<T> recipeOrderedList = new ArrayList<>();
	public HashMap<String, T> recipeNameMap = new HashMap<>();

	/** Blueprint pool name to list of recipe names that are part of this pool */
	public static HashMap<String, List<String>> blueprintPools = new HashMap<>();
	/** Name to recipe map for all recipes that are part of pools for lookup */
	public static HashMap<String, GenericRecipe> nameToRecipeGlobal = new HashMap<>();

	/** Groups for auto switch functionality (changes recipe automatically based on first solid input) */
	public HashMap<String, List<GenericRecipe>> autoSwitchGroups = new HashMap<>();

	public abstract int inputItemLimit();
	public abstract int inputFluidLimit();
	public abstract int outputItemLimit();
	public abstract int outputFluidLimit();
	public boolean hasDuration() { return true; }
	public boolean hasPower() { return true; }

	public abstract void registerDefaults();

	/** Clears and registers the default recipes, the original's SerializableRecipe.initialize without the config file */
	public void initialize() {
		this.deleteRecipes();
		this.registerDefaults();
	}

	/** Adds a recipe to a blueprint pool (i.e. a blueprint item's recipe list) */
	public static void addToPool(String pool, GenericRecipe recipe) {
		blueprintPools.computeIfAbsent(pool, k -> new ArrayList<>()).add(recipe.name);
	}

	/** Adds a recipe to an auto switch group (recipe can switch based on first solid input) */
	public void addToGroup(String group, GenericRecipe recipe) {
		autoSwitchGroups.computeIfAbsent(group, k -> new ArrayList<>()).add(recipe);
	}

	public static void clearPools() {
		blueprintPools.clear();
		nameToRecipeGlobal.clear();
	}

	public void deleteRecipes() {
		this.recipeOrderedList.clear();
		this.recipeNameMap.clear();
		this.autoSwitchGroups.clear();
	}

	public void register(T recipe) {
		this.recipeOrderedList.add(recipe);
		if(recipeNameMap.containsKey(recipe.name)) throw new IllegalStateException("Recipe " + recipe.name + " has been registered with a duplicate ID!");
		this.recipeNameMap.put(recipe.name, recipe);
		nameToRecipeGlobal.put(recipe.name, recipe);
	}

	public abstract T instantiateRecipe(String name);

	///////////////
	/// CLASSES ///
	///////////////
	public static interface IOutput {
		/** true for ChanceOutputMulti with a poolsize >1 */
		public boolean possibleMultiOutput();
		/** Decides an output, returns a copy of the held result, empty if nothing is produced */
		public ItemStack collapse();
		/** Returns an itemstack only if possibleMultiOutput is false, null otherwise */
		public ItemStack getSingle();
		public ItemStack[] getAllPossibilities();
		public String[] getLabel();
	}

	/** A chance output, produces either an ItemStack or nothing */
	public static class ChanceOutput implements IOutput {

		// a weight of 0 means this output is not part of a weighted output

		public ItemStack stack;
		public float chance = 1F;
		public int itemWeight;

		public ChanceOutput(ItemStack stack) { this(stack, 1F, 0); }
		public ChanceOutput(ItemStack stack, int weight) { this(stack, 1F, weight); }
		public ChanceOutput(ItemStack stack, float chance) { this(stack, chance, 0); }
		public ChanceOutput(ItemStack stack, float chance, int weight) {
			this.itemWeight = weight;
			this.stack = stack;
			this.chance = chance;
		}

		@Override
		public ItemStack collapse() {
			if(this.chance >= 1F) return getSingle();
			int finalSize = 0;
			for(int i = 0; i < this.stack.getCount(); i++) if(RNG.nextFloat() <= chance) finalSize++;
			if(finalSize <= 0) return ItemStack.EMPTY;
			return this.stack.copyWithCount(finalSize);
		}

		@Override public ItemStack getSingle() { return this.stack.copy(); }
		@Override public boolean possibleMultiOutput() { return false; }
		@Override public ItemStack[] getAllPossibilities() { return new ItemStack[] {getSingle()}; }

		@Override
		public String[] getLabel() {
			return new String[] {ChatFormatting.GRAY + "" + this.stack.getCount() + "x " + this.stack.getHoverName().getString() + (this.chance >= 1 ? "" : " (" + (int)(this.chance * 1000) / 10F + "%)")};
		}
	}

	/** Multiple choice chance output, produces a ChanceOutput chosen randomly by weight */
	public static class ChanceOutputMulti implements IOutput {

		public List<ChanceOutput> pool = new ArrayList<>();

		public ChanceOutputMulti(ChanceOutput... out) {
			for(ChanceOutput output : out) pool.add(output);
		}

		private int totalWeight() {
			int total = 0;
			for(ChanceOutput out : pool) total += out.itemWeight;
			return total;
		}

		@Override
		public ItemStack collapse() {
			int total = totalWeight();
			if(total <= 0) return pool.get(0).collapse();
			int pick = RNG.nextInt(total);
			for(ChanceOutput out : pool) {
				pick -= out.itemWeight;
				if(pick < 0) return out.collapse();
			}
			return pool.get(pool.size() - 1).collapse();
		}

		@Override public boolean possibleMultiOutput() { return pool.size() > 1; }
		@Override public ItemStack getSingle() { return possibleMultiOutput() ? null : pool.get(0).getSingle(); }

		@Override public ItemStack[] getAllPossibilities() {
			ItemStack[] outputs = new ItemStack[pool.size()];
			for(int i = 0; i < outputs.length; i++) outputs[i] = pool.get(i).getSingle();
			return outputs;
		}

		@Override
		public String[] getLabel() {
			String[] label = new String[pool.size() + 1];
			label[0] = "One of:";
			int totalWeight = totalWeight();
			for(int i = 1; i < label.length; i++) {
				ChanceOutput output = pool.get(i - 1);
				float chance = (float) output.itemWeight / (float) totalWeight * output.chance;
				label[i] = "  " + ChatFormatting.GRAY + output.stack.getCount() + "x " + output.stack.getHoverName().getString() + " (" + (int)(chance * 1000F) / 10F + "%)";
			}
			return label;
		}
	}
}
