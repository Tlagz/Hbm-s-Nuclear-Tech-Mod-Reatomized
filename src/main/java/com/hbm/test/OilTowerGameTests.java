package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticCracker;
import com.hbm.tileentity.machine.oil.TileEntityMachineFractionTower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Fractioning tower (single and stacked) and catalytic cracker (absolute positions) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class OilTowerGameTests {

	private static BlockPos center(GameTestHelper helper, int size) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(size - 1, 0, size - 1));
		return new BlockPos(Math.min(a.getX(), b.getX()) + size / 2 - 1, a.getY() + 1, Math.min(a.getZ(), b.getZ()) + size / 2 - 1);
	}

	private static TileEntityMachineFractionTower tower(GameTestHelper helper, BlockPos core) {
		helper.assertTrue(core.equals(ModBlocks.machine_fraction_tower.get().placeMultiblock(helper.getLevel(), core.north(), Direction.NORTH)), "the tower should fit at " + core);
		return (TileEntityMachineFractionTower) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void fractionTowerSplitsHeavyOil(GameTestHelper helper) {
		TileEntityMachineFractionTower tower = tower(helper, center(helper, 8));
		tower.tanks[0].setTankType(Fluids.HEAVYOIL);
		tower.tanks[0].setFill(1_000);

		helper.succeedWhen(() -> {
			// 100mB heavy oil into 30 bitumen and 70 smear every 10 ticks
			helper.assertTrue(tower.tanks[1].getTankType() == Fluids.BITUMEN && tower.tanks[2].getTankType() == Fluids.SMEAR, "heavy oil fractions are bitumen and smear");
			helper.assertTrue(tower.tanks[1].getFill() >= 90 && tower.tanks[2].getFill() == tower.tanks[1].getFill() / 30 * 70, "30:70 split, has " + tower.tanks[1].getFill() + " / " + tower.tanks[2].getFill());
		});
	}

	@GameTest(template = "empty_8x12x8", timeoutTicks = 100)
	public static void stackedTowersShareWork(GameTestHelper helper) {
		BlockPos core = center(helper, 8);
		TileEntityMachineFractionTower bottom = tower(helper, core);
		TileEntityMachineFractionTower top = tower(helper, core.above(3));
		bottom.tanks[0].setTankType(Fluids.NAPHTHA);
		bottom.tanks[0].setFill(4_000);

		helper.succeedWhen(() -> {
			helper.assertTrue(top.tanks[0].getTankType() == Fluids.NAPHTHA, "the top segment takes the bottom's type");
			// the oil moves up, the fractions made above come down to the bottom segment
			helper.assertTrue(bottom.tanks[2].getTankType() == Fluids.DIESEL && bottom.tanks[2].getFill() >= 120, "both segments work, the diesel collects at the bottom, has " + bottom.tanks[2].getFill());
		});
	}

	@GameTest(template = "empty_12x20x12", timeoutTicks = 60)
	public static void crackerCracksOilWithSteam(GameTestHelper helper) {
		BlockPos core = center(helper, 12);
		BlockPos placed = ModBlocks.machine_catalytic_cracker.get().placeMultiblock(helper.getLevel(), core.north(3), Direction.NORTH);
		helper.assertTrue(core.equals(placed), "the cracker should fit, core " + placed);
		TileEntityMachineCatalyticCracker cracker = (TileEntityMachineCatalyticCracker) helper.getLevel().getBlockEntity(core);
		cracker.tanks[0].setTankType(Fluids.OIL);
		cracker.tanks[0].setFill(1_000);
		cracker.tanks[1].setFill(8_000);

		helper.succeedWhen(() -> {
			// 100mB oil + 200mB steam into 80 cracked oil, 20 petroleum and 2 spent steam
			helper.assertTrue(cracker.tanks[2].getTankType() == Fluids.CRACKOIL && cracker.tanks[3].getTankType() == Fluids.PETROLEUM, "oil cracks into cracked oil and petroleum");
			helper.assertTrue(cracker.tanks[2].getFill() >= 160 && cracker.tanks[4].getFill() == cracker.tanks[2].getFill() / 40, "80:20 and 2mB spent steam per crack, has " + cracker.tanks[2].getFill() + ", lps " + cracker.tanks[4].getFill());
			helper.assertTrue(cracker.tanks[1].getFill() == 8_000 - cracker.tanks[2].getFill() / 80 * 200, "200mB steam per crack");
		});
	}
}
