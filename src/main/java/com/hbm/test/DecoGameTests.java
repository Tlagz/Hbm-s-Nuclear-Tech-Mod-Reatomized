package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockGrate;
import com.hbm.blocks.generic.BlockMetalFence;
import com.hbm.blocks.generic.DecoBlock;
import com.hbm.lib.RefStrings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Steel walls, grates, deco pipes and metal fences */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class DecoGameTests {

	private static BlockState place(GameTestHelper helper, Player player, Block block, Direction face, double hitY) {
		BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
		BlockHitResult hit = new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + hitY, pos.getZ() + 0.5), face, pos.relative(face.getOpposite()), false);
		return block.getStateForPlacement(new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, new ItemStack(block), hit));
	}

	@GameTest(template = "empty_8x4x8")
	public static void placement(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.CREATIVE);
		player.setYRot(180);
		helper.assertTrue(place(helper, player, ModBlocks.steel_wall.get(), Direction.UP, 0).getValue(DecoBlock.FACING) == Direction.NORTH, "walls face where the player looks");
		helper.assertTrue(place(helper, player, ModBlocks.deco_pipe.get(), Direction.NORTH, 0.5).getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Z, "pipes run along the clicked face's axis");

		helper.assertTrue(place(helper, player, ModBlocks.steel_grate.get(), Direction.UP, 0).getValue(BlockGrate.LEVEL) == 0, "grates placed on a floor lie at the bottom");
		helper.assertTrue(place(helper, player, ModBlocks.steel_grate.get(), Direction.DOWN, 1).getValue(BlockGrate.LEVEL) == 7, "grates placed on a ceiling hang at the top");
		helper.assertTrue(place(helper, player, ModBlocks.steel_grate.get(), Direction.NORTH, 0.4).getValue(BlockGrate.LEVEL) == 3, "grates placed on a wall sit at the clicked height");

		// items fall through wide grates, not through normal ones
		BlockState wide = ModBlocks.steel_grate_wide.get().defaultBlockState();
		BlockState narrow = ModBlocks.steel_grate.get().defaultBlockState();
		ItemEntity item = new ItemEntity(helper.getLevel(), 0, 0, 0, new ItemStack(Items.IRON_INGOT));
		BlockPos pos = helper.absolutePos(BlockPos.ZERO);
		helper.assertTrue(wide.getCollisionShape(helper.getLevel(), pos, CollisionContext.of(item)).isEmpty(), "items fall through wide grates");
		helper.assertFalse(narrow.getCollisionShape(helper.getLevel(), pos, CollisionContext.of(item)).isEmpty(), "items stay on steel grates");
		helper.assertFalse(wide.getCollisionShape(helper.getLevel(), pos, CollisionContext.of(player)).isEmpty(), "players walk on wide grates");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void fencesConnect(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.CREATIVE);
		// absolute positions, the helper's relative ones are turned for these tests
		BlockPos a = helper.absolutePos(new BlockPos(2, 1, 2)), b = a.east();
		helper.getLevel().setBlockAndUpdate(a, ModBlocks.fence_metal.get().defaultBlockState());
		// hitting the air at b itself: air is replaceable, so the fence goes there
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(b), Direction.UP, b, false);
		BlockState placed = ModBlocks.fence_metal.get().getStateForPlacement(new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, new ItemStack(ModBlocks.fence_metal.get()), hit));
		helper.assertTrue(placed.getValue(BlockMetalFence.WEST) && !placed.getValue(BlockMetalFence.EAST), "a fence connects to the fence next to it");
		helper.getLevel().setBlockAndUpdate(b, placed);
		helper.assertTrue(helper.getLevel().getBlockState(a).getValue(BlockMetalFence.EAST), "the neighbor connects back");
		helper.assertFalse(BlockMetalFence.showPost(false, false, true, false, true), "straight fences hide the post");
		helper.assertTrue(BlockMetalFence.showPost(true, false, true, false, true), "fence posts always show it");
		helper.succeed();
	}
}
