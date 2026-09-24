package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidContainerBase;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineChemicalPlant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The chemical plant: fluid recipes from the tanks, fluid containers in the tank slots, item outputs */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ChemicalPlantGameTests {

	private static TileEntityMachineChemicalPlant place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_chemical_plant.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 3)), Direction.NORTH);
		TileEntityMachineChemicalPlant chemplant = (TileEntityMachineChemicalPlant) helper.getLevel().getBlockEntity(core);
		chemplant.setItem(0, new ItemStack(ModItems.battery_creative.get()));
		return chemplant;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void chemplantMakesSulfuricAcid(GameTestHelper helper) {
		helper.assertTrue(ChemicalPlantRecipes.INSTANCE.recipeOrderedList.size() > 40, "the translated recipes should be registered");
		TileEntityMachineChemicalPlant chemplant = place(helper);
		chemplant.chemplantModule.setRecipe("chem.sulfuricacid", false);

		// the recipe sets the tank types, the fill stays if the type already matches
		chemplant.inputTanks[0].setTankType(Fluids.PEROXIDE);
		chemplant.inputTanks[0].setFill(2_000);
		chemplant.inputTanks[1].setTankType(Fluids.WATER);
		chemplant.inputTanks[1].setFill(2_000);
		chemplant.setItem(4, new ItemStack(ModItems.sulfur.get(), 2));

		helper.succeedWhen(() -> {
			helper.assertTrue(chemplant.outputTanks[0].getTankType() == Fluids.SULFURIC_ACID, "the output tank should take sulfuric acid, is " + chemplant.outputTanks[0].getTankType().getName());
			helper.assertTrue(chemplant.outputTanks[0].getFill() == 4_000, "two operations should make 4000mB acid, has " + chemplant.outputTanks[0].getFill());
			helper.assertTrue(chemplant.getItem(4).isEmpty() && chemplant.inputTanks[0].getFill() == 0, "sulfur and peroxide should be used up");
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void chemplantMakesPolymerFromTankItem(GameTestHelper helper) {
		TileEntityMachineChemicalPlant chemplant = place(helper);
		chemplant.chemplantModule.setRecipe("chem.polymer", false);

		chemplant.setItem(4, new ItemStack(ModItems.powder_coal.get(), 2));
		chemplant.setItem(5, new ItemStack(ModItems.fluorite.get(), 1));
		// petroleum comes from a universal tank in the first fluid input slot
		chemplant.setItem(10, ItemFluidContainerBase.withFluid(ModItems.fluid_tank_full.get(), Fluids.PETROLEUM));

		helper.succeedWhen(() -> {
			helper.assertTrue(chemplant.getItem(13).is(ModItems.fluid_tank_empty.get()), "the emptied tank should be in the container output slot, is " + chemplant.getItem(13));
			helper.assertTrue(chemplant.getItem(7).is(ModItems.ingot_polymer.get()) && chemplant.getItem(7).getCount() == 4, "the chemical plant should make 4 polymer, output: " + chemplant.getItem(7));
		});
	}
}
