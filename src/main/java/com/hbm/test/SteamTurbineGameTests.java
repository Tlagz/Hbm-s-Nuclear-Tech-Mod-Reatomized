package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineTurbine;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The small steam turbine: steam in, power and spent steam out, capped at 6000mB per tick */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class SteamTurbineGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void turbineMakesPowerFromSteam(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_turbine.get().defaultBlockState());
		TileEntityMachineTurbine turbine = (TileEntityMachineTurbine) helper.getLevel().getBlockEntity(pos);

		turbine.tanks[0].setFill(20_000);

		helper.runAfterDelay(1, () -> {
			helper.assertTrue(turbine.tanks[0].getFill() == 14_000, "at most 6000mB of steam per tick, left " + turbine.tanks[0].getFill());
			helper.assertTrue(turbine.tanks[1].getTankType() == Fluids.SPENTSTEAM && turbine.tanks[1].getFill() > 0, "spent steam comes out");
			helper.assertTrue(turbine.getPower() > 0, "the turbine should make power");
		});
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(turbine.tanks[0].getFill() == 0, "all steam used up");
			helper.succeed();
		});
	}
}
