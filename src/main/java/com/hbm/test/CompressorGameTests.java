package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineCompressorBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Compressors (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class CompressorGameTests {

	private static TileEntityMachineCompressorBase place(GameTestHelper helper, Block block) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, block.defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityMachineCompressorBase compressor = (TileEntityMachineCompressorBase) helper.getLevel().getBlockEntity(core);
		helper.onEachTick(() -> compressor.setPower(TileEntityMachineCompressorBase.maxPower));
		return compressor;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 140)
	public static void compressorRaisesPressure(GameTestHelper helper) {
		TileEntityMachineCompressorBase compressor = place(helper, ModBlocks.machine_compressor.get());
		compressor.tanks[0].setTankType(Fluids.WATER);
		compressor.tanks[0].setFill(5_000);

		helper.runAfterDelay(105, () -> {
			// no recipe: 1000mB per 100 ticks, one pressure step up
			helper.assertTrue(compressor.tanks[1].getTankType() == Fluids.WATER && compressor.tanks[1].getPressure() == 1 && compressor.tanks[1].getFill() == 1_000, "1000mB of water at 1 PU, got " + compressor.tanks[1].getFill());
			helper.assertTrue(compressor.tanks[0].getFill() == 4_000, "1000mB used");

			CompoundTag data = new CompoundTag();
			data.putInt("compression", 2);
			compressor.receiveControl(data);
			helper.assertTrue(compressor.tanks[0].getPressure() == 2 && compressor.tanks[1].getPressure() == 3, "the input pressure is picked in the GUI");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 80)
	public static void compressorRecipe(GameTestHelper helper) {
		TileEntityMachineCompressorBase compressor = place(helper, ModBlocks.machine_compressor_compact.get());
		compressor.tanks[0].setTankType(Fluids.PETROLEUM);

		// changing the pressure empties the tank (like the original), so pick it first
		CompoundTag data = new CompoundTag();
		data.putInt("compression", 1);
		compressor.receiveControl(data);
		helper.assertTrue(compressor.tanks[0].getFill() == 0, "a new pressure empties the input");
		compressor.tanks[0].setFill(4_000);

		helper.runAfterDelay(25, () -> {
			// pressurized petroleum into LPG in 20 ticks
			helper.assertTrue(compressor.tanks[1].getTankType() == Fluids.LPG && compressor.tanks[1].getPressure() == 0 && compressor.tanks[1].getFill() == 1_000, "2000mB of 1 PU petroleum into 1000mB LPG, got " + compressor.tanks[1].getFill());
			helper.succeed();
		});
	}
}
