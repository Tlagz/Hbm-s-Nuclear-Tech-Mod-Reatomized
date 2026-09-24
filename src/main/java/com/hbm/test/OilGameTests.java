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

	@GameTest(template = "empty_8x12x8", timeoutTicks = 200)
	public static void fireboxHeatsBoilerOilIntoHotOil(GameTestHelper helper) {
		BlockPos base = helper.absolutePos(new BlockPos(3, 1, 3));
		// both have an offset of 1: the core ends up one block behind (south of) the placed block
		BlockPos firebox = ModBlocks.heater_firebox.get().placeMultiblock(helper.getLevel(), base.north(), Direction.NORTH);
		BlockPos boiler = ModBlocks.machine_boiler.get().placeMultiblock(helper.getLevel(), base.north().above(), Direction.NORTH);
		helper.assertTrue(base.equals(firebox) && base.above().equals(boiler), "firebox at " + firebox + " and boiler at " + boiler + " should be stacked at " + base);

		com.hbm.tileentity.machine.TileEntityHeaterFirebox heater = (com.hbm.tileentity.machine.TileEntityHeaterFirebox) helper.getLevel().getBlockEntity(firebox);
		heater.setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL, 8));
		com.hbm.tileentity.machine.TileEntityHeatBoiler heat = (com.hbm.tileentity.machine.TileEntityHeatBoiler) helper.getLevel().getBlockEntity(boiler);
		heat.tanks[0].setTankType(com.hbm.inventory.fluid.Fluids.OIL);
		heat.tanks[0].setFill(1000);

		helper.succeedWhen(() -> {
			helper.assertTrue(heater.heatEnergy > 0 || heater.burnTime > 0, "the firebox should burn coal");
			helper.assertTrue(heat.tanks[1].getTankType() == com.hbm.inventory.fluid.Fluids.HOTOIL && heat.tanks[1].getFill() > 0,
					"the boiler should make hot oil, has " + heat.tanks[1].getFill() + " " + heat.tanks[1].getTankType().getName() + ", heat " + heat.heat);
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void refinerySplitsHotOil(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.machine_refinery.get().defaultBlockState()
				.setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		com.hbm.tileentity.machine.oil.TileEntityMachineRefinery refinery = (com.hbm.tileentity.machine.oil.TileEntityMachineRefinery) helper.getLevel().getBlockEntity(core);
		refinery.tanks[0].setFill(2000);
		refinery.power = com.hbm.tileentity.machine.oil.TileEntityMachineRefinery.maxPower;

		helper.succeedWhen(() -> {
			helper.assertTrue(refinery.tanks[0].getFill() == 1000, "10 operations should have used 1000mB hot oil, left " + refinery.tanks[0].getFill());
			helper.assertTrue(refinery.tanks[1].getTankType() == com.hbm.inventory.fluid.Fluids.HEAVYOIL && refinery.tanks[1].getFill() == 500, "heavy oil 10x50, has " + refinery.tanks[1].getFill());
			helper.assertTrue(refinery.tanks[2].getFill() == 250 && refinery.tanks[3].getFill() == 150 && refinery.tanks[4].getFill() == 100, "naphtha/light oil/petroleum should be 250/150/100");
			helper.assertTrue(refinery.getItem(11).is(com.hbm.items.ModItems.sulfur.get()), "one sulfur every 10 operations, slot: " + refinery.getItem(11));
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
