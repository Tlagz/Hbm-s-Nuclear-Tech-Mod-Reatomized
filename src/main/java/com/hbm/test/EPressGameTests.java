package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineEPress;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The electric press: powered pressing, no warm-up like the burner press */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class EPressGameTests {

	private static TileEntityMachineEPress place(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		ModBlocks.machine_epress.get().placeMultiblock(helper.getLevel(), core, Direction.NORTH);
		TileEntityMachineEPress press = (TileEntityMachineEPress) helper.getLevel().getBlockEntity(core);
		press.setItem(1, new ItemStack(ModItems.stamp_iron_plate.get()));
		press.setItem(2, new ItemStack(Items.IRON_INGOT, 3));
		return press;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 80)
	public static void epressMakesPlates(GameTestHelper helper) {
		TileEntityMachineEPress press = place(helper);
		press.setItem(0, new ItemStack(ModItems.battery_creative.get()));

		helper.succeedWhen(() -> {
			helper.assertTrue(press.getItem(3).is(ModItems.plate_iron.get()) && press.getItem(3).getCount() == 3, "three iron plates, output: " + press.getItem(3));
			helper.assertTrue(press.getItem(1).getDamageValue() == 3, "the stamp should wear down once per plate");
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void epressNeedsPower(GameTestHelper helper) {
		TileEntityMachineEPress press = place(helper);
		press.setPower(250);

		helper.runAfterDelay(40, () -> {
			helper.assertTrue(press.getItem(3).isEmpty() && press.getItem(2).getCount() == 3, "two ticks of power shouldn't press anything");
			helper.assertTrue(press.getPower() < 100, "the power should be used up while pressing");
			helper.succeed();
		});
	}
}
