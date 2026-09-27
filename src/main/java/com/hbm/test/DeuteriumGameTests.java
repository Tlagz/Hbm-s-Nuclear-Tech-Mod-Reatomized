package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDeuteriumExtractor;
import com.hbm.tileentity.machine.TileEntityDeuteriumTower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Deuterium extractor and tower: 50mB of water to 1mB of heavy water */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class DeuteriumGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void extractorMakesHeavyWater(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_deuterium_extractor.get().defaultBlockState());
		TileEntityDeuteriumExtractor extractor = (TileEntityDeuteriumExtractor) helper.getLevel().getBlockEntity(pos);

		extractor.tanks[0].setFill(1_000);
		extractor.setPower(10_000);

		helper.runAfterDelay(2, () -> {
			// each tick converts at most the heavy water tank's size worth of water, 100mB into 2mB
			helper.assertTrue(extractor.tanks[1].getFill() == 4 && extractor.tanks[0].getFill() == 800, "two ticks make 4mB heavy water out of 200mB water, has " + extractor.tanks[1].getFill());
			helper.assertTrue(extractor.getPower() == 9_000, "each operation costs a 20th of the buffer, has " + extractor.getPower());
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x12x8")
	public static void towerTakesWaterThroughItsBase(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_deuterium_tower.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 3)), Direction.NORTH);
		TileEntityDeuteriumTower tower = (TileEntityDeuteriumTower) helper.getLevel().getBlockEntity(core);

		// every other block of the 2x2 base is a port
		Direction dir = BlockDummyable.getRotation(tower.getBlockState());
		for(BlockPos base : new BlockPos[] {core.relative(dir, -1), core.relative(dir.getCounterClockWise()), core.relative(dir, -1).relative(dir.getCounterClockWise())}) {
			helper.assertTrue(helper.getLevel().getBlockEntity(base) instanceof TileEntityProxyCombo proxy && proxy.fluid && proxy.power, "the base block at " + base + " should be a port");
		}
		helper.assertTrue(helper.getLevel().getBlockState(core.above(9)).getBlock() == ModBlocks.machine_deuterium_tower.get(), "the tower is 10 blocks tall");

		TileEntityProxyCombo port = (TileEntityProxyCombo) helper.getLevel().getBlockEntity(core.relative(dir, -1));
		port.transferFluid(Fluids.WATER, 0, 50_000);
		tower.setPower(100_000);

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(tower.tanks[1].getFill() == 200 && tower.tanks[0].getFill() == 40_000, "the tower does 5000mB of water per tick, two ticks make 200mB heavy water, has " + tower.tanks[1].getFill());
			helper.succeed();
		});
	}
}
