package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Turbofan: aviation fuel for power, afterburner, redstone shutoff and the blades */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class TurbofanGameTests {

	private static TileEntityMachineTurbofan place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_turbofan.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(8, 1, 8)), Direction.NORTH);
		helper.assertTrue(core != null, "the turbofan should fit");
		TileEntityMachineTurbofan fan = (TileEntityMachineTurbofan) helper.getLevel().getBlockEntity(core);
		fan.tank.setTankType(Fluids.KEROSENE);
		fan.tank.setFill(10_000);
		return fan;
	}

	@GameTest(template = "empty_16x8x16")
	public static void turbofanBurnsKerosene(GameTestHelper helper) {
		TileEntityMachineTurbofan fan = place(helper);

		helper.runAfterDelay(10, () -> {
			helper.assertTrue(fan.wasOn && fan.getPower() > 0, "kerosene makes power, has " + fan.getPower());
			helper.assertTrue(fan.tank.getFill() < 10_000, "and gets used up");
			long normal = fan.getPower();
			int used = 10_000 - fan.tank.getFill();

			// the afterburner burns more fuel for even more power
			fan.setPower(0);
			fan.tank.setFill(10_000);
			fan.setItem(2, new ItemStack(ModItems.upgrade_afterburn_3.get()));

			helper.runAfterDelay(10, () -> {
				helper.assertTrue(fan.afterburner == 3, "level 3 afterburner");
				helper.assertTrue(10_000 - fan.tank.getFill() > used * 3, "four times the fuel");
				helper.assertTrue(fan.getPower() > normal * 4, "more than four times the power, " + fan.getPower() + " vs " + normal);
				helper.succeed();
			});
		});
	}

	@GameTest(template = "empty_16x8x16")
	public static void turbofanStopsWithRedstone(GameTestHelper helper) {
		TileEntityMachineTurbofan fan = place(helper);
		// a redstone block next to one of the ports
		Direction dir = BlockDummyable.getRotation(fan.getBlockState()).getClockWise();
		BlockPos port = fan.getBlockPos().relative(dir.getCounterClockWise(), 2);
		helper.getLevel().setBlockAndUpdate(port.relative(dir.getCounterClockWise()), Blocks.REDSTONE_BLOCK.defaultBlockState());

		helper.runAfterDelay(10, () -> {
			helper.assertTrue(!fan.wasOn && fan.tank.getFill() == 10_000, "redstone at a port turns it off");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_16x8x16", timeoutTicks = 60)
	public static void turbofanShredsAndCollectsBlood(GameTestHelper helper) {
		TileEntityMachineTurbofan fan = place(helper);
		Direction dir = BlockDummyable.getRotation(fan.getBlockState()).getClockWise();
		BlockPos core = fan.getBlockPos();

		net.minecraft.world.entity.animal.Pig pig = new net.minecraft.world.entity.animal.Pig(EntityType.PIG, helper.getLevel());
		pig.setNoAi(true);
		pig.moveTo(core.getX() + 0.5 + dir.getStepX() * 3.6, core.getY(), core.getZ() + 0.5 + dir.getStepZ() * 3.6, 0, 0);
		helper.getLevel().addFreshEntity(pig);

		helper.runAfterDelay(20, () -> {
			helper.assertTrue(!pig.isAlive(), "the blades kill");
			// like the original, every tick the corpse spends in the blades counts, 50 mB each
			helper.assertTrue(fan.blood.getFill() >= 50 && fan.showBlood, "and the blood gets collected, has " + fan.blood.getFill());
			helper.succeed();
		});
	}
}
