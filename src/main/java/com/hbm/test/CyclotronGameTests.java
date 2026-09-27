package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Cyclotron: particle + target make a new element and antimatter, water turns into spent steam */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class CyclotronGameTests {

	private static TileEntityMachineCyclotron place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_cyclotron.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(8, 1, 8)), Direction.NORTH);
		helper.assertTrue(core != null, "the cyclotron should fit");
		return (TileEntityMachineCyclotron) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_16x8x16", timeoutTicks = 260)
	public static void cyclotronTransmutes(GameTestHelper helper) {
		helper.assertTrue(CyclotronRecipes.recipes.size() == 42, "all 42 recipes, has " + CyclotronRecipes.recipes.size());

		TileEntityMachineCyclotron cyc = place(helper);
		cyc.setItem(0, new ItemStack(ModItems.part_lithium.get()));
		cyc.setItem(3, new ItemStack(ModItems.powder_iron.get()));
		// speed 4, 173 ticks instead of 690
		cyc.setItem(10, new ItemStack(ModItems.upgrade_speed_3.get()));

		helper.onEachTick(() -> {
			cyc.setPower(TileEntityMachineCyclotron.maxPower);
			cyc.tanks[0].setFill(cyc.tanks[0].getMaxFill());
			// nothing takes the spent steam away here, a full tank stops the machine
			if(cyc.tanks[1].getFill() > 16_000) cyc.tanks[1].setFill(0);
		});

		helper.runAfterDelay(10, () -> helper.assertTrue(cyc.progress > 0 && cyc.tanks[1].getFill() > 0, "it runs and makes spent steam"));

		helper.runAfterDelay(200, () -> {
			helper.assertTrue(cyc.getItem(6).is(ModItems.powder_cobalt.get()), "lithium on iron makes cobalt, got " + cyc.getItem(6));
			helper.assertTrue(cyc.getItem(0).isEmpty() && cyc.getItem(3).isEmpty(), "both inputs are used up");
			helper.assertTrue(cyc.tanks[2].getFill() == 50, "and 50 mB antimatter, has " + cyc.tanks[2].getFill());
			helper.succeed();
		});
	}

	@GameTest(template = "empty_16x8x16")
	public static void cyclotronSideLanes(GameTestHelper helper) {
		TileEntityMachineCyclotron cyc = place(helper);
		BlockPos core = cyc.getBlockPos();

		// the three blocks of each side feed one lane each, particle + target
		int[] middle = cyc.getAccessibleSlotsFromSide(core.north(2), Direction.NORTH);
		helper.assertTrue(middle[0] == 1 && middle[1] == 4, "the middle of a side is lane 2");
		helper.assertTrue(cyc.isItemValidForSlot(0, new ItemStack(ModItems.part_carbon.get())), "particles go into the particle slots");
		helper.assertTrue(!cyc.isItemValidForSlot(3, new ItemStack(ModItems.part_carbon.get())), "but not into the targets");
		helper.succeed();
	}
}
