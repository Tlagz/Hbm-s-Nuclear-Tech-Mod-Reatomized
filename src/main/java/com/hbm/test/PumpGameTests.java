package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachinePumpElectric;
import com.hbm.tileentity.machine.TileEntityMachinePumpSteam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Ground water pumps: valid ground below, steam and electric operation (absolute positions) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class PumpGameTests {

	/** The pump's core position, in the middle of the test area one block up */
	private static BlockPos core(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		return new BlockPos(Math.min(a.getX(), b.getX()) + 3, a.getY() + 1, Math.min(a.getZ(), b.getZ()) + 3);
	}

	/** Dirt under the pump's 3x3 footprint, 4 layers deep (the test floor is stone) */
	private static void dirt(GameTestHelper helper, BlockPos core) {
		for(int x = -1; x <= 1; x++) for(int z = -1; z <= 1; z++) for(int y = 1; y <= 4; y++) {
			helper.getLevel().setBlockAndUpdate(core.offset(x, -y, z), Blocks.DIRT.defaultBlockState());
		}
	}

	private static <T> T place(GameTestHelper helper, com.hbm.blocks.BlockDummyable block, BlockPos core) {
		helper.assertTrue(core.equals(block.placeMultiblock(helper.getLevel(), core.north(), Direction.NORTH)), "the pump should fit");
		@SuppressWarnings("unchecked") T tile = (T) helper.getLevel().getBlockEntity(core);
		return tile;
	}

	@GameTest(template = "empty_8x4x8")
	public static void electricPumpOnDirt(GameTestHelper helper) {
		BlockPos core = core(helper);
		dirt(helper, core);
		TileEntityMachinePumpElectric pump = place(helper, ModBlocks.pump_electric.get(), core);

		helper.onEachTick(() -> pump.setPower(TileEntityMachinePumpElectric.maxPower));
		helper.succeedWhen(() -> {
			helper.assertTrue(pump.onGround && pump.isOn, "dirt is valid ground, the pump should run");
			helper.assertTrue(pump.water.getFill() >= 30_000, "10,000mB water per tick, has " + pump.water.getFill());
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void pumpNeedsValidGround(GameTestHelper helper) {
		TileEntityMachinePumpElectric pump = place(helper, ModBlocks.pump_electric.get(), core(helper));

		helper.onEachTick(() -> pump.setPower(TileEntityMachinePumpElectric.maxPower));
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(!pump.onGround && !pump.isOn && pump.water.getFill() == 0, "stone isn't valid ground, water: " + pump.water.getFill());
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void steamPumpUsesSteam(GameTestHelper helper) {
		BlockPos core = core(helper);
		dirt(helper, core);
		TileEntityMachinePumpSteam pump = place(helper, ModBlocks.pump_steam.get(), core);

		pump.steam.setFill(1_000);
		helper.runAfterDelay(20, () -> {
			// 1000mB steam is 10 cycles of 100mB, each 1mB spent steam and 1000mB water; the spent steam tank holds 10
			helper.assertTrue(pump.steam.getFill() == 0 && pump.lps.getFill() == 10 && pump.water.getFill() == 10_000, "10 cycles expected: steam " + pump.steam.getFill() + ", lps " + pump.lps.getFill() + ", water " + pump.water.getFill());
			helper.succeed();
		});
	}
}
