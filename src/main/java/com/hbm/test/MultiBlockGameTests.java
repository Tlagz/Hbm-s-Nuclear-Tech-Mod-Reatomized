package com.hbm.test;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockEnums.EnumStoneType;
import com.hbm.blocks.BlockEnums.LightstoneType;
import com.hbm.blocks.BlockEnums.TileType;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockNTMSand.EnumSandType;
import com.hbm.inventory.OreDictManager;
import com.hbm.items.ItemEnums.EnumCokeType;
import com.hbm.lib.RefStrings;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Blocks of the original's BlockEnumMulti, one block per former metadata value */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class MultiBlockGameTests {

	private static ItemStack craft(GameTestHelper helper, int width, int height, ItemStack... grid) {
		List<ItemStack> items = new ArrayList<>(List.of(grid));
		CraftingInput input = CraftingInput.of(width, height, items);
		return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
				.map(r -> r.value().assemble(input, helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
	}

	@GameTest(template = "empty_8x4x8")
	public static void variantsNamesRecipesAndTags(GameTestHelper helper) {
		helper.assertTrue(ModBlocks.lightstone.get(LightstoneType.TILE).get().getDescriptionId().equals("block.hbm.lightstone.tile"), "variants keep the original translation keys");
		helper.assertTrue(ModBlocks.sand_mix.get(EnumSandType.BORON).get().getDescriptionId().equals("block.hbm.sand_boron"), "mixed sands use the original's sand_ names");

		ItemStack large = ModBlocks.vinyl_tile.stack(TileType.LARGE);
		ItemStack small = craft(helper, 2, 2, large.copy(), large.copy(), large.copy(), large.copy());
		helper.assertTrue(small.is(ModBlocks.vinyl_tile.get(TileType.SMALL).get().asItem()) && small.getCount() == 4, "4 large vinyl tiles make 4 small ones, got " + small);
		// the plain block in the original recipe matched any metadata
		ItemStack chiseled = craft(helper, 1, 1, ModBlocks.lightstone.stack(LightstoneType.UNREFINED));
		helper.assertTrue(chiseled.is(ModBlocks.lightstone.get(LightstoneType.CHISELED).get().asItem()), "any lightstone can be chiseled, got " + chiseled);

		helper.assertTrue(ModBlocks.block_coke.stack(EnumCokeType.COAL).getBurnTime(RecipeType.SMELTING) == 200 * 160, "coke blocks burn 160 items long");
		helper.assertTrue(ModBlocks.stone_resource.stack(EnumStoneType.MALACHITE).is(OreDictManager.tag("oreMalachite")), "malachite stone is malachite ore");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void mixedSandFalls(GameTestHelper helper) {
		BlockPos floor = helper.absolutePos(new BlockPos(2, 1, 2));
		helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
		helper.getLevel().setBlockAndUpdate(floor.above(2), ModBlocks.sand_mix.get(EnumSandType.LEAD).get().defaultBlockState());
		helper.succeedWhen(() -> helper.assertTrue(helper.getLevel().getBlockState(floor.above()).is(ModBlocks.sand_mix.get(EnumSandType.LEAD).get()), "mixed sand should fall onto the stone"));
	}
}
