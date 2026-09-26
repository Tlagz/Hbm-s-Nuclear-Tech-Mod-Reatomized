package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityCog;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityHeaterFirebox;
import com.hbm.tileentity.machine.TileEntityHeaterOven;
import com.hbm.tileentity.machine.TileEntityStirling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void ovenTakesHeatFromBelow(GameTestHelper helper) {
		TileEntityHeaterFirebox firebox = firebox(helper);
		BlockPos core = ModBlocks.heater_oven.get().placeMultiblock(helper.getLevel(), center(helper, 2), Direction.NORTH);
		TileEntityHeaterOven oven = (TileEntityHeaterOven) helper.getLevel().getBlockEntity(core);

		firebox.heatEnergy = 10_000;
		helper.succeedWhen(() -> helper.assertTrue(oven.heatEnergy > 0 && firebox.heatEnergy < 10_000, "the oven pulls the firebox's heat, has " + oven.heatEnergy));
	}
}
