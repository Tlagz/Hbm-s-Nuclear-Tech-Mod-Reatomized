package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineBrickFurnace;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFurnaceBrick;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Bricked furnace */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class FurnaceBrickGameTests {

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void brickFurnaceSmeltsClayFast(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_furnace_brick_off.get().defaultBlockState());
		TileEntityFurnaceBrick furnace = (TileEntityFurnaceBrick) helper.getLevel().getBlockEntity(pos);

		furnace.setItem(0, new ItemStack(Items.CLAY_BALL, 4));
		furnace.setItem(1, new ItemStack(Items.COAL, 1));

		helper.runAfterDelay(55, () -> {
			// clay smelts 4 times as fast: 200 progress at 4 per tick
			helper.assertTrue(furnace.getItem(2).is(Items.BRICK) && furnace.getItem(2).getCount() == 1, "a brick after 50 ticks, got " + furnace.getItem(2));
			helper.assertTrue(helper.getLevel().getBlockState(pos).getValue(MachineBrickFurnace.LIT), "the furnace is lit while burning");
			helper.succeed();
		});
	}
}
