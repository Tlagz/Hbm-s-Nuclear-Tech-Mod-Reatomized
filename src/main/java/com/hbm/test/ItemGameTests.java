package com.hbm.test;

import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.items.machine.ItemBatteryPack.EnumBatteryPack;
import com.hbm.items.machine.ItemBreedingRod.BreedingRodType;
import com.hbm.lib.RefStrings;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Items ported from the original's ItemEnumMulti subclasses */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ItemGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void batteryPacksAndRods(GameTestHelper helper) {
		ItemStack lead = ModItems.battery_pack.stack(EnumBatteryPack.BATTERY_LEAD);
		IBatteryItem battery = (IBatteryItem) lead.getItem();
		helper.assertTrue(battery.getCharge(lead) == 0, "new battery packs are empty");
		helper.assertTrue(battery.getMaxCharge(lead) == EnumBatteryPack.BATTERY_LEAD.capacity && battery.getMaxCharge(lead) == 1_000L * 20 * 60 * 15, "lead battery capacity");
		battery.chargeBattery(lead, 5_000);
		helper.assertTrue(battery.getCharge(lead) == 5_000, "charging should add to the charge");
		helper.assertTrue(((ItemBatteryPack) ModItems.battery_pack.get(EnumBatteryPack.CAPACITOR_GOLD).get()).pack.isCapacitor(), "gold capacitor is a capacitor");

		ItemStack rod = ModItems.rod_dual.stack(BreedingRodType.U238);
		helper.assertTrue(rod.getCraftingRemainingItem().is(ModItems.rod_dual_empty.get()), "a dual rod leaves an empty dual rod when crafted");
		helper.succeed();
	}
}
