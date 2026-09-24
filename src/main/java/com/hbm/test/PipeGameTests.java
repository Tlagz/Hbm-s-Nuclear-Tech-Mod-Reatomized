package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.FluidDuctStandard;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.uninos.UniNodespace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Fluid pipes: connections depend on the fluid type, identifiers change the type, pipes join the fluid network */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class PipeGameTests {

	private static boolean connected(GameTestHelper helper, BlockPos pos, Direction dir) {
		BlockState state = helper.getLevel().getBlockState(pos);
		return state.getValue(FluidDuctStandard.CONNECTIONS.get(dir));
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void pipesConnectByType(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(new BlockPos(2, 1, 2));
		BlockPos b = a.east();
		BlockPos c = b.east();
		for(BlockPos pos : new BlockPos[] {a, b, c}) helper.getLevel().setBlockAndUpdate(pos, ModBlocks.fluid_duct_neo.get().defaultBlockState());

		// placed with setBlock, no placement context: let the pipes work out their connections
		for(BlockPos pos : new BlockPos[] {a, b, c}) FluidDuctStandard.refreshConnections(helper.getLevel(), pos);
		helper.assertTrue(connected(helper, a, Direction.EAST) && connected(helper, b, Direction.WEST) && connected(helper, b, Direction.EAST), "untyped pipes should connect to each other");

		// a different type in the middle cuts the line
		FluidDuctStandard.setType(helper.getLevel(), b, Fluids.DIESEL);
		helper.assertFalse(connected(helper, a, Direction.EAST), "a NONE pipe must not connect to a diesel pipe");
		helper.assertFalse(connected(helper, b, Direction.WEST) || connected(helper, b, Direction.EAST), "the diesel pipe must not connect to NONE pipes");

		// recursive change (sneak click) from the first pipe converts the rest of the NONE line, the diesel pipe stops it
		FluidDuctStandard.setType(helper.getLevel(), b, Fluids.NONE);
		((FluidDuctStandard) ModBlocks.fluid_duct_neo.get()).changeTypeRecursively(helper.getLevel(), a, Fluids.NONE, Fluids.KEROSENE, 64);
		for(BlockPos pos : new BlockPos[] {a, b, c}) {
			helper.assertTrue(FluidDuctStandard.getType(helper.getLevel(), pos) == Fluids.KEROSENE, "whole line should be kerosene, " + pos + " is " + FluidDuctStandard.getType(helper.getLevel(), pos).getName());
		}
		helper.assertTrue(connected(helper, a, Direction.EAST) && connected(helper, c, Direction.WEST), "kerosene line should be connected again");

		// the pipes join the kerosene network once they tick
		helper.succeedWhen(() -> {
			for(BlockPos pos : new BlockPos[] {a, b, c}) {
				helper.assertTrue(UniNodespace.getNode(helper.getLevel(), pos, Fluids.KEROSENE.getNetworkProvider()) != null, "pipe at " + pos + " should have a kerosene node");
			}
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void pipeConnectsToWoodBurnerBack(GameTestHelper helper) {
		// wood burner facing north, its fluid connections are 2 blocks behind the core (south)
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 2));
		ModBlocks.machine_wood_burner.get().placeMultiblock(helper.getLevel(), core, Direction.NORTH);

		BlockPos behind = core.south(2);
		BlockPos side = core.west();
		helper.getLevel().setBlockAndUpdate(behind, ModBlocks.fluid_duct_neo.get().defaultBlockState());
		helper.getLevel().setBlockAndUpdate(side, ModBlocks.fluid_duct_neo.get().defaultBlockState());
		FluidDuctStandard.refreshConnections(helper.getLevel(), behind);
		FluidDuctStandard.refreshConnections(helper.getLevel(), side);

		helper.assertTrue(connected(helper, behind, Direction.NORTH), "pipe behind the burner should connect to its fluid port");
		helper.assertFalse(connected(helper, side, Direction.EAST), "pipe at the burner's side must not connect");
		helper.succeed();
	}
}
