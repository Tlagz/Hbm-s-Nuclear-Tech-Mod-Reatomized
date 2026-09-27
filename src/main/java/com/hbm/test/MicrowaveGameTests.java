package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMicrowave;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The microwave: food only, speed dial, and don't turn it all the way up */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class MicrowaveGameTests {

	private static final BlockPos POS = new BlockPos(3, 1, 3);

	private static TileEntityMicrowave place(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(POS);
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_microwave.get().defaultBlockState());
		return (TileEntityMicrowave) helper.getLevel().getBlockEntity(pos);
	}

	private static void setSpeed(TileEntityMicrowave mic, int speed) {
		for(int i = 0; i < speed; i++) {
			CompoundTag data = new CompoundTag();
			data.putBoolean("up", true);
			mic.receiveControl(data);
		}
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void microwaveCooksFood(GameTestHelper helper) {
		TileEntityMicrowave mic = place(helper);
		mic.setPower(TileEntityMicrowave.maxPower);
		mic.setItem(0, new ItemStack(Items.BEEF, 2));

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(mic.time == 0, "nothing happens at speed 0");
			setSpeed(mic, 3);
		});

		// 300 progress at 6 per tick is 50 ticks
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(mic.getItem(1).is(Items.COOKED_BEEF) && mic.getItem(1).getCount() == 1, "one steak after 50 ticks at speed 3, got " + mic.getItem(1));
			helper.assertTrue(mic.getPower() < TileEntityMicrowave.maxPower, "cooking uses power");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void microwaveIgnoresNonFood(GameTestHelper helper) {
		TileEntityMicrowave mic = place(helper);
		mic.setPower(TileEntityMicrowave.maxPower);
		mic.setItem(0, new ItemStack(Items.RAW_IRON));
		setSpeed(mic, 3);

		helper.runAfterDelay(10, () -> {
			helper.assertTrue(mic.time == 0 && mic.getPower() == TileEntityMicrowave.maxPower, "raw iron smelts, but it isn't food");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void microwaveExplodesAtFullSpeed(GameTestHelper helper) {
		TileEntityMicrowave mic = place(helper);
		mic.setPower(TileEntityMicrowave.maxPower);
		mic.setItem(0, new ItemStack(Items.BEEF));
		setSpeed(mic, 10);
		helper.assertTrue(mic.speed == TileEntityMicrowave.maxSpeed, "speed is capped at 5");

		helper.runAfterDelay(3, () -> {
			helper.assertBlockNotPresent(ModBlocks.machine_microwave.get(), POS);
			helper.succeed();
		});
	}
}
