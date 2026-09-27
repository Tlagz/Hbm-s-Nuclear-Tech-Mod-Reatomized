package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory;
import com.hbm.util.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/** The assembly factory: four recipe fields, water cooling, coolant-only ports and per-field item ports */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class AssemblyFactoryGameTests {

	private static TileEntityMachineAssemblyFactory place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_assembly_factory.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 2)), Direction.NORTH);
		TileEntityMachineAssemblyFactory factory = (TileEntityMachineAssemblyFactory) helper.getLevel().getBlockEntity(core);
		factory.setItem(0, new ItemStack(ModItems.battery_creative.get()));
		return factory;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void factoryMakesHazclothInFourthField(GameTestHelper helper) {
		TileEntityMachineAssemblyFactory factory = place(helper);
		factory.assemblerModule[3].setRecipe("ass.hazcloth", false);
		factory.setItem(5 + 3 * 14, new ItemStack(ModItems.powder_lead.get(), 8));
		factory.setItem(6 + 3 * 14, new ItemStack(Items.STRING, 16));

		helper.onEachTick(() -> {
			factory.water.setFill(4_000);
			factory.lps.setFill(0);
		});

		helper.succeedWhen(() -> {
			helper.assertTrue(factory.getItem(17 + 3 * 14).is(ModItems.hazmat_cloth.get()) && factory.getItem(17 + 3 * 14).getCount() == 8, "two crafts should make 8 hazmat cloth, output: " + factory.getItem(59));
			helper.assertTrue(factory.getItem(47).isEmpty() && factory.getItem(48).isEmpty(), "the inputs should be used up");
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void factoryNeedsCooling(GameTestHelper helper) {
		TileEntityMachineAssemblyFactory factory = place(helper);
		factory.assemblerModule[0].setRecipe("ass.hazcloth", false);
		factory.setItem(5, new ItemStack(ModItems.powder_lead.get(), 8));
		factory.setItem(6, new ItemStack(Items.STRING, 16));

		helper.runAfterDelay(40, () -> {
			helper.assertTrue(factory.assemblerModule[0].progress == 0 && factory.getItem(17).isEmpty(), "without water nothing should happen");
			factory.water.setFill(4_000);
		});
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(factory.assemblerModule[0].progress > 0 && factory.lps.getFill() > 0, "with water it starts working");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void factoryPorts(GameTestHelper helper) {
		TileEntityMachineAssemblyFactory factory = place(helper);
		BlockPos core = factory.getBlockPos();
		Direction dir = BlockDummyable.getRotation(factory.getBlockState());
		Direction rot = dir.getClockWise();

		TileEntityProxyCombo coolant = (TileEntityProxyCombo) helper.getLevel().getBlockEntity(core.relative(rot, -1).relative(dir, -2));
		helper.assertTrue(coolant.getCoreObject() instanceof TileEntityMachineAssemblyFactory.DelegateAssemblyFactory, "the coolant line dummy should hand out the delegate");
		helper.assertTrue(coolant.transferFluid(Fluids.WATER, 0, 1_000) == 0 && factory.water.getFill() == 1_000, "water should go into the coolant tank");

		factory.assemblerModule[1].setRecipe("ass.hazcloth", false);
		DirPos port = factory.getIOPos()[1];
		IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, port.relative(port.getDir().getOpposite()), port.getDir());
		helper.assertTrue(handler != null && handler.getSlots() == 16, "the port should expose its field's 12 inputs and the 4 outputs, has " + (handler == null ? -1 : handler.getSlots()));

		ItemStack rest = handler.insertItem(0, new ItemStack(ModItems.powder_lead.get(), 4), false);
		helper.assertTrue(rest.isEmpty() && factory.getItem(5 + 14).getCount() == 4, "the lead should go into the second field");
		helper.succeed();
	}
}
