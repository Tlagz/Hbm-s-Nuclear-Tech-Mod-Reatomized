package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineFunnel;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The combinator funnel compresses with 3x3 and 2x2 crafting recipes */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class FunnelGameTests {

	private static TileEntityMachineFunnel place(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_funnel.get().defaultBlockState());
		return (TileEntityMachineFunnel) helper.getLevel().getBlockEntity(pos);
	}

	@GameTest(template = "empty_8x4x8")
	public static void funnelCompresses(GameTestHelper helper) {
		TileEntityMachineFunnel funnel = place(helper);
		funnel.setItem(0, new ItemStack(Items.IRON_INGOT, 18));
		funnel.setItem(1, new ItemStack(Items.QUARTZ, 4));
		funnel.setItem(2, new ItemStack(Items.STICK, 9));

		helper.runAfterDelay(3, () -> {
			helper.assertTrue(funnel.getItem(9).is(Items.IRON_BLOCK) && funnel.getItem(9).getCount() == 2 && funnel.getItem(0).isEmpty(), "18 iron ingots become 2 iron blocks, got " + funnel.getItem(9));
			helper.assertTrue(funnel.getItem(10).is(Items.QUARTZ_BLOCK), "4 quartz become a quartz block (2x2), got " + funnel.getItem(10));
			helper.assertTrue(funnel.getItem(11).isEmpty() && funnel.getItem(2).getCount() == 9, "sticks have no compressed form");
			helper.assertFalse(funnel.canPlaceItem(3, new ItemStack(Items.STICK)), "sticks aren't accepted");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void funnel3x3OnlyMode(GameTestHelper helper) {
		TileEntityMachineFunnel funnel = place(helper);
		funnel.mode = TileEntityMachineFunnel.MODE_3x3;
		funnel.setItem(0, new ItemStack(Items.QUARTZ, 4));

		helper.runAfterDelay(3, () -> {
			helper.assertTrue(funnel.getItem(9).isEmpty() && funnel.getItem(0).getCount() == 4, "quartz only has a 2x2 recipe, nothing happens in 3x3 mode");
			helper.succeed();
		});
	}
}
