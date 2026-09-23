package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.BlockCable;
import com.hbm.lib.RefStrings;

import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for UNINOS / power networks, run with ./gradlew runGameTestServer */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class NetworkGameTests {

	private static PowerNode node(GameTestHelper helper, int x) {
		return Nodespace.getNode(helper.getLevel(), helper.absolutePos(new BlockPos(x, 1, 1)));
	}

	@GameTest(template = "empty_8x4x8")
	public static void cableLineFormsOneNetworkAndSplits(GameTestHelper helper) {
		for(int x = 1; x <= 5; x++) helper.setBlock(new BlockPos(x, 1, 1), ModBlocks.red_cable.get());

		BlockState middle = helper.getBlockState(new BlockPos(3, 1, 1));
		helper.assertTrue(middle.getValue(BlockCable.CONNECTIONS.get(Direction.EAST)) && middle.getValue(BlockCable.CONNECTIONS.get(Direction.WEST)), "middle cable should connect east and west");
		helper.assertFalse(middle.getValue(BlockCable.CONNECTIONS.get(Direction.UP)), "middle cable should not connect up");

		helper.runAfterDelay(5, () -> {
			PowerNode first = node(helper, 1);
			helper.assertTrue(first != null && first.hasValidNet(), "first cable should have a node with a valid net");
			for(int x = 2; x <= 5; x++) {
				PowerNode n = node(helper, x);
				helper.assertTrue(n != null && n.net == first.net, "cable " + x + " should share the first cable's net");
			}

			helper.destroyBlock(new BlockPos(3, 1, 1));

			helper.runAfterDelay(5, () -> {
				helper.assertTrue(node(helper, 3) == null, "broken cable's node should be gone");
				PowerNode left = node(helper, 1), right = node(helper, 5);
				helper.assertTrue(left.hasValidNet() && right.hasValidNet(), "both halves should have valid nets");
				helper.assertTrue(left.net != right.net, "halves should be separate nets");
				helper.assertTrue(node(helper, 2).net == left.net && node(helper, 4).net == right.net, "halves should be consistent");
				helper.succeed();
			});
		});
	}
}
