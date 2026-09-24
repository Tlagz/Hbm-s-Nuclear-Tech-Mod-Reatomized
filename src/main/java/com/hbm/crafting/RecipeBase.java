package com.hbm.crafting;

import static com.hbm.inventory.OreDictManager.KEY_STICK;

import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.items.ModItems;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * The recipe functions the generated recipe classes (com.hbm.crafting.gen, tools/gen_recipes.py) call:
 * the original's CraftingManager.addRecipeAuto/addShapelessAuto and the helpers of MineralRecipes,
 * ToolRecipes, ArmorRecipes and RodRecipes. They write into the RecipeSink of the running datagen.
 */
public class RecipeBase {

	public static RecipeSink sink;

	public static void addRecipeAuto(ItemStack result, Object... ins) { sink.addRecipeAuto(result, ins); }

	/** new ItemStack(item, count) without the ItemLike/Holder overload ambiguity of DeferredItem */
	public static ItemStack stack(ItemLike item, int count) { return new ItemStack(item, count); }
	public static void addShapelessAuto(ItemStack result, Object... ins) { sink.addShapelessAuto(result, ins); }

	/// MineralRecipes ///

	public static void add1To9Pair(ItemLike one, ItemLike nine) {
		add1To9(new ItemStack(one), new ItemStack(nine, 9));
		add9To1(new ItemStack(nine), new ItemStack(one));
	}
	public static void add1To9Pair(ItemStack one, ItemStack nine) {
		add1To9(one, nine);
		add9To1(nine, one);
	}
	public static void add1To9(ItemLike one, ItemLike nine) {
		add1To9(new ItemStack(one), new ItemStack(nine, 9));
	}
	public static void add1To9(ItemStack one, ItemStack nine) {
		addRecipeAuto(nine, "#", '#', one);
	}
	public static void add9To1(ItemLike nine, ItemLike one) {
		add9To1(new ItemStack(nine), new ItemStack(one));
	}
	public static void add9To1(ItemStack nine, ItemStack one) {
		addRecipeAuto(one, "###", "###", "###", '#', nine);
	}
	/** Full set of nugget, ingot and block */
	public static void addMineralSet(ItemLike nugget, ItemLike ingot, ItemLike block) {
		add1To9(new ItemStack(ingot), new ItemStack(nugget, 9));
		add9To1(new ItemStack(nugget), new ItemStack(ingot));
		add1To9(new ItemStack(block), new ItemStack(ingot, 9));
		add9To1(new ItemStack(ingot), new ItemStack(block));
	}
	public static void addBillet(ItemLike billet, ItemLike nugget, String... ore) {
		for(String o : ore) addRecipeAuto(new ItemStack(billet), "###", "###", '#', o);
		addBillet(billet, nugget);
	}
	public static void addBillet(ItemLike billet, ItemLike ingot, ItemLike nugget, String... ore) {
		for(String o : ore) addRecipeAuto(new ItemStack(billet), "###", "###", '#', o);
		addBillet(billet, ingot, nugget);
	}
	public static void addBilletFragment(ItemStack billet, ItemStack nugget) {
		addRecipeAuto(billet.copy(), "###", "###", '#', nugget);
	}
	public static void addBillet(ItemLike billet, ItemLike nugget) {
		addRecipeAuto(new ItemStack(billet), "###", "###", '#', nugget);
		addShapelessAuto(new ItemStack(nugget, 6), billet);
	}
	public static void addBillet(ItemLike billet, ItemLike ingot, ItemLike nugget) {
		addRecipeAuto(new ItemStack(billet), "###", "###", '#', nugget);
		addShapelessAuto(new ItemStack(nugget, 6), billet);
		addBilletToIngot(billet, ingot);
	}
	public static void addBilletToIngot(ItemLike billet, ItemLike ingot) {
		addShapelessAuto(new ItemStack(ingot, 2), billet, billet, billet);
		addRecipeAuto(new ItemStack(billet, 3), "##", '#', ingot);
	}

	/// ToolRecipes ///

	public static final String[] patternSword = new String[] {"X", "X", "#"};
	public static final String[] patternPick = new String[] {"XXX", " # ", " # "};
	public static final String[] patternAxe = new String[] {"XX", "X#", " #"};
	public static final String[] patternShovel = new String[] {"X", "#", "#"};
	public static final String[] patternHoe = new String[] {"XX", " #", " #"};

	public static void addSword(Object ingot, ItemLike sword) { addTool(ingot, sword, patternSword); }
	public static void addPickaxe(Object ingot, ItemLike pick) { addTool(ingot, pick, patternPick); }
	public static void addAxe(Object ingot, ItemLike axe) { addTool(ingot, axe, patternAxe); }
	public static void addShovel(Object ingot, ItemLike shovel) { addTool(ingot, shovel, patternShovel); }
	public static void addHoe(Object ingot, ItemLike hoe) { addTool(ingot, hoe, patternHoe); }

	public static void addTool(Object ingot, ItemLike tool, String[] pattern) {
		addRecipeAuto(new ItemStack(tool), pattern, 'X', ingot, '#', KEY_STICK);
	}

	/// ArmorRecipes ///

	public static final String[] patternHelmet = new String[] {"XXX", "X X"};
	public static final String[] patternChetplate = new String[] {"X X", "XXX", "XXX"};
	public static final String[] patternLeggings = new String[] {"XXX", "X X", "X X"};
	public static final String[] patternBoots = new String[] {"X X", "X X"};

	public static void addHelmet(Object ingot, ItemLike armor) { addArmor(ingot, armor, patternHelmet); }
	public static void addChest(Object ingot, ItemLike armor) { addArmor(ingot, armor, patternChetplate); }
	public static void addLegs(Object ingot, ItemLike armor) { addArmor(ingot, armor, patternLeggings); }
	public static void addBoots(Object ingot, ItemLike armor) { addArmor(ingot, armor, patternBoots); }

	public static void addArmor(Object ingot, ItemLike armor, String[] pattern) {
		addRecipeAuto(new ItemStack(armor), pattern, 'X', ingot);
	}

	/// RodRecipes ///

	/** Fill rods with one billet. For fuels only, therefore no unloading or ore dict */
	public static void addFuelRodBillet(ItemLike billet, ItemLike out) {
		addShapelessAuto(new ItemStack(out), ModItems.rod_empty, billet);
	}
	public static void addDualFuelRodBillet(ItemLike billet, ItemLike out) {
		addShapelessAuto(new ItemStack(out), ModItems.rod_dual_empty, billet, billet);
	}
	public static void addQuadFuelRodBillet(ItemLike billet, ItemLike out) {
		addShapelessAuto(new ItemStack(out), ModItems.rod_quad_empty, billet, billet, billet, billet);
	}
	public static void addRodBilletUnload(ItemLike billet, ItemLike out) {
		addShapelessAuto(new ItemStack(out), ModItems.rod_empty, billet);
		addShapelessAuto(new ItemStack(billet, 1), out);
	}
	public static void addRodBilletUnload(DictFrame mat, ItemLike billet, ItemLike out) {
		addShapelessAuto(new ItemStack(out), ModItems.rod_empty, mat.billet());
		addShapelessAuto(new ItemStack(billet, 1), out);
	}
	public static void addDualRodBilletUnload(ItemLike billet, ItemLike out) {
		addShapelessAuto(new ItemStack(out), ModItems.rod_dual_empty, billet, billet);
		addShapelessAuto(new ItemStack(billet, 2), out);
	}
	public static void addDualRodBilletUnload(DictFrame mat, ItemLike billet, ItemLike out) {
		addShapelessAuto(new ItemStack(out), ModItems.rod_dual_empty, mat.billet(), mat.billet());
		addShapelessAuto(new ItemStack(billet, 2), out);
	}
	public static void addQuadRodBilletUnload(ItemLike billet, ItemLike out) {
		addShapelessAuto(new ItemStack(out), ModItems.rod_quad_empty, billet, billet, billet, billet);
		addShapelessAuto(new ItemStack(billet, 4), out);
	}
	public static void addQuadRodBilletUnload(DictFrame mat, ItemLike billet, ItemLike out) {
		addShapelessAuto(new ItemStack(out), ModItems.rod_quad_empty, mat.billet(), mat.billet(), mat.billet(), mat.billet());
		addShapelessAuto(new ItemStack(billet, 4), out);
	}
}
