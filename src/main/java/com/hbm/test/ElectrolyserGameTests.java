package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityElectrolyser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Electrolysis machine (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ElectrolyserGameTests {

	private static TileEntityElectrolyser place(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.machine_electrolyser.get().defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityElectrolyser tile = (TileEntityElectrolyser) helper.getLevel().getBlockEntity(core);
		tile.setPower(TileEntityElectrolyser.maxPower);
		return tile;
	}

	@GameTest(template = "empty_8x4x8")
	public static void electrolyserSplitsWater(GameTestHelper helper) {
		TileEntityElectrolyser tile = place(helper);
		tile.tanks[0].setFill(10_000);

		helper.runAfterDelay(25, () -> {
			// 2000mB water into 200mB hydrogen and 200mB oxygen every 10 ticks
			int ops = (10_000 - tile.tanks[0].getFill()) / 2_000;
			helper.assertTrue(ops == 2, "two operations in 25 ticks, got " + ops);
			helper.assertTrue(tile.tanks[1].getTankType() == Fluids.HYDROGEN && tile.tanks[1].getFill() == 400 && tile.tanks[2].getFill() == 400, "hydrogen and oxygen");
			long used = TileEntityElectrolyser.maxPower - tile.power;
			helper.assertTrue(used >= 10_000L * 20 && used <= 10_000L * 26 && used % 10_000 == 0, "10kHE per working tick, used " + used);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void electrolyserMetalMode(GameTestHelper helper) {
		TileEntityElectrolyser tile = place(helper);
		tile.tanks[3].setFill(1_000);
		tile.setItem(14, new ItemStack(ModItems.crystal_iron.get(), 2));

		helper.runAfterDelay(1, () -> {
			helper.assertTrue(tile.canProcessMetal() && tile.getDurationMetal() == 600, "iron crystal takes 600 ticks");
			tile.processMetal();
			helper.assertTrue(tile.leftStack.material == Mats.MAT_IRON && tile.leftStack.amount == MaterialShapes.INGOT.q(6), "6 ingots of iron");
			helper.assertTrue(tile.rightStack.material == Mats.MAT_TITANIUM && tile.rightStack.amount == MaterialShapes.INGOT.q(2), "2 ingots of titanium");
			helper.assertTrue(tile.getItem(15).is(ModItems.powder_lithium_tiny.get()) && tile.getItem(15).getCount() == 3, "tiny lithium byproduct");
			helper.assertTrue(tile.tanks[3].getFill() == 900 && tile.getItem(14).getCount() == 1, "100mB nitric acid and one crystal used");

			tile.setItem(14, new ItemStack(ModItems.crystal_gold.get()));
			helper.assertTrue(!tile.canProcessMetal(), "gold doesn't mix with the stored iron");
			helper.succeed();
		});
	}
}
