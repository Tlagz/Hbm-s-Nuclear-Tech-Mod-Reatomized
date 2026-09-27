package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityTesla;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Tesla coil: zaps what it can see, spares ocelots, runs for free on a meteorite battery */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class TeslaGameTests {

	private static TileEntityTesla place(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(8, 1, 8));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.tesla.get().defaultBlockState());
		return (TileEntityTesla) helper.getLevel().getBlockEntity(pos);
	}

	@GameTest(template = "empty_16x8x16")
	public static void teslaZapsWhatItSees(GameTestHelper helper) {
		TileEntityTesla tesla = place(helper);
		Pig visible = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(12, 1, 8));
		Pig hidden = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 1, 8));
		Ocelot cat = helper.spawnWithNoFreeWill(EntityType.OCELOT, new BlockPos(8, 1, 12));
		// a wall between the coil and the hidden pig
		for(int y = 1; y <= 4; y++) for(int z = 5; z <= 11; z++) helper.setBlock(new BlockPos(5, y, z), Blocks.STONE);
		tesla.setPower(TileEntityTesla.maxPower);

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(visible.getHealth() < visible.getMaxHealth(), "the pig in sight gets zapped");
			helper.assertTrue(hidden.getHealth() == hidden.getMaxHealth(), "the one behind the wall doesn't");
			helper.assertTrue(cat.getHealth() == cat.getMaxHealth(), "ocelots are spared");
			helper.assertTrue(tesla.targets.size() == 1, "one beam, has " + tesla.targets.size());
			helper.assertTrue(tesla.getPower() < TileEntityTesla.maxPower, "zapping takes power");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_16x8x16")
	public static void teslaRunsOnMeteoriteBattery(GameTestHelper helper) {
		helper.setBlock(new BlockPos(8, 1, 8), ModBlocks.meteor_battery.get());
		BlockPos pos = helper.absolutePos(new BlockPos(8, 2, 8));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.tesla.get().defaultBlockState());
		TileEntityTesla tesla = (TileEntityTesla) helper.getLevel().getBlockEntity(pos);
		Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(11, 1, 8));

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "the battery powers the coil");
			helper.succeed();
		});
	}
}
