package com.hbm.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.hbm.inventory.OreDictManager;
import com.hbm.lib.RefStrings;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

/**
 * The original's CraftingManager.addRecipeAuto / addShapelessAuto, writing 1.21 recipes during datagen.
 * Ingredients are what the original passed: ore dict keys (Strings, turned into tags), items, blocks and stacks.
 * Every tag ingredient adds a "tag not empty" condition, so recipes for materials that aren't ported yet
 * are skipped when the recipes load instead of failing.
 */
public class RecipeSink {

	private final RecipeOutput output;
	private final Map<String, Integer> names = new HashMap<>();
	public int shaped = 0;
	public int shapeless = 0;
	public int smelting = 0;

	public RecipeSink(RecipeOutput output) {
		this.output = output;
	}

	/** addRecipeAuto(result, "rows"..., 'c', ingredient, ...) */
	public void addRecipeAuto(ItemStack result, Object... ins) {
		List<String> rows = new ArrayList<>();
		Map<Character, Ingredient> key = new LinkedHashMap<>();
		Set<ICondition> conditions = new LinkedHashSet<>();

		int i = 0;
		while(i < ins.length && (ins[i] instanceof String || ins[i] instanceof String[])) {
			if(ins[i] instanceof String[] array) for(String row : array) rows.add(row);
			else rows.add((String) ins[i]);
			i++;
		}

		for(; i + 1 < ins.length; i += 2) {
			char c = (Character) ins[i];
			key.put(c, ingredient(ins[i + 1], conditions));
		}

		// 1.7.10 allowed rows of different lengths, 1.21 patterns are rectangular
		int width = rows.stream().mapToInt(String::length).max().orElse(0);
		List<String> pattern = rows.stream().map(row -> String.format("%-" + width + "s", row)).toList();

		ShapedRecipe recipe = new ShapedRecipe("", CraftingBookCategory.MISC, ShapedRecipePattern.of(key, pattern), result.copy());
		output.accept(id(result), recipe, null, conditions.toArray(new ICondition[0]));
		shaped++;
	}

	/** addShapelessAuto(result, ingredients...) */
	public void addShapelessAuto(ItemStack result, Object... ins) {
		NonNullList<Ingredient> ingredients = NonNullList.create();
		Set<ICondition> conditions = new LinkedHashSet<>();
		for(Object in : ins) ingredients.add(ingredient(in, conditions));

		ShapelessRecipe recipe = new ShapelessRecipe("", CraftingBookCategory.MISC, result.copy(), ingredients);
		output.accept(id(result), recipe, null, conditions.toArray(new ICondition[0]));
		shapeless++;
	}

	/** GameRegistry.addSmelting(input, result, xp): a furnace recipe with the vanilla cooking time */
	public void addSmelting(Object in, ItemStack result, float xp) {
		Set<ICondition> conditions = new LinkedHashSet<>();
		SmeltingRecipe recipe = new SmeltingRecipe("", CookingBookCategory.MISC, ingredient(in, conditions), result.copy(), xp, 200);
		output.accept(id("smelting/", result), recipe, null, conditions.toArray(new ICondition[0]));
		smelting++;
	}

	private Ingredient ingredient(Object o, Set<ICondition> conditions) {
		if(o instanceof String key) {
			TagKey<Item> tag = OreDictManager.tag(key);
			conditions.add(new NotCondition(new TagEmptyCondition(tag)));
			return Ingredient.of(tag);
		}
		if(o instanceof TagKey<?> tag) {
			@SuppressWarnings("unchecked") TagKey<Item> itemTag = (TagKey<Item>) tag;
			conditions.add(new NotCondition(new TagEmptyCondition(itemTag)));
			return Ingredient.of(itemTag);
		}
		if(o instanceof ItemStack stack) {
			// components (e.g. the fluid of a canister) must match, the count doesn't matter
			if(!stack.getComponentsPatch().isEmpty()) return DataComponentIngredient.of(false, stack);
			return Ingredient.of(stack.getItem());
		}
		if(o instanceof Ingredient ingredient) return ingredient; // e.g. any variant of a multi block
		if(o instanceof ItemLike like) return Ingredient.of(like);
		if(o instanceof Supplier<?> supplier && supplier.get() instanceof ItemLike like) return Ingredient.of(like);
		throw new IllegalArgumentException("Unknown recipe ingredient " + o);
	}

	/** hbm:[output]_[n], numbered in registration order since many outputs have several recipes */
	private ResourceLocation id(ItemStack result) {
		return id("", result);
	}

	private ResourceLocation id(String prefix, ItemStack result) {
		String path = prefix + BuiltInRegistries.ITEM.getKey(result.getItem()).getPath();
		int n = names.merge(path, 1, Integer::sum);
		return RefStrings.loc(n == 1 ? path : path + "_" + n);
	}
}
