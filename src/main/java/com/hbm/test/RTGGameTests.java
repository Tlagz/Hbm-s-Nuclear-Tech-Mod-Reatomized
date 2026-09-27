package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemRTGPelletDepleted.DepletedRTGMaterial;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineRTG;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** RTG pellets and the RT generator */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class RTGGameTests {

	private static TileEntityMachineRTG place(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_rtg_grey.get().defaultBlockState());
		return (TileEntityMachineRTG) helper.getLevel().getBlockEntity(pos);
	}

	@GameTest(template = "empty_8x4x8")
	public static void rtgMakesPowerAndPelletsDecay(GameTestHelper helper) {
		TileEntityMachineRTG rtg = place(helper);
		rtg.setItem(0, new ItemStack(ModItems.pellet_rtg.get()));
		rtg.setItem(1, new ItemStack(ModItems.pellet_rtg_polonium.get()));

		helper.runAfterDelay(1, () -> {
			helper.assertTrue(rtg.heat == 60, "plutonium (10) and polonium (50) make 60 heat, has " + rtg.heat);
			helper.assertTrue(rtg.getPower() == 300, "5HE per heat per tick, has " + rtg.getPower());
			long max = ModItems.pellet_rtg.get().getMaxLifespan();
			helper.assertTrue(ModItems.pellet_rtg.get().getLifespan(rtg.getItem(0)) < max, "the pellet should decay while inside");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void spentPelletsDeplete(GameTestHelper helper) {
		TileEntityMachineRTG rtg = place(helper);
		ItemStack almostDone = new ItemStack(ModItems.pellet_rtg.get());
		almostDone.set(ModDataComponents.PELLET_DEPLETION.get(), 1L);
		rtg.setItem(0, almostDone);

		helper.runAfterDelay(3, () -> {
			helper.assertTrue(rtg.getItem(0).is(ModItems.pellet_rtg_depleted.get(DepletedRTGMaterial.LEAD).get()), "the spent plutonium pellet turns into a depleted lead pellet, got " + rtg.getItem(0));
			helper.assertTrue(rtg.getItem(0).getCraftingRemainingItem().is(ModItems.plate_iron.get()), "depleted pellets give their iron plate back when crafted");
			helper.succeed();
		});
	}
}
