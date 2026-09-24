package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineOilWell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Oil drilling */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class OilGameTests {

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void derrickDrillsAndPumpsOil(GameTestHelper helper) {
		// only the core block, the drill only cares about the column below it (the structure is 10 blocks tall)
		BlockPos core = helper.absolutePos(new BlockPos(3, 3, 3));
		helper.getLevel().setBlockAndUpdate(core.below(), Blocks.STONE.defaultBlockState());
		helper.getLevel().setBlockAndUpdate(core.below(2), ModBlocks.ore_oil.get().defaultBlockState());
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.machine_well.get().defaultBlockState()
				.setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));

		TileEntityMachineOilWell well = (TileEntityMachineOilWell) helper.getLevel().getBlockEntity(core);
		well.power = well.getMaxPower();

		helper.succeedWhen(() -> {
			helper.assertTrue(helper.getLevel().getBlockState(core.below()).is(ModBlocks.oil_pipe.get()), "the stone should have been drilled into a pipe");
			helper.assertTrue(well.tanks[0].getFill() >= 500, "the derrick should have pumped oil, has " + well.tanks[0].getFill());
			helper.assertTrue(well.tanks[1].getFill() >= 100, "and some gas, has " + well.tanks[1].getFill());
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void derrickNeedsPower(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 3, 3));
		helper.getLevel().setBlockAndUpdate(core.below(), Blocks.STONE.defaultBlockState());
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.machine_well.get().defaultBlockState()
				.setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityMachineOilWell well = (TileEntityMachineOilWell) helper.getLevel().getBlockEntity(core);

		helper.runAtTickTime(80, () -> {
			helper.assertTrue(helper.getLevel().getBlockState(core.below()).is(Blocks.STONE), "an unpowered derrick must not drill");
			helper.assertTrue(well.indicator == 2, "the GUI indicator should show the missing power, is " + well.indicator);
			helper.succeed();
		});
	}
}
