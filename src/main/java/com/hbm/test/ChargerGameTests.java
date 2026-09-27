package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.Charger;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBatteryPack.EnumBatteryPack;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityCharger;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The charger folds out for a player with an uncharged battery and charges it */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ChargerGameTests {

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void chargerChargesHeldBattery(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.charger.get().defaultBlockState().setValue(Charger.FACING, Direction.NORTH));
		TileEntityCharger charger = (TileEntityCharger) helper.getLevel().getBlockEntity(pos);

		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		ItemStack battery = ModItems.battery_pack.stack(EnumBatteryPack.BATTERY_LEAD);
		player.setItemInHand(InteractionHand.MAIN_HAND, battery);

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(charger.getMaxPower() > 0, "the charger should ask for power for the empty battery");
			helper.assertTrue(charger.transferPower(1_000) == 1_000, "it only charges once folded out");
		});

		helper.runAfterDelay(25, () -> {
			long left = charger.transferPower(1_000);
			IBatteryItem item = (IBatteryItem) player.getMainHandItem().getItem();
			long charged = item.getCharge(player.getMainHandItem());
			helper.assertTrue(charged > 0 && charged == 1_000 - left, "the held battery should be charged, has " + charged);
			helper.succeed();
		});
	}
}
