package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFEL;
import com.hbm.tileentity.machine.TileEntitySILEX;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The FEL shining through a SILEX chamber separates uranium isotopes */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class SILEXGameTests {

	@GameTest(template = "empty_16x8x16", timeoutTicks = 200)
	public static void felPowersSilex(GameTestHelper helper) {
		helper.assertTrue(SILEXRecipes.getOutput(new ItemStack(ModItems.ingot_uranium.get())) != null, "uranium ingots have a SILEX recipe");
		helper.assertTrue(SILEXRecipes.getOutput(new ItemStack(ModItems.powder_uranium.get())) != null, "uranium dust counts as ingots (dictionary translation)");

		// the laser's core at x 5 facing east (placed two blocks in front of the core), the chamber's core 6 blocks further
		BlockPos felCore = helper.absolutePos(new BlockPos(5, 1, 7));
		BlockPos placedFel = ModBlocks.machine_fel.get().placeMultiblock(helper.getLevel(), felCore.relative(Direction.EAST, 2), Direction.EAST);
		helper.assertTrue(felCore.equals(placedFel), "the FEL core should be at " + felCore + ", is " + placedFel);

		BlockPos silexCore = felCore.relative(Direction.EAST, 6);
		BlockPos placedSilex = ModBlocks.machine_silex.get().placeMultiblock(helper.getLevel(), silexCore.relative(Direction.EAST), Direction.EAST);
		helper.assertTrue(silexCore.equals(placedSilex), "the SILEX core should be at " + silexCore + ", is " + placedSilex);

		TileEntityFEL fel = (TileEntityFEL) helper.getLevel().getBlockEntity(felCore);
		TileEntitySILEX silex = (TileEntitySILEX) helper.getLevel().getBlockEntity(silexCore);

		fel.setItem(1, new ItemStack(ModItems.laser_crystal_bismuth.get()));
		fel.isOn = true;
		helper.onEachTick(() -> fel.setPower(TileEntityFEL.maxPower));

		silex.tank.setTankType(Fluids.PEROXIDE);
		silex.tank.setFill(8_000);
		silex.setItem(0, new ItemStack(ModItems.ingot_uranium.get(), 2));

		helper.runAfterDelay(30, () -> {
			helper.assertTrue(fel.mode == EnumWavelengths.VISIBLE && !fel.missingValidSilex, "the laser should find the chamber");
			helper.assertTrue(silex.currentFill > 0 && silex.current.is(ModItems.ingot_uranium.get()), "an ingot should be dissolved, fill " + silex.currentFill);
		});

		helper.succeedWhen(() -> {
			int nuggets = 0;
			for(int i = 5; i < 11; i++) {
				ItemStack out = silex.getItem(i);
				if(!out.isEmpty()) {
					helper.assertTrue(out.is(ModItems.nugget_u235.get()) || out.is(ModItems.nugget_u238.get()), "only uranium nuggets come out, got " + out);
					nuggets += out.getCount();
				}
			}
			helper.assertTrue(nuggets >= 1, "at least one nugget should be separated");
		});
	}
}
