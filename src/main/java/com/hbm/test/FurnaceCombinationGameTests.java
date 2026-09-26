package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.CombinationRecipes;
import com.hbm.items.ItemEnums.EnumCokeType;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;
import com.hbm.util.Tuple.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Combination furnace (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class FurnaceCombinationGameTests {

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void combinationFurnaceCokesCoal(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.furnace_combination.get().defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityFurnaceCombination furnace = (TileEntityFurnaceCombination) helper.getLevel().getBlockEntity(core);

		furnace.setItem(0, new ItemStack(Items.COAL, 2));
		furnace.heat = TileEntityFurnaceCombination.maxHeat;

		helper.runAfterDelay(60, () -> {
			// 20,000 TU per item, a hundredth of the stored heat per tick
			helper.assertTrue(furnace.getItem(1).is(ModItems.coke.get(EnumCokeType.COAL).get()), "coal coke, got " + furnace.getItem(1));
			helper.assertTrue(furnace.tank.getTankType() == Fluids.COALCREOSOTE && furnace.tank.getFill() == 100 * furnace.getItem(1).getCount(), "100mB creosote per coke, got " + furnace.tank.getFill());
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void combinationRecipeLookup(GameTestHelper helper) {
		Pair<ItemStack, FluidStack> log = CombinationRecipes.getOutput(new ItemStack(Items.BIRCH_LOG));
		helper.assertTrue(log != null && log.getKey().is(Items.CHARCOAL) && log.getValue().type == Fluids.WOODOIL, "logs (tag) into charcoal and wood oil");
		Pair<ItemStack, FluidStack> clay = CombinationRecipes.getOutput(new ItemStack(Items.CLAY));
		helper.assertTrue(clay != null && clay.getKey().is(Items.BRICKS) && clay.getValue() == null, "clay blocks into bricks without fluid");
		helper.assertTrue(CombinationRecipes.getOutput(new ItemStack(Items.DIRT)) == null, "dirt does nothing");
		helper.succeed();
	}
}
