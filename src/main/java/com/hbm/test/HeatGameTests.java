package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityCog;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityHeaterFirebox;
import com.hbm.tileentity.machine.TileEntityHeaterOven;
import com.hbm.tileentity.machine.TileEntityStirling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Heat consumers on a firebox: the Stirling engine and the heating oven (absolute positions) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class HeatGameTests {

	/** The placement position of a 3x3 machine with offset 1 whose core ends up in the middle of the area */
	private static BlockPos center(GameTestHelper helper, int y) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		return new BlockPos(Math.min(a.getX(), b.getX()) + 3, a.getY() + y, Math.min(a.getZ(), b.getZ()) + 2);
	}

	private static TileEntityHeaterFirebox firebox(GameTestHelper helper) {
		BlockPos core = ModBlocks.heater_firebox.get().placeMultiblock(helper.getLevel(), center(helper, 1), Direction.NORTH);
		return (TileEntityHeaterFirebox) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_8x4x8")
	public static void stirlingMakesPower(GameTestHelper helper) {
		TileEntityHeaterFirebox firebox = firebox(helper);
		BlockPos core = ModBlocks.machine_stirling.get().placeMultiblock(helper.getLevel(), center(helper, 2), Direction.NORTH);
		TileEntityStirling stirling = (TileEntityStirling) helper.getLevel().getBlockEntity(core);
		helper.assertTrue(core.below().equals(firebox.getBlockPos()), "the engine sits on the firebox's core");

		// 2000 TU in the firebox: 10% per tick is 200 TU/t, below the 300 the normal engine takes
		helper.onEachTick(() -> firebox.heatEnergy = 2_000);
		helper.succeedWhen(() -> {
			// the firebox loses some of its heat in its own tick first, so a bit less than 200 TU/t arrive
			helper.assertTrue(stirling.getPower() > 50 && stirling.getPower() <= 100, "about 200 TU/t should give up to 100 HE/t, got " + stirling.getPower());
			helper.assertTrue(stirling.hasCog, "no overspeed below the limit");
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 500)
	public static void stirlingOverspeedThrowsGear(GameTestHelper helper) {
		TileEntityHeaterFirebox firebox = firebox(helper);
		BlockPos core = ModBlocks.machine_stirling.get().placeMultiblock(helper.getLevel(), center(helper, 2), Direction.NORTH);
		TileEntityStirling stirling = (TileEntityStirling) helper.getLevel().getBlockEntity(core);

		// way too much heat for the iron gear
		helper.onEachTick(() -> firebox.heatEnergy = 50_000);
		helper.succeedWhen(() -> {
			helper.assertFalse(stirling.hasCog, "the gear should fly off after the overspeed limit");
			var cogs = helper.getLevel().getEntitiesOfClass(EntityCog.class, new AABB(core).inflate(64));
			helper.assertTrue(!cogs.isEmpty(), "the gear is an entity now");
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void electricHeaterMakesHeat(GameTestHelper helper) {
		BlockPos core = ModBlocks.heater_electric.get().placeMultiblock(helper.getLevel(), center(helper, 1).south(), Direction.NORTH);
		helper.assertTrue(core != null, "the heater should fit");
		com.hbm.tileentity.machine.TileEntityHeaterElectric heater = (com.hbm.tileentity.machine.TileEntityHeaterElectric) helper.getLevel().getBlockEntity(core);

		for(int i = 0; i < 3; i++) heater.toggleSetting();
		helper.assertTrue(heater.getHeatGen() == 300 && heater.getConsumption() == (long) (Math.pow(3, 1.4D) * 200D), "setting 3: 300 TU/t for " + heater.getConsumption() + " HE/t");

		helper.onEachTick(() -> heater.setPower(heater.getMaxPower()));
		helper.succeedWhen(() -> helper.assertTrue(heater.isOn && heater.getHeatStored() > 1000, "the heater should heat up while powered, has " + heater.getHeatStored()));
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 400)
	public static void steelFurnaceSmeltsWithHeat(GameTestHelper helper) {
		TileEntityHeaterFirebox firebox = firebox(helper);
		BlockPos core = ModBlocks.furnace_steel.get().placeMultiblock(helper.getLevel(), center(helper, 2), Direction.NORTH);
		com.hbm.tileentity.machine.TileEntityFurnaceSteel furnace = (com.hbm.tileentity.machine.TileEntityFurnaceSteel) helper.getLevel().getBlockEntity(core);

		furnace.setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_ORE, 4));
		helper.onEachTick(() -> firebox.heatEnergy = 100_000);
		helper.succeedWhen(() -> {
			helper.assertTrue(furnace.getItem(3).is(net.minecraft.world.item.Items.IRON_INGOT) && furnace.getItem(3).getCount() >= 5, "4 iron ore and the 25% ore bonus should give 5 ingots, got " + furnace.getItem(3));
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void ovenTakesHeatFromBelow(GameTestHelper helper) {
		TileEntityHeaterFirebox firebox = firebox(helper);
		BlockPos core = ModBlocks.heater_oven.get().placeMultiblock(helper.getLevel(), center(helper, 2), Direction.NORTH);
		TileEntityHeaterOven oven = (TileEntityHeaterOven) helper.getLevel().getBlockEntity(core);

		firebox.heatEnergy = 10_000;
		helper.succeedWhen(() -> helper.assertTrue(oven.heatEnergy > 0 && firebox.heatEnergy < 10_000, "the oven pulls the firebox's heat, has " + oven.heatEnergy));
	}

	/** The boiler takes the burner's heat through the heat source proxy on top of the 2 block tall burner */
	@GameTest(template = "empty_8x12x8", timeoutTicks = 200)
	public static void oilburnerHeatsBoilerThroughProxy(GameTestHelper helper) {
		BlockPos burnerCore = ModBlocks.heater_oilburner.get().placeMultiblock(helper.getLevel(), center(helper, 1), Direction.NORTH);
		com.hbm.tileentity.machine.TileEntityHeaterOilburner burner = (com.hbm.tileentity.machine.TileEntityHeaterOilburner) helper.getLevel().getBlockEntity(burnerCore);
		BlockPos boilerCore = ModBlocks.machine_boiler.get().placeMultiblock(helper.getLevel(), center(helper, 3), Direction.NORTH);
		helper.assertTrue(boilerCore != null && boilerCore.equals(burnerCore.above(2)), "the boiler sits on the burner");
		com.hbm.tileentity.machine.TileEntityHeatBoiler boiler = (com.hbm.tileentity.machine.TileEntityHeatBoiler) helper.getLevel().getBlockEntity(boilerCore);

		burner.isOn = true;
		for(int i = 0; i < 9; i++) burner.toggleSetting();
		helper.assertTrue(burner.setting == 10, "ten screwdriver turns from 1 wrap around at 10, is " + burner.setting);

		helper.onEachTick(() -> burner.tank.setFill(burner.tank.getMaxFill()));
		helper.succeedWhen(() -> {
			helper.assertTrue(boiler.heat > 10_000, "the boiler should heat up from the burner below, has " + boiler.heat + ", burner " + burner.heatEnergy);
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void heatexCoolsHotCoolant(GameTestHelper helper) {
		BlockPos core = ModBlocks.heater_heatex.get().placeMultiblock(helper.getLevel(), center(helper, 1), Direction.NORTH);
		com.hbm.tileentity.machine.TileEntityHeaterHeatex heatex = (com.hbm.tileentity.machine.TileEntityHeaterHeatex) helper.getLevel().getBlockEntity(core);

		// 100mB per cycle: 1mB hot coolant is 300 TU
		CompoundTag control = new CompoundTag();
		control.putInt("toCool", 100);
		heatex.receiveControl(control);

		helper.onEachTick(() -> {
			heatex.tanks[0].setFill(10_000);
			heatex.tanks[1].setFill(0);
		});
		helper.runAfterDelay(20, () -> {
			helper.assertTrue(heatex.tanks[1].getTankType() == com.hbm.inventory.fluid.Fluids.COOLANT, "hot coolant cools into coolant");
			helper.assertTrue(heatex.heatEnergy > 20 * 100 * 300 * 0.9 && heatex.heatEnergy <= 21 * 100 * 300, "about 30,000 TU per tick, has " + heatex.heatEnergy);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x12x8", timeoutTicks = 200)
	public static void industrialBoilerMakesSteam(GameTestHelper helper) {
		TileEntityHeaterFirebox firebox = firebox(helper);
		BlockPos core = ModBlocks.machine_industrial_boiler.get().placeMultiblock(helper.getLevel(), center(helper, 2), Direction.NORTH);
		com.hbm.tileentity.machine.TileEntityHeatBoilerIndustrial boiler = (com.hbm.tileentity.machine.TileEntityHeatBoilerIndustrial) helper.getLevel().getBlockEntity(core);

		boiler.tanks[0].setFill(64_000);
		helper.onEachTick(() -> firebox.heatEnergy = 100_000);
		helper.succeedWhen(() -> {
			helper.assertTrue(boiler.tanks[1].getTankType() == com.hbm.inventory.fluid.Fluids.STEAM && boiler.tanks[1].getMaxFill() == 6_400_000, "64,000mB water tank makes room for 6.4M steam");
			helper.assertTrue(boiler.tanks[1].getFill() > 0 && boiler.isOn, "the boiler should be boiling, has " + boiler.tanks[1].getFill() + "mB steam, " + boiler.heat + " TU");
		});
	}
}
