package com.hbm.test;

import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.items.machine.ItemBatteryPack.EnumBatteryPack;
import com.hbm.items.machine.ItemArcElectrode;
import com.hbm.items.machine.ItemArcElectrode.EnumElectrodeType;
import com.hbm.items.machine.ItemBatterySC.EnumBatterySC;
import com.hbm.items.machine.ItemBreedingRod.BreedingRodType;
import com.hbm.items.machine.ItemChemicalDye.EnumChemDye;
import com.hbm.inventory.OreDictManager;
import com.hbm.lib.RefStrings;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
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

	@GameTest(template = "empty_8x4x8")
	public static void batteriesElectrodesAndDyes(GameTestHelper helper) {
		ItemStack sc = ModItems.battery_sc.stack(EnumBatterySC.PU238);
		IBatteryItem battery = (IBatteryItem) sc.getItem();
		battery.dischargeBattery(sc, 500);
		helper.assertTrue(battery.getCharge(sc) == 1_000 && battery.getDischargeRate(sc) == 1_000, "self-charging batteries are always full");

		ItemStack electrode = ModItems.arc_electrode.stack(EnumElectrodeType.GRAPHITE);
		for(int i = 0; i < 9; i++) helper.assertFalse(ItemArcElectrode.damage(electrode), "a graphite electrode lasts 10 uses, burnt after " + (i + 1));
		helper.assertTrue(ItemArcElectrode.damage(electrode), "a graphite electrode is burnt after 10 uses");

		helper.assertTrue(ModItems.chemical_dye.stack(EnumChemDye.SILVER).is(OreDictManager.tag("dyeLightGray")), "silver chemical dye is light gray dye");
		helper.assertTrue(ModItems.crayon.stack(EnumChemDye.RED).is(OreDictManager.tag("dye")), "crayons are dyes");
		helper.assertTrue(ModItems.crayon.stack(EnumChemDye.RED).has(net.minecraft.core.component.DataComponents.FOOD), "crayons can be eaten");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void ragGetsDampInWater(GameTestHelper helper) {
		BlockPos water = helper.absolutePos(new BlockPos(2, 1, 2));
		helper.getLevel().setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
		ItemEntity rag = new ItemEntity(helper.getLevel(), water.getX() + 0.5, water.getY() + 0.2, water.getZ() + 0.5, new ItemStack(ModItems.rag.get(), 3));
		helper.getLevel().addFreshEntity(rag);
		helper.succeedWhen(() -> helper.assertTrue(rag.getItem().is(ModItems.rag_damp.get()) && rag.getItem().getCount() == 3, "a rag in water should turn damp, is " + rag.getItem()));
	}
}
