package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.FluidDuctStandard;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Fuel burning generators */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class GeneratorGameTests {

	private static TileEntityMachineDiesel diesel(GameTestHelper helper, BlockPos pos) {
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_diesel.get().defaultBlockState());
		return (TileEntityMachineDiesel) helper.getLevel().getBlockEntity(pos);
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 300)
	public static void dieselChainBarrelPipeGeneratorCableFurnace(GameTestHelper helper) {
		BlockPos start = helper.absolutePos(new BlockPos(1, 1, 3));

		helper.getLevel().setBlockAndUpdate(start, ModBlocks.barrel_steel.get().defaultBlockState());
		TileEntityBarrel barrel = (TileEntityBarrel) helper.getLevel().getBlockEntity(start);
		barrel.tank.setTankType(Fluids.DIESEL);
		barrel.tank.setFill(16000);
		barrel.mode = 2;

		helper.getLevel().setBlockAndUpdate(start.east(), ModBlocks.fluid_duct_neo.get().defaultBlockState());
		FluidDuctStandard.setType(helper.getLevel(), start.east(), Fluids.DIESEL);

		TileEntityMachineDiesel diesel = diesel(helper, start.east(2));
		diesel.isOn = true;

		helper.getLevel().setBlockAndUpdate(start.east(3), ModBlocks.red_cable.get().defaultBlockState());
		helper.getLevel().setBlockAndUpdate(start.east(4), ModBlocks.machine_electric_furnace_off.get().defaultBlockState());
		TileEntityMachineElectricFurnace furnace = (TileEntityMachineElectricFurnace) helper.getLevel().getBlockEntity(start.east(4));
		furnace.setItem(1, new ItemStack(Items.RAW_IRON));

		helper.succeedWhen(() -> {
			helper.assertTrue(barrel.tank.getFill() < 16000, "barrel should feed the generator");
			helper.assertTrue(diesel.wasOn, "generator should be running");
			helper.assertTrue(diesel.smoke.getFill() > 0, "burning diesel should fill the smoke buffer");
			helper.assertTrue(furnace.getItem(2).is(Items.IRON_INGOT), "furnace should smelt with diesel power, generator power: " + diesel.power + ", furnace: " + furnace.power);
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void dieselRejectsLowGradeFuel(GameTestHelper helper) {
		TileEntityMachineDiesel diesel = diesel(helper, helper.absolutePos(new BlockPos(2, 1, 2)));
		diesel.tank.setTankType(Fluids.HEAVYOIL);
		diesel.tank.setFill(1000);
		diesel.isOn = true;

		helper.assertFalse(diesel.hasAcceptableFuel(), "heavy oil (LOW grade) must not be accepted");
		helper.runAtTickTime(40, () -> {
			helper.assertTrue(diesel.power == 0 && diesel.tank.getFill() == 1000, "nothing may burn, power " + diesel.power + ", fill " + diesel.tank.getFill());
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void dieselStopsWithRedstone(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
		TileEntityMachineDiesel diesel = diesel(helper, pos);
		diesel.tank.setFill(1000);
		diesel.isOn = true;
		helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.REDSTONE_BLOCK.defaultBlockState());

		helper.runAtTickTime(40, () -> {
			helper.assertTrue(diesel.tank.getFill() == 1000, "a powered generator must not burn fuel, fill " + diesel.tank.getFill());
			helper.succeed();
		});
	}
}
