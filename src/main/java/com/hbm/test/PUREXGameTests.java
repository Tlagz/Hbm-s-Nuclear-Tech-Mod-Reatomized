package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.PUREXRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemPileRodMK2.EnumPileRod;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachinePUREX;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** PUREX: reprocessing a Chicago Pile plutonium rod with sulfuric acid */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class PUREXGameTests {

	@GameTest(template = "empty_8x12x8", timeoutTicks = 200)
	public static void purexReprocessesPileRods(GameTestHelper helper) {
		helper.assertTrue(PUREXRecipes.INSTANCE.recipeOrderedList.size() > 20, "the translated recipes should be registered, has " + PUREXRecipes.INSTANCE.recipeOrderedList.size());

		BlockPos core = ModBlocks.machine_purex.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 2)), Direction.NORTH);
		TileEntityMachinePUREX purex = (TileEntityMachinePUREX) helper.getLevel().getBlockEntity(core);
		purex.setItem(0, new ItemStack(ModItems.battery_creative.get()));

		purex.purexModule.setRecipe("purex.pilepu239", false);
		helper.assertTrue(purex.canPlaceItem(4, ModItems.pile_rod.stack(EnumPileRod.PU239)), "the input takes the plutonium rod");
		helper.assertTrue(purex.canPlaceItem(4, ModItems.pile_rod.stack(EnumPileRod.WASTE)), "the other pile rods are in the same autoswitch group");
		helper.assertFalse(purex.canPlaceItem(4, new ItemStack(ModItems.ingot_steel.get())), "unrelated items are not accepted");

		purex.setItem(4, ModItems.pile_rod.stack(EnumPileRod.PU239, 2));
		purex.inputTanks[0].setTankType(Fluids.SULFURIC_ACID);
		purex.inputTanks[0].setFill(1_000);

		helper.succeedWhen(() -> {
			helper.assertTrue(purex.getItem(4).isEmpty() && purex.inputTanks[0].getFill() == 800, "two rods and 200mB of acid should be used up");
			helper.assertTrue(purex.getItem(7).is(ModItems.billet_pu239.get()) && purex.getItem(7).getCount() == 4, "four plutonium billets, got " + purex.getItem(7));
			helper.assertTrue(purex.getItem(8).is(ModItems.billet_uranium.get()) && purex.getItem(8).getCount() == 2, "two uranium billets, got " + purex.getItem(8));
		});
	}
}
