package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.FoundryOutlet;
import com.hbm.blocks.machine.FoundryTank;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFoundryTank;
import com.hbm.tileentity.machine.TileEntitySlag;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Foundry tanks (stacking, spreading, draining into outlets) and the slag tap */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class FoundryTankGameTests {

	private static TileEntityFoundryTank tank(GameTestHelper helper, BlockPos rel) {
		BlockPos pos = helper.absolutePos(rel);
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.foundry_tank.get().getStateForPlacement(new net.minecraft.world.item.context.BlockPlaceContext(helper.getLevel(), null, net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY, new net.minecraft.world.phys.BlockHitResult(pos.getCenter(), Direction.UP, pos, false))));
		return (TileEntityFoundryTank) helper.getLevel().getBlockEntity(pos);
	}

	private static void fill(TileEntityFoundryTank tank, int amount) {
		tank.type = Mats.MAT_IRON;
		tank.amount = amount;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void tanksConnectAndDrainDownwards(GameTestHelper helper) {
		TileEntityFoundryTank lower = tank(helper, new BlockPos(3, 1, 3));
		TileEntityFoundryTank upper = tank(helper, new BlockPos(3, 2, 3));
		TileEntityFoundryTank side = tank(helper, new BlockPos(4, 1, 3));

		helper.assertBlockProperty(new BlockPos(3, 1, 3), FoundryTank.UP, true);
		helper.assertBlockProperty(new BlockPos(3, 1, 3), FoundryTank.EAST, true);
		helper.assertBlockProperty(new BlockPos(3, 2, 3), FoundryTank.DOWN, true);
		helper.assertBlockProperty(new BlockPos(4, 1, 3), FoundryTank.NORTH, false);

		int ingot = MaterialShapes.INGOT.q(1);
		fill(upper, ingot * 10);

		helper.runAfterDelay(40, () -> {
			helper.assertTrue(upper.amount == 0, "the upper tank drains into the lower one, has " + upper.amount);
			helper.assertTrue(lower.amount + side.amount == ingot * 10, "nothing gets lost, " + lower.amount + " + " + side.amount);
			helper.assertTrue(side.amount > 0 && side.type == Mats.MAT_IRON, "and it spreads to the tank next to it");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void tankFeedsOutletIntoTankBelow(GameTestHelper helper) {
		TileEntityFoundryTank tank = tank(helper, new BlockPos(2, 2, 3));
		helper.setBlock(new BlockPos(3, 2, 3), ModBlocks.foundry_outlet.get().defaultBlockState().setValue(FoundryOutlet.FACING, Direction.EAST));
		TileEntityFoundryTank target = tank(helper, new BlockPos(3, 1, 3));
		// the tank shows the outlet hole
		helper.assertBlockProperty(new BlockPos(2, 2, 3), FoundryTank.OUT_EAST, true);

		int ingot = MaterialShapes.INGOT.q(1);
		fill(tank, ingot * 4);

		helper.runAfterDelay(40, () -> {
			helper.assertTrue(target.amount == ingot * 4 && target.type == Mats.MAT_IRON, "the tank below the outlet should get all of it, has " + target.amount);
			helper.assertTrue(tank.amount == 0, "and the tank be empty");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void slagtapDumpsSlag(GameTestHelper helper) {
		TileEntityFoundryTank tank = tank(helper, new BlockPos(2, 3, 3));
		helper.setBlock(new BlockPos(3, 3, 3), ModBlocks.foundry_slagtap.get().defaultBlockState().setValue(FoundryOutlet.FACING, Direction.EAST));

		int amount = TileEntitySlag.maxAmount / 2;
		fill(tank, amount);

		helper.runAfterDelay(40, () -> {
			// the template's floor is below y 0, the slag lands on it and spreads out
			int total = 0;
			for(int x = 0; x < 8; x++) for(int z = 0; z < 8; z++) {
				if(helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(x, 0, z))) instanceof TileEntitySlag slag) {
					helper.assertTrue(slag.mat == Mats.MAT_IRON, "iron slag");
					total += slag.amount;
				}
			}
			helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(3, 0, 3))).is(ModBlocks.slag.get()), "slag right below the tap");
			helper.assertTrue(total == amount, "all of the iron ends up as slag, " + total + " of " + amount);
			helper.assertTrue(tank.amount == 0, "the tank is empty");
			helper.succeed();
		});
	}
}
