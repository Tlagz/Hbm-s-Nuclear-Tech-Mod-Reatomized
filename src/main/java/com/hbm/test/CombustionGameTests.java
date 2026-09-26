package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemPistons.EnumPistonType;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineCombustionEngine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Combustion engine: fuel grades against piston sets (absolute positions) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class CombustionGameTests {

	private static TileEntityMachineCombustionEngine engine(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		BlockPos pos = new BlockPos(Math.min(a.getX(), b.getX()) + 4, a.getY() + 1, Math.min(a.getZ(), b.getZ()) + 4);
		BlockPos core = ModBlocks.machine_combustion_engine.get().placeMultiblock(helper.getLevel(), pos, Direction.NORTH);
		helper.assertTrue(core != null, "the engine should fit");
		return (TileEntityMachineCombustionEngine) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_8x4x8")
	public static void dieselWithDeshPistons(GameTestHelper helper) {
		TileEntityMachineCombustionEngine engine = engine(helper);
		engine.tank.setTankType(Fluids.DIESEL);
		engine.tank.setFill(1_000);
		engine.setItem(2, ModItems.piston_set.stack(EnumPistonType.DESH));
		engine.isOn = true;
		engine.setting = 10;

		// the fuel values are calculated at startup (registerCalculatedFuel), not the ones in the fluid definitions
		long perTenth = Fluids.DIESEL.getTrait(com.hbm.inventory.fluid.trait.FT_Combustible.class).getCombustionEnergy() / 10_000;

		helper.succeedWhen(() -> {
			// 2mB/t, diesel is high grade (100% for desh pistons): the energy per bucket / 10,000 per tenth of a mB
			int burnedTenths = 10_000 - (engine.tank.getFill() * 10 + engine.tenth);
			helper.assertTrue(burnedTenths >= 200, "should burn 20 tenths of a mB per tick, burned " + burnedTenths);
			helper.assertTrue(engine.power == burnedTenths * perTenth, perTenth + " HE per tenth, power " + engine.power + " for " + burnedTenths);
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void wrongFuelGradeMakesNothing(GameTestHelper helper) {
		TileEntityMachineCombustionEngine engine = engine(helper);
		engine.tank.setTankType(Fluids.KEROSENE);
		engine.tank.setFill(1_000);
		engine.setItem(2, ModItems.piston_set.stack(EnumPistonType.STEEL));
		engine.isOn = true;
		engine.setting = 30;

		helper.runAfterDelay(10, () -> {
			// steel pistons can't burn aviation fuel at all
			helper.assertTrue(engine.power == 0 && engine.tank.getFill() == 1_000 && !engine.wasOn, "nothing should burn, power " + engine.power + ", fuel " + engine.tank.getFill());
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void engineNeedsPistons(GameTestHelper helper) {
		TileEntityMachineCombustionEngine engine = engine(helper);
		engine.tank.setTankType(Fluids.DIESEL);
		engine.tank.setFill(1_000);
		engine.isOn = true;
		engine.setting = 30;

		helper.runAfterDelay(10, () -> {
			helper.assertTrue(engine.power == 0 && engine.tank.getFill() == 1_000, "no piston set, no power");
			engine.setItem(2, new ItemStack(ModItems.piston_set.get(EnumPistonType.DURA).get()));
		});
		helper.runAfterDelay(20, () -> {
			helper.assertTrue(engine.power > 0, "running with dura pistons");
			helper.succeed();
		});
	}
}
