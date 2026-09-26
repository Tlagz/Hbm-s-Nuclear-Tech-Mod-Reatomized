package com.hbm.test;

import java.math.BigInteger;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBatteryPack.EnumBatteryPack;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.storage.TileEntityBatteryBase;
import com.hbm.tileentity.machine.storage.TileEntityBatteryREDD;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** REDD energy storage */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class BatteryREDDGameTests {

	private static TileEntityBatteryREDD redd(GameTestHelper helper) {
		// facing north the core ends up 2 blocks south of the placed block, the structure spans z 5..9 and x 1..9
		BlockPos core = ModBlocks.machine_battery_redd.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(5, 1, 5)), Direction.NORTH);
		return (TileEntityBatteryREDD) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_12x20x12")
	public static void reddChargesSocket(GameTestHelper helper) {
		TileEntityBatteryREDD redd = redd(helper);
		redd.power = BigInteger.valueOf(1_000_000_000L);
		redd.redLow = TileEntityBatteryBase.mode_output;

		// a socket north of the REDD, joined through one cable at the REDD's north west port
		BlockPos socketCore = ModBlocks.machine_battery_socket.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 2)), Direction.NORTH);
		TileEntityBatterySocket socket = (TileEntityBatterySocket) helper.getLevel().getBlockEntity(socketCore);
		socket.setItem(0, new ItemStack(ModItems.battery_pack.get(EnumBatteryPack.BATTERY_LEAD).get()));
		helper.setBlock(new BlockPos(3, 1, 4), ModBlocks.red_cable.get());

		helper.runAfterDelay(40, () -> {
			long got = socket.getPower();
			helper.assertTrue(got > 0, "the socket charges from the REDD");
			helper.assertTrue(redd.power.equals(BigInteger.valueOf(1_000_000_000L - got)), "what the REDD lost the socket got, REDD " + redd.power + " socket " + got);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_12x20x12")
	public static void reddKeepsHugeCharge(GameTestHelper helper) {
		TileEntityBatteryREDD redd = redd(helper);
		BigInteger huge = BigInteger.TWO.pow(70); // more than a long can hold
		redd.power = huge;

		ItemStack drop = new ItemStack(ModBlocks.machine_battery_redd.get());
		drop.applyComponents(redd.collectComponents());
		CustomData data = drop.get(ModDataComponents.PERSISTENT.get());
		helper.assertTrue(data != null && new BigInteger(data.copyTag().getByteArray("power")).equals(huge), "the charge is on the item");

		helper.assertTrue(redd.getPower() == redd.getMaxPower() / 2, "offers at most half its connection limit at once");
		helper.succeed();
	}
}
