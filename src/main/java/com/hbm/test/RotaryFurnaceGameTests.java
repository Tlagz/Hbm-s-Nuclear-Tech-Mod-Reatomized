package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Rotary furnace (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class RotaryFurnaceGameTests {

	@GameTest(template = "empty_8x4x8", timeoutTicks = 140)
	public static void rotaryFurnaceMakesSteel(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.machine_rotary_furnace.get().defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityMachineRotaryFurnace furnace = (TileEntityMachineRotaryFurnace) helper.getLevel().getBlockEntity(core);

		furnace.setItem(0, new ItemStack(Items.IRON_INGOT, 2));
		furnace.setItem(2, new ItemStack(Items.COAL, 2));
		furnace.setItem(4, new ItemStack(Items.COAL, 1));
		helper.onEachTick(() -> furnace.tanks[1].setFill(furnace.tanks[1].getMaxFill()));

		helper.runAfterDelay(2, () -> helper.assertTrue(furnace.maxBurnTime == 800 && furnace.getItem(4).isEmpty(), "the coal burns for half its furnace time, got " + furnace.maxBurnTime));

		helper.runAfterDelay(110, () -> {
			// 100 ticks with coal: one ingot of molten steel, nothing below to pour into
			helper.assertTrue(furnace.output != null && furnace.output.material == Mats.MAT_STEEL && furnace.output.amount == MaterialShapes.INGOT.q(1), "one ingot of steel, got " + (furnace.output == null ? null : furnace.output.amount));
			helper.assertTrue(furnace.getItem(0).getCount() == 1 && furnace.getItem(2).getCount() == 1, "one iron ingot and one coal used");
			helper.assertTrue(furnace.tanks[2].getFill() > 0, "the used steam comes back as spent steam");
			helper.succeed();
		});
	}
}
