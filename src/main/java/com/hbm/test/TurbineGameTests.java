package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityChungus;
import com.hbm.tileentity.machine.TileEntityMachineIndustrialTurbine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Steam turbines on the shared turbine base: industrial turbine and leviathan (absolute positions) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class TurbineGameTests {

	private static BlockPos min(GameTestHelper helper, int size) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(size - 1, 0, size - 1));
		return new BlockPos(Math.min(a.getX(), b.getX()), a.getY() + 1, Math.min(a.getZ(), b.getZ()));
	}

	/** North facing, core at x+4 z+4, 7 long along z */
	private static TileEntityMachineIndustrialTurbine industrial(GameTestHelper helper) {
		BlockPos core = min(helper, 8).offset(4, 0, 4);
		helper.assertTrue(core.equals(ModBlocks.machine_industrial_turbine.get().placeMultiblock(helper.getLevel(), core.north(3), Direction.NORTH)), "the turbine should fit");
		return (TileEntityMachineIndustrialTurbine) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_8x4x8")
	public static void industrialTurbineSpinsUp(GameTestHelper helper) {
		TileEntityMachineIndustrialTurbine turbine = industrial(helper);

		helper.onEachTick(() -> {
			turbine.tanks[0].setFill(turbine.tanks[0].getMaxFill());
			turbine.tanks[1].setFill(0);
		});
		helper.succeedWhen(() -> {
			// 20% of 750,000mB is 1500 ops of 200 TU, the flywheel gives out at least 5% of that from the start
			helper.assertTrue(turbine.maxPower == 300_000, "the power target should be 300kHE/t, is " + turbine.maxPower);
			helper.assertTrue(turbine.spin > 0.1 && turbine.powerBuffer > 30_000, "the flywheel should spin up, spin " + turbine.spin + ", power " + turbine.powerBuffer);
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void industrialTurbineLeverCyclesSteam(GameTestHelper helper) {
		TileEntityMachineIndustrialTurbine turbine = industrial(helper);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);

		// the lever is on the front (the placement direction, 3 from the core), one block up
		BlockPos lever = turbine.getBlockPos().north(3).above();
		helper.getLevel().getBlockState(lever).useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(lever), Direction.NORTH, lever, false));

		helper.assertTrue(turbine.tanks[0].getTankType() == Fluids.HOTSTEAM && turbine.tanks[1].getTankType() == Fluids.STEAM, "steam -> dense steam, got " + turbine.tanks[0].getTankType().getName());
		helper.assertTrue(turbine.tanks[0].getMaxFill() == 75_000, "the compressor shrinks the tank to a tenth, " + turbine.tanks[0].getMaxFill());

		// a click anywhere else does nothing
		BlockPos side = turbine.getBlockPos().above();
		helper.getLevel().getBlockState(side).useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(side), Direction.UP, side, false));
		helper.assertTrue(turbine.tanks[0].getTankType() == Fluids.HOTSTEAM, "only the lever switches");
		helper.succeed();
	}

	/** South facing, core at x+5 z+11: from z+1 (back connector) to z+15 (front connector) */
	private static TileEntityChungus chungus(GameTestHelper helper) {
		BlockPos core = min(helper, 16).offset(5, 0, 11);
		helper.assertTrue(core.equals(ModBlocks.machine_chungus.get().placeMultiblock(helper.getLevel(), core.south(3), Direction.SOUTH)), "the leviathan should fit");
		return (TileEntityChungus) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_16x8x16")
	public static void leviathanUsesAllSteam(GameTestHelper helper) {
		TileEntityChungus turbine = chungus(helper);

		helper.onEachTick(() -> {
			turbine.tanks[0].setFill(1_000_000);
			turbine.tanks[1].setFill(0);
		});
		helper.succeedWhen(() -> {
			// all 1,000,000mB in one tick: 10,000 ops of 200 TU at 85%
			helper.assertTrue(turbine.powerBuffer == 1_700_000, "1.7MHE/t expected, got " + turbine.powerBuffer);
			helper.assertTrue(turbine.operational, "the turbine is running");
		});
	}

	@GameTest(template = "empty_16x8x16")
	public static void leviathanBreaksCompletely(GameTestHelper helper) {
		TileEntityChungus turbine = chungus(helper);
		BlockPos core = turbine.getBlockPos();

		// the parts outside the main dimensions: top layer, the long body, the back end and the front connector
		BlockPos[] parts = {core.above(4).south(), core.north(5).above(3), core.north(9).above(2), core.south(4).above(2)};
		for(BlockPos part : parts) helper.assertTrue(helper.getLevel().getBlockState(part).is(ModBlocks.machine_chungus.get()), "missing part at " + part);

		helper.getLevel().destroyBlock(core.north(9), false);
		helper.succeedWhen(() -> {
			helper.assertTrue(helper.getLevel().getBlockState(core).isAir(), "the core should be gone");
			for(BlockPos part : parts) helper.assertTrue(helper.getLevel().getBlockState(part).isAir(), "leftover part at " + part);
		});
	}
}
