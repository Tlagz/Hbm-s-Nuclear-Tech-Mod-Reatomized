package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityCondenser;
import com.hbm.tileentity.machine.TileEntitySteamEngine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The steam chain: steam engine and condenser (absolute positions) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class SteamGameTests {

	private static BlockPos min(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		return new BlockPos(Math.min(a.getX(), b.getX()), a.getY() + 1, Math.min(a.getZ(), b.getZ()));
	}

	/** A south facing engine along the z axis, core at x+3 z+6, its ports at x+1, one block up, z+5 to z+7 */
	private static TileEntitySteamEngine engine(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_steam_engine.get().placeMultiblock(helper.getLevel(), min(helper).offset(3, 0, 7), Direction.SOUTH);
		helper.assertTrue(core != null && core.equals(min(helper).offset(3, 0, 6)), "the engine should fit, core at " + core);
		return (TileEntitySteamEngine) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_8x4x8")
	public static void steamEngineRunsIntoCondenser(GameTestHelper helper) {
		TileEntitySteamEngine engine = engine(helper);
		BlockPos port = min(helper).offset(1, 1, 6);
		helper.getLevel().setBlockAndUpdate(port, ModBlocks.machine_condenser.get().defaultBlockState());
		TileEntityCondenser condenser = (TileEntityCondenser) helper.getLevel().getBlockEntity(port);

		// full steam in, the condensed water taken away
		helper.onEachTick(() -> {
			engine.tanks[0].setFill(engine.tanks[0].getMaxFill());
			condenser.tanks[1].setFill(0);
		});
		helper.succeedWhen(() -> {
			// 20 ops of 100mB steam into 1mB spent steam, 200 TU each at 85%
			helper.assertTrue(engine.getPower() == 3400, "20 ops per tick should give 3400 HE/t, got " + engine.getPower());
			helper.assertTrue(condenser.waterTimer > 0, "the condenser should be making water");
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void steamEngineStallsWhenFull(GameTestHelper helper) {
		TileEntitySteamEngine engine = engine(helper);
		helper.onEachTick(() -> engine.tanks[0].setFill(engine.tanks[0].getMaxFill()));

		// nothing takes the spent steam, the first tick fills the 20mB tank and then it stops
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(engine.tanks[1].getFill() == 20 && engine.tanks[1].getTankType() == Fluids.SPENTSTEAM, "the spent steam tank should be full, has " + engine.tanks[1].getFill());
			helper.assertTrue(engine.getPower() == 0, "no room for spent steam, no power, got " + engine.getPower());
			helper.succeed();
		});
	}

	private static BlockPos min12(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(11, 0, 11));
		return new BlockPos(Math.min(a.getX(), b.getX()), a.getY() + 1, Math.min(a.getZ(), b.getZ()));
	}

	private static com.hbm.tileentity.machine.storage.TileEntityBarrel barrel(GameTestHelper helper, BlockPos pos, com.hbm.inventory.fluid.FluidType type, short mode, int fill) {
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.barrel_steel.get().defaultBlockState());
		com.hbm.tileentity.machine.storage.TileEntityBarrel barrel = (com.hbm.tileentity.machine.storage.TileEntityBarrel) helper.getLevel().getBlockEntity(pos);
		barrel.tank.setTankType(type);
		barrel.tank.setFill(fill);
		barrel.mode = mode;
		return barrel;
	}

	/** Spent steam pushed into one side port through the proxy, water comes out into a barrel at another port */
	@GameTest(template = "empty_12x20x12", timeoutTicks = 200)
	public static void smallTowerCondensesThroughPorts(GameTestHelper helper) {
		BlockPos core = min12(helper).offset(5, 0, 5);
		helper.assertTrue(core.equals(ModBlocks.machine_tower_small.get().placeMultiblock(helper.getLevel(), core.north(2), Direction.NORTH)), "the tower should fit");
		com.hbm.tileentity.machine.TileEntityTowerSmall tower = (com.hbm.tileentity.machine.TileEntityTowerSmall) helper.getLevel().getBlockEntity(core);

		var source = barrel(helper, core.west(3), Fluids.SPENTSTEAM, (short) 2, 4000);
		var target = barrel(helper, core.east(3), Fluids.WATER, (short) 0, 0);

		helper.succeedWhen(() -> {
			helper.assertTrue(source.tank.getFill() == 0, "the tower should take all the spent steam, left: " + source.tank.getFill());
			helper.assertTrue(target.tank.getFill() == 4000, "all of it should arrive as water, has " + target.tank.getFill() + ", tower " + tower.tanks[0].getFill() + " / " + tower.tanks[1].getFill());
		});
	}

	@GameTest(template = "empty_12x20x12", timeoutTicks = 200)
	public static void largeTowerCondensesThroughPorts(GameTestHelper helper) {
		BlockPos core = min12(helper).offset(5, 0, 5);
		helper.assertTrue(core.equals(ModBlocks.machine_tower_large.get().placeMultiblock(helper.getLevel(), core.north(4), Direction.NORTH)), "the tower should fit");

		// side ports: 5 out, in the middle and 3 to either side
		var source = barrel(helper, core.south(5).east(3), Fluids.SPENTSTEAM, (short) 2, 16000);
		var target = barrel(helper, core.west(5).north(3), Fluids.WATER, (short) 0, 0);

		helper.succeedWhen(() -> {
			helper.assertTrue(source.tank.getFill() == 0 && target.tank.getFill() == 16000, "16000mB spent steam through the tower, source " + source.tank.getFill() + ", target " + target.tank.getFill());
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void poweredCondenserNeedsPower(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_condenser_powered.get().placeMultiblock(helper.getLevel(), min(helper).offset(4, 0, 3), Direction.NORTH);
		helper.assertTrue(core != null, "the condenser should fit");
		com.hbm.tileentity.machine.TileEntityCondenserPowered condenser = (com.hbm.tileentity.machine.TileEntityCondenserPowered) helper.getLevel().getBlockEntity(core);
		condenser.tanks[0].setFill(1000);

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(condenser.tanks[1].getFill() == 0, "no power, no condensing");
			condenser.setPower(1_000_000);
		});
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(condenser.tanks[0].getFill() == 0 && condenser.tanks[1].getFill() == 1000, "1000mB should be condensed, has " + condenser.tanks[0].getFill() + " / " + condenser.tanks[1].getFill());
			helper.assertTrue(condenser.getPower() == 1_000_000 - 10 * 1000, "10 HE per mB, power left " + condenser.getPower());
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void condenserTurnsSpentSteamIntoWater(GameTestHelper helper) {
		BlockPos pos = min(helper).offset(3, 0, 3);
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_condenser.get().defaultBlockState());
		TileEntityCondenser condenser = (TileEntityCondenser) helper.getLevel().getBlockEntity(pos);
		condenser.tanks[0].setFill(70);

		helper.succeedWhen(() -> {
			helper.assertTrue(condenser.tanks[0].getFill() == 0 && condenser.tanks[1].getFill() == 70, "70mB spent steam should become 70mB water, has " + condenser.tanks[0].getFill() + " / " + condenser.tanks[1].getFill());
			helper.assertTrue(condenser.tanks[1].getTankType() == Fluids.WATER, "water comes out");
		});
	}
}
