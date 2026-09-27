package com.hbm.inventory.recipes;

import static com.hbm.inventory.OreDictManager.*;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidContainerBase;
import com.hbm.util.Tuple.Triplet;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Alloy furnace (the original's "blast furnace", TileEntityDiFurnace and its RTG variant) recipes: two inputs in any
 * order, one output. Material frames (DictFrame) match the ingot, plate, gem and dust of that material.
 *
 * TODO the meteorite items (starmetal, meteorite sword) once they're ported, recipe config file, IMC, JEI
 */
public class BlastFurnaceRecipes {

	private static final List<Triplet<Object, Object, ItemStack>> blastFurnaceRecipes = new ArrayList<>();

	public static void registerDefaults() {
		blastFurnaceRecipes.clear();

		/* STEEL */
		addRecipe(IRON,			COAL,										new ItemStack(ModItems.ingot_steel.get(), 1));
		addRecipe(IRON,			ANY_COKE,									new ItemStack(ModItems.ingot_steel.get(), 1));
		addRecipe(IRON.ore(),	COAL,										new ItemStack(ModItems.ingot_steel.get(), 2));
		addRecipe(IRON.ore(),	ANY_COKE,									new ItemStack(ModItems.ingot_steel.get(), 3));
		addRecipe(IRON.ore(),	new ComparableStack(ModItems.powder_flux.get()),	new ItemStack(ModItems.ingot_steel.get(), 3));

		addRecipe(CU,									REDSTONE,										new ItemStack(ModItems.ingot_red_copper.get(), 2));
		addRecipe(new ComparableStack(ItemFluidContainerBase.withFluid(ModItems.canister_full, Fluids.GASOLINE)), KEY_SLIME, new ItemStack(ModItems.canister_napalm.get()));
		addRecipe(W,									SA326.nugget(),									new ItemStack(ModItems.ingot_magnetized_tungsten.get()));
		addRecipe(STEEL,								TC99.nugget(),									new ItemStack(ModItems.ingot_tcalloy.get()));
		addRecipe(GOLD.plate(),							ModItems.plate_mixed.get(),						new ItemStack(ModItems.plate_paa.get(), 2));
	}

	public static void addRecipe(Object in1, Object in2, ItemStack out) {
		if(in1 instanceof ItemLike item) in1 = new ComparableStack(item);
		if(in2 instanceof ItemLike item) in2 = new ComparableStack(item);
		blastFurnaceRecipes.add(new Triplet<>(in1, in2, out));
	}

	/** The output for the two inputs in either order, or empty */
	public static ItemStack getOutput(ItemStack in1, ItemStack in2) {
		if(in1.isEmpty() || in2.isEmpty()) return ItemStack.EMPTY;

		for(Triplet<Object, Object, ItemStack> recipe : blastFurnaceRecipes) {
			AStack[] recipeItem1 = getRecipeStacks(recipe.getX());
			AStack[] recipeItem2 = getRecipeStacks(recipe.getY());

			if((doStacksMatch(recipeItem1, in1) && doStacksMatch(recipeItem2, in2)) || (doStacksMatch(recipeItem2, in1) && doStacksMatch(recipeItem1, in2))) {
				return recipe.getZ().copy();
			}
		}
		return ItemStack.EMPTY;
	}

	private static boolean doStacksMatch(AStack[] recipe, ItemStack in) {
		for(AStack stack : recipe) if(stack.matchesRecipe(in, true)) return true;
		return false;
	}

	private static AStack[] getRecipeStacks(Object in) {
		if(in instanceof DictFrame frame) return new AStack[] { new OreDictStack(frame.ingot()), new OreDictStack(frame.plate()), new OreDictStack(frame.gem()), new OreDictStack(frame.dust()) };
		if(in instanceof AStack stack) return new AStack[] { stack };
		if(in instanceof String dict) return new AStack[] { new OreDictStack(dict) };
		return new AStack[0];
	}

	public static List<Triplet<AStack[], AStack[], ItemStack>> getRecipes() {
		List<Triplet<AStack[], AStack[], ItemStack>> subRecipes = new ArrayList<>();
		for(Triplet<Object, Object, ItemStack> recipe : blastFurnaceRecipes) {
			subRecipes.add(new Triplet<>(getRecipeStacks(recipe.getX()), getRecipeStacks(recipe.getY()), recipe.getZ()));
		}
		return subRecipes;
	}
}
