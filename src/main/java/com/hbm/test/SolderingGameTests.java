package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.items.machine.ItemCircuit.EnumCircuitType;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineSolderingStation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Soldering station recipes and processing (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class SolderingGameTests {

	private static ItemStack circuit(EnumCircuitType type, int count) {
		return new ItemStack(ModItems.circuit.get(type).get(), count);
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 140)
	public static void solderingMakesAnalogCircuit(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, ModBlocks.machine_soldering_station.get().defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		TileEntityMachineSolderingStation solderer = (TileEntityMachineSolderingStation) helper.getLevel().getBlockEntity(core);

		// the toppings in any of the three slots, and the order doesn't matter
		solderer.setItem(0, circuit(EnumCircuitType.CAPACITOR, 2));
		solderer.setItem(2, circuit(EnumCircuitType.VACUUM_TUBE, 3));
		solderer.setItem(3, circuit(EnumCircuitType.PCB, 4));
		solderer.setItem(5, new ItemStack(ModItems.wire_fine.get(Mats.MAT_LEAD).get(), 4));
		// the buffer only holds 20 ticks of consumption, keep it topped up like a power network would
		helper.onEachTick(() -> solderer.setPower(solderer.getMaxPower()));

		helper.assertTrue(SolderingRecipes.recipes.size() >= 16, "the generated recipes are registered");

		helper.runAfterDelay(110, () -> {
			// 100 ticks at 100 HE
			helper.assertTrue(solderer.getItem(6).is(ModItems.circuit.get(EnumCircuitType.ANALOG).get()), "an analog circuit, got " + solderer.getItem(6));
			helper.assertTrue(solderer.getItem(0).isEmpty() && solderer.getItem(2).isEmpty() && solderer.getItem(3).isEmpty() && solderer.getItem(5).isEmpty(), "all the ingredients used");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void solderingNeedsEveryIngredient(GameTestHelper helper) {
		ItemStack[] inputs = {
				circuit(EnumCircuitType.CAPACITOR, 2), circuit(EnumCircuitType.VACUUM_TUBE, 3), ItemStack.EMPTY,
				circuit(EnumCircuitType.PCB, 4), ItemStack.EMPTY,
				ItemStack.EMPTY};
		helper.assertTrue(SolderingRecipes.getRecipe(inputs) == null, "no solder, no recipe");
		inputs[5] = new ItemStack(ModItems.wire_fine.get(Mats.MAT_LEAD).get(), 4);
		helper.assertTrue(SolderingRecipes.getRecipe(inputs) != null, "with the solder it matches");
		inputs[1] = circuit(EnumCircuitType.VACUUM_TUBE, 2);
		helper.assertTrue(SolderingRecipes.getRecipe(inputs) == null, "too few vacuum tubes");
		helper.succeed();
	}
}
