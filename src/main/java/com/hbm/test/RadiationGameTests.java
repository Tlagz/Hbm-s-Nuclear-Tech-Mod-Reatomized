package com.hbm.test;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for radiation and hazards, run with ./gradlew runGameTestServer */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class RadiationGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void chunkRadiationContaminatesMobs(GameTestHelper helper) {
		Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(2, 1, 2));
		Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
		ChunkRadiationManager.proxy.setRadiation(helper.getLevel(), pig.blockPosition(), 100F);

		helper.runAfterDelay(20, () -> {
			helper.assertTrue(HbmLivingProps.getRadiation(pig) > 50F, "pig should have picked up radiation, has " + HbmLivingProps.getRadiation(pig));
			helper.assertTrue(HbmLivingProps.getRadiation(zombie) == 0F, "zombies are immune");
			ChunkRadiationManager.proxy.setRadiation(helper.getLevel(), pig.blockPosition(), 0F);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void irradiatedCowBecomesMooshroom(GameTestHelper helper) {
		Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(2, 1, 2));
		HbmLivingProps.setRadiation(cow, 60F);

		helper.runAfterDelay(2, () -> {
			helper.assertEntityPresent(EntityType.MOOSHROOM);
			helper.assertEntityNotPresent(EntityType.COW);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void uraniumIngotIsRadioactive(GameTestHelper helper) {
		ItemStack stack = new ItemStack(ModItems.ingot_uranium.get(), 4);
		float level = HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.RADIATION);
		helper.assertTrue(Math.abs(level - HazardRegistry.u * HazardRegistry.ingot) < 1e-6, "uranium ingot should have " + HazardRegistry.u + " RAD/s, has " + level);

		Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(2, 1, 2));
		pig.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, stack);

		helper.runAfterDelay(20, () -> {
			// 4 ingots * 0.35 RAD/s for ~1 second
			float rad = HbmLivingProps.getRadiation(pig);
			helper.assertTrue(rad > 1.0F && rad < 2.0F, "pig holding 4 uranium ingots should have ~1.4 RAD, has " + rad);
			helper.succeed();
		});
	}
}
