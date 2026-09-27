package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.RockMillRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineRockMill;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The rock mill: crushing with random outputs and the colloid-to-clay recipe */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class RockMillGameTests {

	private static TileEntityMachineRockMill place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_rockmill.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 2)), Direction.NORTH);
		TileEntityMachineRockMill mill = (TileEntityMachineRockMill) helper.getLevel().getBlockEntity(core);
		mill.setItem(0, new ItemStack(ModItems.battery_creative.get()));
		return mill;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 260)
	public static void rockMillMakesClay(GameTestHelper helper) {
		helper.assertTrue(RockMillRecipes.INSTANCE.recipeOrderedList.size() == 9, "all nine recipes should be registered, has " + RockMillRecipes.INSTANCE.recipeOrderedList.size());
		TileEntityMachineRockMill mill = place(helper);

		mill.rockMillModule.setRecipe("rock.clay", false);
		mill.inputTanks[0].setTankType(Fluids.COLLOID);
		mill.inputTanks[0].setFill(2_500);
		mill.setItem(2, new ItemStack(net.minecraft.world.level.block.Blocks.SAND, 2));

		helper.succeedWhen(() -> {
			helper.assertTrue(mill.getItem(5).is(Items.CLAY_BALL) && mill.getItem(5).getCount() == 4, "four clay balls, got " + mill.getItem(5));
			helper.assertTrue(mill.getItem(2).isEmpty() && mill.inputTanks[0].getFill() == 0, "sand and colloid used up");
		});
	}

	/** Random outputs are only rolled into an empty slot, so the output has to be pulled out like by a hopper */
	@GameTest(template = "empty_8x4x8", timeoutTicks = 260)
	public static void rockMillCrushesCobble(GameTestHelper helper) {
		TileEntityMachineRockMill mill = place(helper);

		mill.rockMillModule.setRecipe("rock.cobble", false);
		mill.inputTanks[0].setTankType(Fluids.WATER);
		mill.inputTanks[0].setFill(1_000);
		mill.setItem(2, new ItemStack(Items.COBBLESTONE, 2));

		int[] made = new int[1];
		helper.onEachTick(() -> {
			ItemStack out = mill.getItem(5);
			if(!out.isEmpty()) {
				helper.assertTrue(out.is(Items.GRAVEL) || out.is(ModItems.powder_quartz.get()), "only gravel and quartz dust come out, got " + out);
				made[0] += out.getCount();
				mill.setItem(5, ItemStack.EMPTY);
			}
		});

		helper.succeedWhen(() -> {
			helper.assertTrue(made[0] == 2, "two cobblestone make two items, made " + made[0]);
			helper.assertTrue(mill.outputTanks[0].getTankType() == Fluids.COLLOID && mill.outputTanks[0].getFill() == 500 && mill.inputTanks[0].getFill() == 500, "the water turns into colloid");
		});
	}
}
