package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineChemicalFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The chemical factory: four recipe fields, water cooling, coolant-only ports and per-field item ports */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ChemicalFactoryGameTests {

	private static TileEntityMachineChemicalFactory place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_chemical_factory.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 2)), Direction.NORTH);
		TileEntityMachineChemicalFactory factory = (TileEntityMachineChemicalFactory) helper.getLevel().getBlockEntity(core);
		factory.setItem(0, new ItemStack(ModItems.battery_creative.get()));
		return factory;
	}

	private static void acidSetup(TileEntityMachineChemicalFactory factory, int field) {
		factory.chemplantModule[field].setRecipe("chem.sulfuricacid", false);
		factory.inputTanks[field * 3].setTankType(Fluids.PEROXIDE);
		factory.inputTanks[field * 3].setFill(2_000);
		factory.inputTanks[field * 3 + 1].setTankType(Fluids.WATER);
		factory.inputTanks[field * 3 + 1].setFill(2_000);
		factory.setItem(5 + field * 7, new ItemStack(ModItems.sulfur.get(), 2));
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void factoryMakesAcidInSecondField(GameTestHelper helper) {
		TileEntityMachineChemicalFactory factory = place(helper);
		acidSetup(factory, 1);
		factory.water.setFill(4_000);

		// every working tick eats 100mB of water, a few ticks in some of it has to be steam
		helper.runAfterDelay(5, () -> helper.assertTrue(factory.water.getFill() < 4_000 && factory.lps.getFill() == 4_000 - factory.water.getFill(), "the cooling water turns into low pressure steam"));

		// then keep it cooled
		helper.onEachTick(() -> {
			if(helper.getTick() > 5) {
				factory.water.setFill(4_000);
				factory.lps.setFill(0);
			}
		});

		helper.succeedWhen(() -> {
			helper.assertTrue(factory.outputTanks[3].getTankType() == Fluids.SULFURIC_ACID && factory.outputTanks[3].getFill() == 4_000, "two operations should make 4000mB acid in the second field, has " + factory.outputTanks[3].getFill());
			helper.assertTrue(factory.getItem(12).isEmpty(), "the sulfur should be used up");
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void factoryNeedsCooling(GameTestHelper helper) {
		TileEntityMachineChemicalFactory factory = place(helper);
		acidSetup(factory, 0);

		helper.runAfterDelay(40, () -> {
			helper.assertTrue(factory.chemplantModule[0].progress == 0 && factory.outputTanks[0].getFill() == 0, "without water nothing should happen");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void factoryCoolantPortsOnlySeeCoolant(GameTestHelper helper) {
		TileEntityMachineChemicalFactory factory = place(helper);
		BlockPos core = factory.getBlockPos();
		Direction dir = BlockDummyable.getRotation(factory.getBlockState());
		Direction rot = dir.getClockWise();

		TileEntityProxyCombo coolant = (TileEntityProxyCombo) helper.getLevel().getBlockEntity(core.relative(rot).relative(dir, 2));
		TileEntityProxyCombo regular = (TileEntityProxyCombo) helper.getLevel().getBlockEntity(core.relative(dir, 2));

		helper.assertTrue(coolant.getCoreObject() instanceof TileEntityMachineChemicalFactory.DelegateChemicalFactory, "the coolant line dummy should hand out the delegate");
		helper.assertTrue(regular.getCoreObject() == factory, "the other dummies should go to the core");

		factory.inputTanks[0].setTankType(Fluids.PEROXIDE);
		helper.assertTrue(coolant.transferFluid(Fluids.PEROXIDE, 0, 1_000) == 1_000, "recipe fluids shouldn't go into the coolant port");
		helper.assertTrue(coolant.transferFluid(Fluids.WATER, 0, 1_000) == 0 && factory.water.getFill() == 1_000, "water should go into the coolant tank");
		helper.assertTrue(regular.transferFluid(Fluids.PEROXIDE, 0, 1_000) == 0 && factory.inputTanks[0].getFill() == 1_000, "recipe fluids go in through the other ports");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void factoryItemPortsFeedTheirField(GameTestHelper helper) {
		TileEntityMachineChemicalFactory factory = place(helper);
		factory.chemplantModule[2].setRecipe("chem.sulfuricacid", false);
		factory.chemplantModule[0].setRecipe("chem.sulfuricacid", false);

		// the third port is on the other side, one block to the front
		com.hbm.util.DirPos port = factory.getIOPos()[2];
		BlockPos dummy = port.relative(port.getDir().getOpposite());
		IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, dummy, port.getDir());
		helper.assertTrue(handler != null && handler.getSlots() == 15, "the port should expose its field's inputs and all outputs, has " + (handler == null ? -1 : handler.getSlots()));

		ItemStack rest = handler.insertItem(0, new ItemStack(ModItems.sulfur.get(), 4), false);
		helper.assertTrue(rest.isEmpty() && factory.getItem(5 + 2 * 7).getCount() == 4, "the sulfur should go into the third field");
		helper.assertTrue(factory.getItem(5).isEmpty(), "the first field shouldn't get anything");
		helper.succeed();
	}
}
