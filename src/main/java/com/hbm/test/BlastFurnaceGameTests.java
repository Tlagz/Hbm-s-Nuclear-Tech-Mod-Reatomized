package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.BlastFurnaceRecipesNT;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineBlastFurnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Steel: the blast furnace, and the furnace recipes translated from the original's SmeltingRecipes */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class BlastFurnaceGameTests {

	@GameTest(template = "empty_8x12x8", timeoutTicks = 300)
	public static void blastFurnaceMakesSteelAndSlag(GameTestHelper helper) {
		helper.assertTrue(BlastFurnaceRecipesNT.INSTANCE.recipeOrderedList.size() > 10, "the translated recipes should be registered");

		BlockPos core = ModBlocks.machine_blast_furnace.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 3)), Direction.NORTH);
		TileEntityMachineBlastFurnace furnace = (TileEntityMachineBlastFurnace) helper.getLevel().getBlockEntity(core);

		furnace.setItem(0, new ItemStack(Items.COAL, 4));
		// iron and sand go into either input slot
		furnace.setItem(1, new ItemStack(Items.SAND));
		furnace.setItem(2, new ItemStack(Items.IRON_INGOT, 2));
		// a full air blast tank makes it 5 times faster (800 -> 160 ticks), it drains 5% per tick without a compressor
		helper.onEachTick(() -> furnace.tanks[0].setFill(furnace.tanks[0].getMaxFill()));

		helper.succeedWhen(() -> {
			helper.assertTrue(furnace.speed == 5F || furnace.getItem(3).getCount() == 2, "the air blast should speed it up to 500%, is " + furnace.speed);
			helper.assertTrue(furnace.getItem(3).is(ModItems.ingot_steel.get()) && furnace.getItem(3).getCount() == 2, "2 iron and sand should make 2 steel, got " + furnace.getItem(3));
			helper.assertTrue(furnace.getItem(4).is(ModItems.ingot_raw.get(Mats.MAT_SLAG).get()), "and a slag ingot, got " + furnace.getItem(4));
			helper.assertTrue(furnace.tanks[1].getFill() > 0, "burning makes flue gas");
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void oresSmeltIntoIngots(GameTestHelper helper) {
		SingleRecipeInput input = new SingleRecipeInput(new ItemStack(ModBlocks.ore_uranium.get()));
		ItemStack result = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, helper.getLevel())
				.map(r -> r.value().assemble(input, helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
		helper.assertTrue(result.is(ModItems.ingot_uranium.get()), "uranium ore should smelt into a uranium ingot, got " + result);
		helper.succeed();
	}
}
