package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.CokerRecipes;
import com.hbm.inventory.recipes.PyroOvenRecipes.PyroOvenRecipe;
import com.hbm.items.ItemEnums.EnumCokeType;
import com.hbm.items.ItemEnums.EnumTarType;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineCoker;
import com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven;
import com.hbm.util.Tuple.Triplet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Coker unit and pyrolysis oven (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class CokerGameTests {

	@SuppressWarnings("unchecked")
	private static <T> T place(GameTestHelper helper, Block block) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, block.defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		return (T) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void cokerCooksHeavyOil(GameTestHelper helper) {
		TileEntityMachineCoker coker = place(helper, ModBlocks.machine_coker.get());
		coker.tanks[0].setFill(16_000);
		coker.heat = TileEntityMachineCoker.maxHeat;

		Triplet<Integer, ItemStack, FluidStack> recipe = CokerRecipes.getOutput(Fluids.HEAVYOIL);
		helper.assertTrue(recipe != null && recipe.getY().is(ModItems.coke.get(EnumCokeType.PETROLEUM).get()) && recipe.getZ().type == Fluids.OIL_COKER, "heavy oil into petroleum coke and coker oil");

		helper.runAfterDelay(60, () -> {
			// without a heat source the stored heat decays, a hundredth of it goes into the 20,000 TU operation every tick
			helper.assertTrue(coker.getItem(1).getCount() == 1, "one coke after 20,000 TU, got " + coker.getItem(1));
			helper.assertTrue(coker.tanks[0].getFill() == 16_000 - recipe.getX() && coker.tanks[1].getFill() == recipe.getZ().fill, "oil used and byproduct made");
			helper.assertTrue(coker.heat <= TileEntityMachineCoker.maxHeat - 20_000, "the burned heat is gone, " + coker.heat);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 140)
	public static void pyroOvenMakesSyngas(GameTestHelper helper) {
		TileEntityMachinePyroOven pyro = place(helper, ModBlocks.machine_pyrooven.get());
		pyro.setPower(TileEntityMachinePyroOven.maxPower);
		pyro.tanks[0].setTankType(Fluids.STEAM);
		pyro.tanks[0].setFill(2_000);
		pyro.setItem(1, new ItemStack(Items.COAL, 2));

		helper.runAfterDelay(105, () -> {
			// 100 ticks at 10,000 HE: 500mB steam and coal into 1000mB syngas
			helper.assertTrue(pyro.tanks[1].getTankType() == Fluids.SYNGAS && pyro.tanks[1].getFill() == 1_000, "one bucket of syngas, got " + pyro.tanks[1].getFill());
			helper.assertTrue(pyro.tanks[0].getFill() == 1_500 && pyro.getItem(1).getCount() == 1, "500mB steam and one coal used");
			helper.assertTrue(pyro.smoke.getFill() > 0, "soot goes into the smoke tank");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void pyroOvenRecipeLookup(GameTestHelper helper) {
		TileEntityMachinePyroOven pyro = place(helper, ModBlocks.machine_pyrooven.get());

		pyro.setItem(1, new ItemStack(ModItems.oil_tar.get(EnumTarType.CRUDE).get(), 4));
		PyroOvenRecipe tar = pyro.getMatchingRecipe();
		helper.assertTrue(tar != null && tar.inputFluid == null && tar.outputFluid.type == Fluids.CARBONDIOXIDE && tar.outputItem.is(ModItems.powder_ash.get(com.hbm.items.ItemEnums.EnumAshType.SOOT).get()), "4 tar (any tar tag) into soot and CO2");

		pyro.setItem(1, ItemStack.EMPTY);
		pyro.tanks[0].setTankType(Fluids.DIESEL);
		PyroOvenRecipe diesel = pyro.getMatchingRecipe();
		helper.assertTrue(diesel != null && diesel.outputItem.is(ModItems.solid_fuel.get()) && diesel.inputItem == null, "diesel into solid fuel");

		pyro.setItem(1, new ItemStack(Items.COAL));
		helper.assertTrue(pyro.getMatchingRecipe() == null, "no diesel recipe with an item");
		helper.succeed();
	}
}
