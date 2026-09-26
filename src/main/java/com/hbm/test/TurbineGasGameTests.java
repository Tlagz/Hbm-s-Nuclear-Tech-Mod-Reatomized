package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineTurbineGas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Gas turbine start/stop sequence and running (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class TurbineGasGameTests {

	private static TileEntityMachineTurbineGas place(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.machine_turbinegas.get().defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityMachineTurbineGas turbine = (TileEntityMachineTurbineGas) helper.getLevel().getBlockEntity(core);
		turbine.tanks[0].setFill(100_000);
		turbine.tanks[1].setFill(16_000);
		turbine.tanks[2].setFill(16_000);
		return turbine;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 620)
	public static void turbineGasStartsUp(GameTestHelper helper) {
		TileEntityMachineTurbineGas turbine = place(helper);

		CompoundTag data = new CompoundTag();
		data.putInt("state", -1);
		turbine.receiveControl(data);

		helper.runAfterDelay(300, () -> helper.assertTrue(turbine.state == -1 && turbine.rpm > 0 && turbine.rpm < 10, "still starting, rpm " + turbine.rpm));
		helper.runAfterDelay(590, () -> {
			helper.assertTrue(turbine.state == 1 && turbine.rpm == 10 && turbine.temp == 300, "running at idle after 580 ticks, rpm " + turbine.rpm + " temp " + turbine.temp);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 160)
	public static void turbineGasMakesPowerAndSteam(GameTestHelper helper) {
		TileEntityMachineTurbineGas turbine = place(helper);
		turbine.state = 1;
		turbine.rpm = 10;
		turbine.temp = 300;
		turbine.counter = 225;
		turbine.powerSliderPos = 60;

		helper.runAfterDelay(140, () -> {
			// 50mB natural gas per tick at full throttle plus 5% idle
			int used = 100_000 - turbine.tanks[0].getFill();
			helper.assertTrue(used >= 52 * 138 && used <= 53 * 141, "52.5mB gas per tick, used " + used);
			helper.assertTrue(turbine.rpm > 10 && turbine.temp > 300, "spinning up and heating, rpm " + turbine.rpm + " temp " + turbine.temp);
			helper.assertTrue(turbine.power > 0, "makes power");
			helper.assertTrue(turbine.tanks[3].getTankType() == Fluids.HOTSTEAM && turbine.tanks[3].getFill() == (16_000 - turbine.tanks[2].getFill()) * 10 && turbine.tanks[3].getFill() > 0, "boils water into hot steam 1:10");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void turbineGasStopsWithoutLube(GameTestHelper helper) {
		TileEntityMachineTurbineGas turbine = place(helper);
		turbine.state = 1;
		turbine.rpm = 10;
		turbine.counter = 225;
		turbine.tanks[1].setFill(0);

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(turbine.state == 0, "no lubricant, no running");
			helper.succeed();
		});
	}
}
