package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes.CrystallizerRecipe;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineCrystallizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Ore acidizer recipes (item + acid) and processing (absolute positions) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class CrystallizerGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void crystallizerRecipesNeedTheRightAcid(GameTestHelper helper) {
		CrystallizerRecipe iron = CrystallizerRecipes.getOutput(new ItemStack(Items.IRON_ORE), Fluids.PEROXIDE);
		helper.assertTrue(iron != null && iron.output.is(ModItems.crystal_iron.get()) && iron.acidAmount == 500, "iron ore + peroxide = iron crystals");

		ItemStack uranium = new ItemStack(ModBlocks.ore_uranium.get());
		helper.assertTrue(CrystallizerRecipes.getOutput(uranium, Fluids.PEROXIDE) == null, "uranium needs sulfuric acid");
		CrystallizerRecipe uraniumRecipe = CrystallizerRecipes.getOutput(uranium, Fluids.SULFURIC_ACID);
		helper.assertTrue(uraniumRecipe != null && uraniumRecipe.output.is(ModItems.crystal_uranium.get()), "uranium ore + sulfuric acid = uranium crystals");
		helper.succeed();
	}

	@GameTest(template = "empty_8x12x8", timeoutTicks = 250)
	public static void crystallizerTansLeather(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		BlockPos pos = new BlockPos(Math.min(a.getX(), b.getX()) + 3, a.getY() + 1, Math.min(a.getZ(), b.getZ()) + 2);
		BlockPos core = ModBlocks.machine_crystallizer.get().placeMultiblock(helper.getLevel(), pos, Direction.NORTH);
		helper.assertTrue(core != null, "the acidizer should fit");
		TileEntityMachineCrystallizer crystallizer = (TileEntityMachineCrystallizer) helper.getLevel().getBlockEntity(core);

		crystallizer.setItem(0, new ItemStack(Items.ROTTEN_FLESH, 2));
		crystallizer.tank.setFill(8_000);
		helper.onEachTick(() -> crystallizer.setPower(TileEntityMachineCrystallizer.maxPower));

		helper.succeedWhen(() -> {
			helper.assertTrue(crystallizer.getItem(2).is(Items.LEATHER), "rotten flesh + peroxide should become leather, output: " + crystallizer.getItem(2));
			helper.assertTrue(crystallizer.tank.getFill() == 7_500 && crystallizer.getItem(0).getCount() == 1, "500mB acid and one flesh used, acid " + crystallizer.tank.getFill());
		});
	}
}
