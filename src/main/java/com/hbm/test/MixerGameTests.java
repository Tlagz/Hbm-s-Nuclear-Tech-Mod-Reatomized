package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineMixer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Mixer (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class MixerGameTests {

	private static TileEntityMachineMixer place(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.machine_mixer.get().defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityMachineMixer mixer = (TileEntityMachineMixer) helper.getLevel().getBlockEntity(core);
		helper.onEachTick(() -> mixer.setPower(TileEntityMachineMixer.maxPower));
		return mixer;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 80)
	public static void mixerMakesSulfuricAcid(GameTestHelper helper) {
		TileEntityMachineMixer mixer = place(helper);
		mixer.tanks[2].setTankType(Fluids.SULFURIC_ACID);
		mixer.tanks[0].setTankType(Fluids.PEROXIDE);
		mixer.tanks[0].setFill(1_600);
		mixer.setItem(1, new ItemStack(ModItems.sulfur.get(), 3));

		helper.runAfterDelay(55, () -> {
			// 50 ticks: 800mB peroxide and one sulfur into 500mB sulfuric acid
			helper.assertTrue(mixer.tanks[2].getFill() == 500 && mixer.tanks[0].getFill() == 800, "one batch of sulfuric acid, got " + mixer.tanks[2].getFill());
			helper.assertTrue(mixer.getItem(1).getCount() == 2, "one sulfur used");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void mixerCyclesRecipes(GameTestHelper helper) {
		TileEntityMachineMixer mixer = place(helper);
		mixer.tanks[2].setTankType(Fluids.FRACKSOL);

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(mixer.tanks[0].getTankType() == Fluids.SULFURIC_ACID && mixer.tanks[1].getTankType() == Fluids.PETROLEUM, "the first fracking solution recipe sets up the tanks");
			CompoundTag data = new CompoundTag();
			data.putBoolean("toggle", true);
			mixer.receiveControl(data);
		});
		helper.runAfterDelay(4, () -> {
			helper.assertTrue(mixer.recipeIndex == 1 && mixer.tanks[0].getTankType() == Fluids.WATER, "the button switches to the water recipe");
			helper.succeed();
		});
	}
}
