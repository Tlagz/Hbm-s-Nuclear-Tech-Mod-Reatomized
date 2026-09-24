package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineCapacitor;
import com.hbm.blocks.machine.MachineCapacitor.TileEntityCapacitor;
import com.hbm.blocks.machine.MachineCapacitorBus;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** First machines: electric furnace, capacitor, bus and cable working together */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class MachineGameTests {

	@GameTest(template = "empty_8x4x8", timeoutTicks = 300)
	public static void furnaceRunsOnCreativeBattery(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.machine_electric_furnace_off.get());
		TileEntityMachineElectricFurnace furnace = (TileEntityMachineElectricFurnace) helper.getBlockEntity(pos);
		furnace.setItem(0, new ItemStack(ModItems.battery_creative.get()));
		furnace.setItem(1, new ItemStack(Items.RAW_IRON, 2));

		helper.succeedWhen(() -> {
			helper.assertTrue(furnace.getItem(2).is(Items.IRON_INGOT) && furnace.getItem(2).getCount() == 2, "furnace should have smelted 2 iron, output: " + furnace.getItem(2));
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 400)
	public static void pressMakesPlates(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		ModBlocks.machine_press.get().placeMultiblock(helper.getLevel(), core, Direction.NORTH);
		com.hbm.tileentity.machine.TileEntityMachinePress press = (com.hbm.tileentity.machine.TileEntityMachinePress) helper.getLevel().getBlockEntity(core);
		press.setItem(0, new ItemStack(Items.COAL, 4));
		press.setItem(1, new ItemStack(ModItems.stamp_iron_plate.get()));
		press.setItem(2, new ItemStack(Items.IRON_INGOT, 3));

		helper.succeedWhen(() -> {
			helper.assertTrue(com.hbm.inventory.recipes.PressRecipes.recipes.size() > 10, "press recipes should be registered");
			helper.assertTrue(press.getItem(3).is(ModItems.plate_iron.get()), "the press should make iron plates, output: " + press.getItem(3) + ", press " + press.press + ", speed " + press.speed);
			helper.assertTrue(press.getItem(1).getDamageValue() >= 1, "the stamp should wear down");
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 150)
	public static void furnaceSpeedUpgrade(GameTestHelper helper) {
		// speed III: 25 ticks per item instead of 100, 4 items can only finish in time with the upgrade
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.machine_electric_furnace_off.get());
		TileEntityMachineElectricFurnace furnace = (TileEntityMachineElectricFurnace) helper.getBlockEntity(pos);
		furnace.setItem(0, new ItemStack(ModItems.battery_creative.get()));
		furnace.setItem(1, new ItemStack(Items.RAW_IRON, 4));
		furnace.setItem(3, new ItemStack(ModItems.upgrade_speed_3.get()));

		helper.succeedWhen(() -> {
			helper.assertTrue(furnace.maxProgress == 25, "speed III should cut the process time to 25, is " + furnace.maxProgress);
			helper.assertTrue(furnace.getItem(2).getCount() == 4, "4 iron should be smelted, output: " + furnace.getItem(2));
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 300)
	public static void capacitorPowersFurnaceThroughBusAndCable(GameTestHelper helper) {
		// capacitor front faces west (receiving side), its back (east) leads into a bus pointing east,
		// the bus outputs into a cable which is next to the furnace
		BlockPos capPos = new BlockPos(1, 1, 2);
		helper.setBlock(capPos, ModBlocks.capacitor_copper.get().defaultBlockState().setValue(MachineCapacitor.FACING, Direction.WEST));
		helper.setBlock(new BlockPos(2, 1, 2), ModBlocks.capacitor_bus.get().defaultBlockState().setValue(MachineCapacitorBus.FACING, Direction.EAST));
		helper.setBlock(new BlockPos(3, 1, 2), ModBlocks.red_cable.get());
		helper.setBlock(new BlockPos(4, 1, 2), ModBlocks.machine_electric_furnace_off.get());

		TileEntityCapacitor capacitor = (TileEntityCapacitor) helper.getBlockEntity(capPos);
		capacitor.setPower(1_000_000L);

		TileEntityMachineElectricFurnace furnace = (TileEntityMachineElectricFurnace) helper.getBlockEntity(new BlockPos(4, 1, 2));
		furnace.setItem(1, new ItemStack(Items.RAW_IRON));

		helper.succeedWhen(() -> {
			helper.assertTrue(furnace.getItem(2).is(Items.IRON_INGOT), "furnace should have smelted iron with capacitor power, furnace power: " + furnace.power + ", output: " + furnace.getItem(2));
			helper.assertTrue(capacitor.getPower() < 1_000_000L, "capacitor should have lost power");
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 400)
	public static void woodBurnerMultiblockPowersFurnace(GameTestHelper helper) {
		// facing north: dummies go 1 up, 1 back (south) and 1 to the side (east), the connection is 2 blocks behind the core
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 2));
		BlockPos placed = ModBlocks.machine_wood_burner.get().placeMultiblock(helper.getLevel(), core, Direction.NORTH);
		helper.assertTrue(core.equals(placed), "multiblock should have space");

		int dummies = 0;
		for(BlockPos p : BlockPos.betweenClosed(core.offset(-2, -1, -2), core.offset(2, 2, 2)))
			if(helper.getLevel().getBlockState(p).is(ModBlocks.machine_wood_burner.get())) dummies++;
		helper.assertTrue(dummies == 8, "wood burner should be 2x2x2, found " + dummies + " blocks");
		// facing north the machine extends to the east (ForgeDirection.getRotation(UP) is clockwise)
		helper.assertTrue(helper.getBlockEntity(new BlockPos(3, 1, 3)) instanceof TileEntityProxyCombo proxy && proxy.power, "back dummy should be a power proxy");
		helper.assertTrue(helper.getBlockEntity(new BlockPos(4, 1, 3)) instanceof TileEntityProxyCombo proxy2 && proxy2.power, "second back dummy should be a power proxy");

		helper.setBlock(new BlockPos(3, 1, 4), ModBlocks.red_cable.get());
		helper.setBlock(new BlockPos(3, 1, 5), ModBlocks.machine_electric_furnace_off.get());

		TileEntityMachineWoodBurner burner = (TileEntityMachineWoodBurner) helper.getLevel().getBlockEntity(core);
		burner.isOn = true;
		burner.setItem(0, new ItemStack(Items.OAK_LOG, 4));

		TileEntityMachineElectricFurnace furnace = (TileEntityMachineElectricFurnace) helper.getBlockEntity(new BlockPos(3, 1, 5));
		furnace.setItem(1, new ItemStack(Items.RAW_IRON));

		helper.succeedWhen(() -> {
			helper.assertTrue(burner.getItem(0).getCount() < 4, "burner should consume logs");
			helper.assertTrue(furnace.getItem(2).is(Items.IRON_INGOT), "furnace should smelt with wood burner power, burner power: " + burner.power + ", furnace: " + furnace.power);
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void breakingDummyRemovesMultiblock(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		ModBlocks.machine_wood_burner.get().placeMultiblock(helper.getLevel(), core, Direction.EAST);
		// not helper.destroyBlock(helper.relativePos(..)), relativePos is broken for unrotated tests
		helper.getLevel().destroyBlock(core.above(), false);

		helper.succeedWhen(() -> {
			for(BlockPos p : BlockPos.betweenClosed(core.offset(-2, -1, -2), core.offset(2, 2, 2)))
				helper.assertFalse(helper.getLevel().getBlockState(p).is(ModBlocks.machine_wood_burner.get()), "all parts should be gone, found " + helper.getLevel().getBlockState(p) + " at " + p.subtract(core));
		});
	}
}
