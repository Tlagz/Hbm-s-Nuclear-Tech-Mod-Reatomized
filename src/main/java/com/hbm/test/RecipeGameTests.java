package com.hbm.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The crafting recipes translated from the original (tools/gen_recipes.py) load and craft what they should */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class RecipeGameTests {

	private static ItemStack craft(GameTestHelper helper, ItemLike... grid) {
		List<ItemStack> items = new ArrayList<>();
		for(ItemLike like : grid) items.add(like == null ? ItemStack.EMPTY : new ItemStack(like));
		CraftingInput input = CraftingInput.of(3, 3, items);
		Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
		return recipe.map(r -> r.value().assemble(input, helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
	}

	@GameTest(template = "empty_8x4x8")
	public static void translatedRecipesCraft(GameTestHelper helper) {
		long loaded = helper.getLevel().getRecipeManager().getRecipes().stream().filter(r -> r.id().getNamespace().equals(RefStrings.MODID)).count();
		MainRegistry.logger.info("Loaded hbm recipes: " + loaded);
		helper.assertTrue(loaded > 300, "most translated recipes should load, only " + loaded);

		// storage blocks, a plain item recipe from the MineralRecipes helpers
		var u = ModItems.ingot_uranium.get();
		ItemStack block = craft(helper, u, u, u, u, u, u, u, u, u);
		helper.assertTrue(block.is(ModBlocks.block_uranium.get().asItem()), "9 uranium ingots should make a uranium block, got " + block);
		ItemStack back = craft(helper, ModBlocks.block_uranium.get(), null, null, null, null, null, null, null, null);
		helper.assertTrue(back.is(u) && back.getCount() == 9, "a uranium block should give 9 ingots, got " + back);

		// tag ingredients: steel plates (c:plates/steel) and any glass pane (c:glass_panes)
		var steel = ModItems.plate_steel.get();
		ItemStack cells = craft(helper, null, steel, null, Items.GLASS_PANE, null, Items.GLASS_PANE, null, steel, null);
		helper.assertTrue(cells.is(ModItems.cell_empty.get()) && cells.getCount() == 6, "steel plates and panes should make 6 empty cells, got " + cells);
		ItemStack tinted = craft(helper, null, steel, null, Items.RED_STAINED_GLASS_PANE, null, Items.GLASS_PANE, null, steel, null);
		helper.assertTrue(tinted.is(ModItems.cell_empty.get()), "any glass pane should work (the original's paneGlass), got " + tinted);

		helper.succeed();
	}
}
