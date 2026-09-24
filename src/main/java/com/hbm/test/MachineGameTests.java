package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineCapacitor;
import com.hbm.blocks.machine.MachineCapacitor.TileEntityCapacitor;
import com.hbm.blocks.machine.MachineCapacitorBus;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;

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
}
