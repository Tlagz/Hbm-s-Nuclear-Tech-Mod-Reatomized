package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidContainerBase;
import com.hbm.items.machine.ItemFluidIDMulti;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Fluid containers, the container registry and tanks loading from item slots */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class FluidContainerGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void registryKnowsContainers(GameTestHelper helper) {
		ItemStack diesel = ItemFluidContainerBase.withFluid(ModItems.canister_full, Fluids.DIESEL);

		helper.assertTrue(FluidContainerRegistry.getFluidType(diesel) == Fluids.DIESEL, "diesel canister should hold diesel");
		helper.assertTrue(FluidContainerRegistry.getFluidContent(diesel, Fluids.DIESEL) == 1000, "canister should hold 1000mB");
		helper.assertTrue(FluidContainerRegistry.getEmptyContainer(diesel).is(ModItems.canister_empty.get()), "empty diesel canister should be a canister_empty");
		helper.assertTrue(FluidContainerRegistry.getFluidType(new ItemStack(Items.WATER_BUCKET)) == Fluids.WATER, "water bucket should be water");
		helper.assertTrue(FluidContainerRegistry.getFluidContent(PotionContents.createItemStack(Items.POTION, Potions.WATER), Fluids.WATER) == 250, "water bottle should be 250mB");
		helper.assertTrue(FluidContainerRegistry.getFluidType(PotionContents.createItemStack(Items.POTION, Potions.HEALING)) == Fluids.NONE, "healing potion must not count as water");

		ItemStack full = FluidContainerRegistry.getFullContainer(new ItemStack(ModItems.fluid_barrel_empty.get()), Fluids.DIESEL);
		helper.assertTrue(full != null && full.is(ModItems.fluid_barrel_full.get()) && ItemFluidContainerBase.getFluid(full) == Fluids.DIESEL, "filling an empty barrel with diesel should give a diesel barrel, got " + full);
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void tankLoadsAndUnloads(GameTestHelper helper) {
		FluidTank tank = new FluidTank(Fluids.DIESEL, 4000);
		NonNullList<ItemStack> slots = NonNullList.withSize(4, ItemStack.EMPTY);

		// load: two canisters in, empty canisters out
		slots.set(0, ItemFluidContainerBase.withFluid(ModItems.canister_full, Fluids.DIESEL).copyWithCount(2));
		tank.loadTank(0, 1, slots);
		tank.loadTank(0, 1, slots);
		helper.assertTrue(tank.getFill() == 2000, "tank should have 2000mB after two canisters, has " + tank.getFill());
		helper.assertTrue(slots.get(0).isEmpty(), "input should be used up");
		helper.assertTrue(slots.get(1).is(ModItems.canister_empty.get()) && slots.get(1).getCount() == 2, "output should be 2 empty canisters, is " + slots.get(1));

		// wrong fluid is ignored
		slots.set(0, ItemFluidContainerBase.withFluid(ModItems.canister_full, Fluids.KEROSENE));
		helper.assertFalse(tank.loadTank(0, 1, slots), "kerosene must not go into a diesel tank");

		// unload: empty barrel needs 16000, too much; empty canister works
		slots.set(2, new ItemStack(ModItems.canister_empty.get()));
		tank.unloadTank(2, 3, slots);
		helper.assertTrue(tank.getFill() == 1000 && ItemFluidContainerBase.getFluid(slots.get(3)) == Fluids.DIESEL, "unloading should fill a diesel canister, tank: " + tank.getFill() + ", out: " + slots.get(3));

		// infinite barrel
		slots.set(0, new ItemStack(ModItems.fluid_barrel_infinite.get()));
		tank.loadTank(0, 1, slots);
		helper.assertTrue(tank.getFill() == 4000, "infinite barrel should fill the tank");

		// identifier changes the type and clears the tank
		ItemStack id = new ItemStack(ModItems.fluid_identifier_multi.get());
		ItemFluidIDMulti.setType(id, Fluids.KEROSENE, true);
		slots.set(2, id);
		helper.assertTrue(tank.setType(2, slots) && tank.getTankType() == Fluids.KEROSENE && tank.getFill() == 0, "identifier should switch the tank to kerosene");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void woodBurnerBurnsDieselFromCanister(GameTestHelper helper) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		ModBlocks.machine_wood_burner.get().placeMultiblock(helper.getLevel(), core, Direction.NORTH);
		TileEntityMachineWoodBurner burner = (TileEntityMachineWoodBurner) helper.getLevel().getBlockEntity(core);

		ItemStack id = new ItemStack(ModItems.fluid_identifier_multi.get());
		ItemFluidIDMulti.setType(id, Fluids.DIESEL, true);
		burner.setItem(2, id);
		burner.setItem(3, ItemFluidContainerBase.withFluid(ModItems.canister_full, Fluids.DIESEL));
		burner.liquidBurn = true;
		burner.isOn = true;

		helper.succeedWhen(() -> {
			helper.assertTrue(burner.tank.getTankType() == Fluids.DIESEL, "tank should be set to diesel by the identifier");
			helper.assertTrue(burner.getItem(4).is(ModItems.canister_empty.get()), "empty canister should come out, slot 4: " + burner.getItem(4));
			helper.assertTrue(burner.tank.getFill() > 0 && burner.tank.getFill() < 1000, "diesel should be burning, fill: " + burner.tank.getFill());
			helper.assertTrue(burner.power > 0, "burning diesel should make power");
		});
	}
}
