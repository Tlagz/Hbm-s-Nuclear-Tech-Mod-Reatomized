package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFurnaceIron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Iron furnace (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class FurnaceIronGameTests {

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void ironFurnaceSmelts(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.furnace_iron.get().defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityFurnaceIron furnace = (TileEntityFurnaceIron) helper.getLevel().getBlockEntity(core);

		furnace.setItem(0, new ItemStack(Items.RAW_IRON, 2));
		furnace.setItem(2, new ItemStack(Items.COAL, 1));

		helper.runAfterDelay(2, () -> {
			// coal burns 25% longer in the iron furnace
			helper.assertTrue(furnace.maxBurnTime == 2000 && furnace.getItem(2).isEmpty(), "the coal is lit with 2000 ticks, got " + furnace.maxBurnTime);
		});
		helper.runAfterDelay(165, () -> {
			helper.assertTrue(furnace.getItem(3).is(Items.IRON_INGOT) && furnace.getItem(3).getCount() == 1 && furnace.getItem(0).getCount() == 1, "one ingot after 160 ticks, got " + furnace.getItem(3));
			helper.assertTrue(furnace.burnTime <= 2000 - 160, "burn time only runs while smelting");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void ironFurnaceKeepsFuelWhenIdle(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.furnace_iron.get().defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityFurnaceIron furnace = (TileEntityFurnaceIron) helper.getLevel().getBlockEntity(core);
		furnace.setItem(1, new ItemStack(Items.COAL, 1));

		helper.runAfterDelay(20, () -> {
			helper.assertTrue(furnace.burnTime == furnace.maxBurnTime && furnace.burnTime > 0, "a lit fuel item doesn't burn down without anything to smelt");
			helper.succeed();
		});
	}
}
