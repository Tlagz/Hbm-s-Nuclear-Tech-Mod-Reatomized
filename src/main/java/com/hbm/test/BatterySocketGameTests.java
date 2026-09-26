package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBatteryPack.EnumBatteryPack;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.storage.TileEntityBatteryBase;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Battery sockets as part of the power grid */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class BatterySocketGameTests {

	private static TileEntityBatterySocket socket(GameTestHelper helper, int x, long charge) {
		BlockPos core = ModBlocks.machine_battery_socket.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(x, 1, 2)), Direction.NORTH);
		TileEntityBatterySocket socket = (TileEntityBatterySocket) helper.getLevel().getBlockEntity(core);
		ItemStack battery = new ItemStack(ModItems.battery_pack.get(EnumBatteryPack.BATTERY_LEAD).get());
		((IBatteryItem) battery.getItem()).setCharge(battery, charge);
		socket.setItem(0, battery);
		return socket;
	}

	@GameTest(template = "empty_8x4x8")
	public static void socketChargesSocket(GameTestHelper helper) {
		long capacity = EnumBatteryPack.BATTERY_LEAD.capacity;

		// the full one outputs, the empty one keeps the default input mode, joined by two cables
		TileEntityBatterySocket full = socket(helper, 1, capacity);
		full.redLow = TileEntityBatteryBase.mode_output;
		TileEntityBatterySocket empty = socket(helper, 5, 0);
		helper.setBlock(new BlockPos(3, 1, 2), ModBlocks.red_cable.get());
		helper.setBlock(new BlockPos(4, 1, 2), ModBlocks.red_cable.get());

		helper.runAfterDelay(40, () -> {
			long got = empty.getPower();
			// a lead battery discharges 1,000 HE per tick
			helper.assertTrue(got > 0 && got <= 40 * 1_000L, "the empty battery charges from the full one, got " + got);
			helper.assertTrue(full.getPower() == capacity - got, "what one lost the other got");
			helper.assertTrue(empty.getComparatorPower() == 0 && full.getComparatorPower() == 15, "comparator output follows the charge");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void socketModeNone(GameTestHelper helper) {
		long capacity = EnumBatteryPack.BATTERY_LEAD.capacity;
		TileEntityBatterySocket full = socket(helper, 1, capacity);
		full.redLow = TileEntityBatteryBase.mode_none;
		TileEntityBatterySocket empty = socket(helper, 5, 0);
		helper.setBlock(new BlockPos(3, 1, 2), ModBlocks.red_cable.get());
		helper.setBlock(new BlockPos(4, 1, 2), ModBlocks.red_cable.get());

		helper.runAfterDelay(20, () -> {
			helper.assertTrue(empty.getPower() == 0 && full.getPower() == capacity, "disabled sockets don't take part");
			helper.succeed();
		});
	}
}
