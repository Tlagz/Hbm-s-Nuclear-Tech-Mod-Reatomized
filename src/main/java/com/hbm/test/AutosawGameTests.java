package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineAutosaw;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Automatic buzz saw: fells trees, replants them, cuts plants, can be suspended */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class AutosawGameTests {

	private static final BlockPos SAW = new BlockPos(8, 1, 8);

	private static TileEntityMachineAutosaw place(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(SAW);
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_autosaw.get().defaultBlockState());
		TileEntityMachineAutosaw saw = (TileEntityMachineAutosaw) helper.getLevel().getBlockEntity(pos);
		saw.tank.setTankType(Fluids.WOODOIL);
		saw.tank.setFill(100);
		return saw;
	}

	/** A small birch north of the saw, where the arm points at yaw 0 */
	private static void plantTree(GameTestHelper helper, int x, int z) {
		helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
		for(int y = 2; y <= 5; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.BIRCH_LOG);
		BlockState leaves = Blocks.BIRCH_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
		for(int dx = -1; dx <= 1; dx++) for(int dz = -1; dz <= 1; dz++) {
			if(dx != 0 || dz != 0) helper.setBlock(new BlockPos(x + dx, 5, z + dz), leaves);
		}
		helper.setBlock(new BlockPos(x, 6, z), leaves);
	}

	@GameTest(template = "empty_16x8x16", timeoutTicks = 100)
	public static void autosawFellsAndReplantsTrees(GameTestHelper helper) {
		plantTree(helper, 8, 3);
		helper.setBlock(new BlockPos(9, 2, 5), Blocks.POPPY);
		helper.setBlock(new BlockPos(8, 1, 5), Blocks.DIRT);
		place(helper);

		helper.runAfterDelay(60, () -> {
			helper.assertBlockPresent(Blocks.BIRCH_SAPLING, new BlockPos(8, 2, 3));
			for(int y = 3; y <= 5; y++) helper.assertBlockNotPresent(Blocks.BIRCH_LOG, new BlockPos(8, y, 3));
			helper.assertBlockNotPresent(Blocks.BIRCH_LEAVES, new BlockPos(8, 6, 3));
			helper.assertBlockNotPresent(Blocks.BIRCH_LEAVES, new BlockPos(9, 5, 3));
			helper.assertItemEntityPresent(net.minecraft.world.item.Items.BIRCH_LOG, new BlockPos(8, 3, 3), 3);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_16x8x16", timeoutTicks = 60)
	public static void autosawSuspendedDoesNothing(GameTestHelper helper) {
		plantTree(helper, 8, 3);
		TileEntityMachineAutosaw saw = place(helper);
		saw.isSuspended = true;

		helper.runAfterDelay(45, () -> {
			helper.assertTrue(saw.tank.getFill() == 100 && !saw.isOn, "a suspended saw burns no fuel");
			helper.assertBlockPresent(Blocks.BIRCH_LOG, new BlockPos(8, 2, 3));
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void autosawReplantsTheRightSapling(GameTestHelper helper) {
		helper.assertTrue(TileEntityMachineAutosaw.getSapling(Blocks.SPRUCE_LOG.defaultBlockState()).is(Blocks.SPRUCE_SAPLING), "spruce logs give spruce saplings");
		helper.assertTrue(TileEntityMachineAutosaw.getSapling(Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()).is(Blocks.DARK_OAK_SAPLING), "stripped dark oak gives dark oak saplings");
		helper.assertTrue(TileEntityMachineAutosaw.getSapling(Blocks.CRIMSON_STEM.defaultBlockState()).is(Blocks.OAK_SAPLING), "anything unknown falls back to oak");
		helper.succeed();
	}
}
