package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineDrain;
import com.hbm.tileentity.machine.TileEntityMachineIntake;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The air intake fills its tank while powered, the drainage pipe spills half its tank every tick */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class IntakeDrainGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void intakeCompressesAir(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_intake.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 3)), Direction.NORTH);
		TileEntityMachineIntake intake = (TileEntityMachineIntake) helper.getLevel().getBlockEntity(core);

		helper.assertTrue(intake.compair.getFill() == 0, "no air without power");
		intake.setPower(2_000);

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(intake.compair.getTankType() == Fluids.AIR && intake.compair.getFill() == 1_000, "the tank should be full of air");
			helper.assertTrue(intake.getPower() == 1_800, "100 HE per tick, has " + intake.getPower());
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void drainSpillsHalfPerTick(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_drain.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 3)), Direction.NORTH);
		TileEntityMachineDrain drain = (TileEntityMachineDrain) helper.getLevel().getBlockEntity(core);

		drain.tank.setTankType(Fluids.WATER);
		drain.tank.setFill(2_000);

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(drain.tank.getFill() == 500, "2000 -> 1000 -> 500mB after two ticks, has " + drain.tank.getFill());
			helper.succeed();
		});
	}
}
