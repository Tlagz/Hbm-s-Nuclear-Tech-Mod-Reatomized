package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineArcWelder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The arc welder: shapeless recipes from its three input slots, one input slot per port */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ArcWelderGameTests {

	private static TileEntityMachineArcWelder place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_arc_welder.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 3)), Direction.NORTH);
		TileEntityMachineArcWelder welder = (TileEntityMachineArcWelder) helper.getLevel().getBlockEntity(core);
		welder.setItem(4, new ItemStack(ModItems.battery_creative.get()));
		return welder;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void welderMakesMotors(GameTestHelper helper) {
		helper.assertTrue(ArcWelderRecipes.recipes.size() > 15, "the translated recipes should be registered");
		TileEntityMachineArcWelder welder = place(helper);

		// shapeless: the dense wires in the first slot, the plates in the last
		welder.setItem(0, ModItems.wire_dense.stack(Mats.MAT_MINGRADE, 2));
		welder.setItem(2, new ItemStack(ModItems.plate_steel.get(), 2));

		helper.succeedWhen(() -> {
			helper.assertTrue(welder.getItem(3).is(ModItems.motor.get()) && welder.getItem(3).getCount() == 2, "two steel plates and two dense wires should weld into 2 motors, got " + welder.getItem(3));
			helper.assertTrue(welder.getItem(0).isEmpty() && welder.getItem(2).isEmpty(), "the inputs should be used up");
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void welderPortsHaveTheirOwnSlots(GameTestHelper helper) {
		TileEntityMachineArcWelder welder = place(helper);
		BlockPos core = welder.getBlockPos();
		Direction dir = Direction.NORTH;
		Direction rot = dir.getClockWise();

		// the red port next to the core only reaches the first input (and the output)
		IItemHandler red = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, core.relative(rot), Direction.UP);
		helper.assertTrue(red != null && red.getSlots() == 2, "the red port should expose 2 slots, " + (red == null ? "none" : red.getSlots()));
		ItemStack left = red.insertItem(0, new ItemStack(ModItems.plate_steel.get(), 2), false);
		helper.assertTrue(left.isEmpty() && welder.getItem(0).is(ModItems.plate_steel.get()), "items put into the red port go into the first input");

		// the yellow port behind the core reaches the second input
		IItemHandler yellow = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, core.relative(dir.getOpposite()), Direction.UP);
		yellow.insertItem(0, ModItems.wire_dense.stack(Mats.MAT_MINGRADE, 2), false);
		helper.assertTrue(welder.getItem(1).is(ModItems.wire_dense.get(Mats.MAT_MINGRADE).get()), "items put into the yellow port go into the second input");
		helper.succeed();
	}
}
