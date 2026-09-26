package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.GasCentrifugeRecipes.PseudoFluidType;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineGasCent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Gas centrifuge enrichment and cascades (only the core blocks, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class GasCentGameTests {

	private static TileEntityMachineGasCent place(GameTestHelper helper, BlockPos rel) {
		BlockPos core = helper.absolutePos(rel);
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.machine_gascent.get().defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityMachineGasCent cent = (TileEntityMachineGasCent) helper.getLevel().getBlockEntity(core);
		cent.setPower(TileEntityMachineGasCent.maxPower);
		return cent;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void gasCentEnrichesUF6(GameTestHelper helper) {
		TileEntityMachineGasCent cent = place(helper, new BlockPos(3, 1, 3));
		cent.tank.setFill(2_000);

		helper.runAfterDelay(160, () -> {
			// the UF6 is taken into the natural UF6 stage, 150 ticks: 400mB into one U238 nugget and 300mB LEUF6
			helper.assertTrue(cent.tank.getFill() == 0 && cent.inputTank.getTankType() == PseudoFluidType.NUF6, "UF6 converted into the natural stage");
			helper.assertTrue(cent.inputTank.getFill() == 1_600 && cent.outputTank.getTankType() == PseudoFluidType.LEUF6 && cent.outputTank.getFill() == 300, "one enrichment step, in " + cent.inputTank.getFill() + " out " + cent.outputTank.getFill());
			helper.assertTrue(cent.getItem(0).is(ModItems.nugget_u238.get()), "a U238 nugget");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void gasCentCascade(GameTestHelper helper) {
		// facing north, the next centrifuge of the cascade sits south of the core
		TileEntityMachineGasCent first = place(helper, new BlockPos(3, 1, 3));
		TileEntityMachineGasCent second = place(helper, new BlockPos(3, 1, 4));
		first.outputTank.setFill(600);

		helper.runAfterDelay(12, () -> {
			helper.assertTrue(second.inputTank.getTankType() == PseudoFluidType.LEUF6 && second.outputTank.getTankType() == PseudoFluidType.MEUF6, "the next stage is set up");
			helper.assertTrue(first.outputTank.getFill() == 0 && second.inputTank.getFill() == 600, "the LEUF6 moved over");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void gasCentHighSpeedNeedsUpgrade(GameTestHelper helper) {
		TileEntityMachineGasCent cent = place(helper, new BlockPos(3, 1, 3));
		cent.tank.setFill(0);
		cent.inputTank.setTankType(PseudoFluidType.HEUF6);
		cent.outputTank.setTankType(PseudoFluidType.NONE);
		cent.inputTank.setFill(1_000);

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(!cent.isProgressing, "HEUF6 needs the GC speed upgrade");
			cent.setItem(6, new ItemStack(ModItems.upgrade_gc_speed.get()));
		});
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(cent.isProgressing && cent.getProcessingSpeed() == 80, "working at 80 ticks with the upgrade");
			helper.succeed();
		});
	}
}
