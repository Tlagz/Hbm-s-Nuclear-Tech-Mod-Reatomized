package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineThresher;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineThresher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Thresher: harvests and replants ripe crops in front of it, leaves unripe ones, shreds monsters */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ThresherGameTests {

	private static final BlockPos THRESHER = new BlockPos(8, 1, 14);

	private static TileEntityMachineThresher place(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(THRESHER);
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_thresher.get().defaultBlockState().setValue(MachineThresher.FACING, Direction.NORTH));
		TileEntityMachineThresher thresher = (TileEntityMachineThresher) helper.getLevel().getBlockEntity(pos);
		thresher.tank.setTankType(Fluids.WOODOIL);
		thresher.tank.setFill(100);
		return thresher;
	}

	@GameTest(template = "empty_16x8x16", timeoutTicks = 100)
	public static void thresherHarvestsAndReplants(GameTestHelper helper) {
		// a row of wheat 6 blocks north: ripe on the left, unripe on the right
		for(int x = 6; x <= 10; x++) {
			helper.setBlock(new BlockPos(x, 0, 8), Blocks.FARMLAND);
			helper.setBlock(new BlockPos(x, 1, 8), Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, x <= 8 ? 7 : 3));
		}
		place(helper);

		helper.runAfterDelay(70, () -> {
			for(int x = 6; x <= 8; x++) {
				helper.assertBlockProperty(new BlockPos(x, 1, 8), CropBlock.AGE, 0);
			}
			for(int x = 9; x <= 10; x++) {
				helper.assertBlockProperty(new BlockPos(x, 1, 8), CropBlock.AGE, 3);
			}
			// drops come out the back (south) of the machine
			helper.assertItemEntityPresent(Items.WHEAT, THRESHER, 3);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_16x8x16", timeoutTicks = 100)
	public static void thresherShredsMonsters(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(8, 1, 8));
		place(helper);

		helper.runAfterDelay(70, () -> {
			helper.assertTrue(!zombie.isAlive(), "the zombie should be dead");
			helper.assertItemEntityPresent(ModItems.nitra_small.get(), THRESHER, 3);
			helper.succeed();
		});
	}
}
