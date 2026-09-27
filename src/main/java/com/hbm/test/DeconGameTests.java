package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmLivingProps.ContaminationEffect;
import com.hbm.lib.RefStrings;
import com.hbm.potion.HbmPotion;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Decontamination shower: removes radiation, the radiation effect and contamination */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class DeconGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void deconCleansWhatStandsOnIt(GameTestHelper helper) {
		helper.setBlock(new BlockPos(2, 1, 3), ModBlocks.decon.get());
		helper.setBlock(new BlockPos(5, 1, 3), ModBlocks.decon.get());
		Pig irradiated = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(2, 2, 3));
		HbmLivingProps.setRadiation(irradiated, 100F);
		// the contamination adds radiation itself until it's washed off, so it gets its own pig
		Pig contaminated = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(5, 2, 3));
		contaminated.addEffect(new MobEffectInstance(HbmPotion.radiation, 600));
		HbmLivingProps.getCont(contaminated).add(new ContaminationEffect(10F, 600, false));

		helper.runAfterDelay(10, () -> {
			helper.assertTrue(HbmLivingProps.getRadiation(irradiated) <= 96F, "half a RAD per tick comes off, has " + HbmLivingProps.getRadiation(irradiated));
			helper.assertTrue(!contaminated.hasEffect(HbmPotion.radiation), "the radiation effect is gone");
			helper.assertTrue(HbmLivingProps.getCont(contaminated).isEmpty(), "and so is the contamination");
			helper.succeed();
		});
	}
}
