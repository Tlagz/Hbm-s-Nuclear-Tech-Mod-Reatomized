package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticReformer;
import com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater;
import com.hbm.tileentity.machine.oil.TileEntityMachineVacuumDistill;
import com.hbm.tileentity.machine.oil.TileEntityOilProcessorBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Vacuum distiller, catalytic reformer, hydrotreater (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class OilProcessingGameTests {

	@SuppressWarnings("unchecked")
	private static <T extends TileEntityOilProcessorBase> T place(GameTestHelper helper, Block block) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, block.defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		T tile = (T) helper.getLevel().getBlockEntity(core);
		tile.setPower(TileEntityOilProcessorBase.maxPower);
		return tile;
	}

	@GameTest(template = "empty_8x4x8")
	public static void vacuumDistillSplitsOil(GameTestHelper helper) {
		TileEntityMachineVacuumDistill distill = place(helper, ModBlocks.machine_vacuum_distill.get());
		distill.tanks[0].setFill(1_000);

		helper.runAfterDelay(5, () -> {
			// 100mB oil per tick into 40 heavy, 25 reformate, 20 light and 15 sour gas, 10,000 HE each
			int ops = (1_000 - distill.tanks[0].getFill()) / 100;
			helper.assertTrue(ops >= 4, "should run every tick, ops " + ops);
			helper.assertTrue(distill.tanks[1].getFill() == ops * 40 && distill.tanks[2].getFill() == ops * 25 && distill.tanks[3].getFill() == ops * 20 && distill.tanks[4].getFill() == ops * 15, "40/25/20/15 split");
			helper.assertTrue(distill.tanks[4].getTankType() == Fluids.SOURGAS && distill.power == TileEntityOilProcessorBase.maxPower - ops * 10_000L, "10kHE per op");
			helper.assertTrue(distill.tanks[0].getPressure() == 2, "the input is pressurized oil");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x12x8")
	public static void reformerNeedsConverter(GameTestHelper helper) {
		TileEntityMachineCatalyticReformer reformer = place(helper, ModBlocks.machine_catalytic_reformer.get());
		reformer.tanks[0].setFill(1_000);

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(reformer.tanks[0].getFill() == 1_000 && reformer.tanks[1].getTankType() == Fluids.REFORMATE, "no catalytic converter, no reforming");
			reformer.setItem(10, new ItemStack(ModItems.catalytic_converter.get()));
		});
		helper.runAfterDelay(10, () -> {
			int ops = (1_000 - reformer.tanks[0].getFill()) / 100;
			helper.assertTrue(ops > 0 && reformer.tanks[1].getFill() == ops * 50 && reformer.tanks[3].getFill() == ops * 10, "naphtha into 50 reformate and 10 hydrogen, ops " + ops);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x12x8")
	public static void hydrotreaterDesulfurizes(GameTestHelper helper) {
		TileEntityMachineHydrotreater treater = place(helper, ModBlocks.machine_hydrotreater.get());
		treater.setItem(10, new ItemStack(ModItems.catalytic_converter.get()));
		treater.tanks[0].setFill(1_000);
		treater.tanks[1].setFill(100);

		helper.runAfterDelay(10, () -> {
			// every 2 ticks: 100mB oil and 5mB hydrogen into 90 desulfurized oil and 15 sour gas
			int ops = (1_000 - treater.tanks[0].getFill()) / 100;
			helper.assertTrue(ops >= 4 && treater.tanks[1].getFill() == 100 - ops * 5, "5mB hydrogen per op, ops " + ops + ", hydrogen " + treater.tanks[1].getFill());
			helper.assertTrue(treater.tanks[2].getTankType() == Fluids.OIL_DS && treater.tanks[2].getFill() == ops * 90 && treater.tanks[3].getFill() == ops * 15, "90 desulfurized oil and 15 sour gas");
			helper.assertTrue(treater.tanks[1].getPressure() == 1, "the hydrogen is pressurized");
			helper.succeed();
		});
	}
}
