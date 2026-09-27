package com.hbm.test;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.BlockConveyorBase;
import com.hbm.blocks.network.BlockConveyorBendable;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.items.tool.ItemConveyorWand;
import com.hbm.lib.RefStrings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Conveyor belts: items ride along, turn with curves, fall off the end; the wand builds whole routes */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ConveyorGameTests {

	/** A belt carrying items towards travel */
	private static void belt(GameTestHelper helper, BlockConveyorBase block, BlockPos rel, Direction travel) {
		BlockPos pos = helper.absolutePos(rel);
		BlockState state = block.getStateForMeta(travel.getOpposite().get3DDataValue());
		helper.getLevel().setBlockAndUpdate(pos, state);
	}

	private static EntityMovingItem ride(GameTestHelper helper, BlockPos rel, ItemStack stack) {
		EntityMovingItem item = new EntityMovingItem(helper.getLevel());
		item.setItemStack(stack);
		Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(rel)).add(0, 0.25, 0);
		item.moveTo(at.x, at.y, at.z, 0, 0);
		helper.getLevel().addFreshEntity(item);
		return item;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void itemsRideAndFallOff(GameTestHelper helper) {
		for(int x = 1; x <= 4; x++) belt(helper, ModBlocks.conveyor.get(), new BlockPos(x, 1, 3), Direction.EAST);
		EntityMovingItem item = ride(helper, new BlockPos(1, 1, 3), new ItemStack(Items.DIAMOND));
		double startX = item.getX();

		helper.runAfterDelay(20, () -> {
			helper.assertTrue(item.isAlive() && item.getX() > startX + 0.5, "the diamond moves east, x " + (item.getX() - startX));
			helper.assertTrue(Math.abs(item.getZ() - helper.absolutePos(new BlockPos(1, 1, 3)).getZ() - 0.5) < 0.01, "centered on the belt");
		});

		// 4 blocks at 1/16 per tick
		helper.runAfterDelay(90, () -> {
			helper.assertTrue(!item.isAlive(), "it left the belt");
			List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(5, 0, 2))).inflate(2));
			helper.assertTrue(dropped.stream().anyMatch(e -> e.getItem().is(Items.DIAMOND)), "and dropped off the end");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void expressIsFaster(GameTestHelper helper) {
		for(int x = 1; x <= 6; x++) belt(helper, ModBlocks.conveyor.get(), new BlockPos(x, 1, 2), Direction.EAST);
		for(int x = 1; x <= 6; x++) belt(helper, ModBlocks.conveyor_express.get(), new BlockPos(x, 1, 4), Direction.EAST);
		EntityMovingItem slow = ride(helper, new BlockPos(1, 1, 2), new ItemStack(Items.IRON_INGOT));
		EntityMovingItem fast = ride(helper, new BlockPos(1, 1, 4), new ItemStack(Items.GOLD_INGOT));
		double start = slow.getX();

		helper.runAfterDelay(30, () -> {
			double s = slow.getX() - start, f = fast.getX() - start;
			helper.assertTrue(f > s * 2.5, "the express belt is three times as fast, " + f + " vs " + s);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void itemsFollowCurves(GameTestHelper helper) {
		// east into a curve that turns south (right), then south
		belt(helper, ModBlocks.conveyor.get(), new BlockPos(1, 1, 1), Direction.EAST);
		BlockPos curve = helper.absolutePos(new BlockPos(2, 1, 1));
		helper.getLevel().setBlockAndUpdate(curve, ModBlocks.conveyor.get().getStateForMeta(Direction.WEST.get3DDataValue()).setValue(BlockConveyorBendable.CURVE, BlockConveyorBendable.Curve.RIGHT));
		helper.assertTrue(ModBlocks.conveyor.get().getOutputDirection(helper.getLevel(), curve) == Direction.SOUTH, "a right turn from travelling east goes south");
		for(int z = 2; z <= 5; z++) belt(helper, ModBlocks.conveyor.get(), new BlockPos(2, 1, z), Direction.SOUTH);
		EntityMovingItem item = ride(helper, new BlockPos(1, 1, 1), new ItemStack(Items.EMERALD));

		helper.runAfterDelay(60, () -> {
			BlockPos at = item.blockPosition();
			helper.assertTrue(item.isAlive() && at.getX() == curve.getX() && at.getZ() > curve.getZ(), "it went around the curve and south, at " + helper.relativePos(at));
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 60)
	public static void droppedItemsGetOnTheBelt(GameTestHelper helper) {
		for(int x = 1; x <= 5; x++) belt(helper, ModBlocks.conveyor.get(), new BlockPos(x, 1, 3), Direction.EAST);
		BlockPos drop = helper.absolutePos(new BlockPos(2, 1, 3));
		ItemEntity entity = new ItemEntity(helper.getLevel(), drop.getX() + 0.5, drop.getY() + 0.3, drop.getZ() + 0.5, new ItemStack(Items.APPLE));
		entity.setDeltaMovement(Vec3.ZERO);
		helper.getLevel().addFreshEntity(entity);

		helper.runAfterDelay(20, () -> {
			helper.assertTrue(!entity.isAlive(), "the dropped apple was picked up by the belt");
			List<EntityMovingItem> riding = helper.getLevel().getEntitiesOfClass(EntityMovingItem.class, new AABB(drop).inflate(4));
			helper.assertTrue(riding.stream().anyMatch(e -> e.getItemStack().is(Items.APPLE)), "and rides on it now");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void wandBuildsRoutes(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.CREATIVE);

		// clicking the east side of (1,1,2), then the west side of (6,1,2): four belts in between travelling east
		BlockPos a = helper.absolutePos(new BlockPos(1, 1, 2));
		BlockPos b = helper.absolutePos(new BlockPos(6, 1, 2));
		int used = ItemConveyorWand.construct(helper.getLevel(), true, ItemConveyorWand.ConveyorType.REGULAR, player,
				a.getX(), a.getY(), a.getZ(), Direction.EAST.get3DDataValue(), b.getX(), b.getY(), b.getZ(), Direction.WEST.get3DDataValue(), 64);
		helper.assertTrue(used == 4, "four belts, used " + used);
		for(int x = 2; x <= 5; x++) {
			BlockState state = helper.getBlockState(new BlockPos(x, 1, 2));
			helper.assertTrue(state.is(ModBlocks.conveyor.get()) && ModBlocks.conveyor.get().getOutputDirection(helper.getLevel(), helper.absolutePos(new BlockPos(x, 1, 2))) == Direction.EAST, "belt at " + x + " goes east, " + state);
		}

		// with a block in the way the build fails without placing anything
		helper.setBlock(new BlockPos(3, 1, 5), net.minecraft.world.level.block.Blocks.STONE);
		BlockPos c = helper.absolutePos(new BlockPos(1, 1, 5));
		BlockPos d = helper.absolutePos(new BlockPos(6, 1, 5));
		int blocked = ItemConveyorWand.construct(helper.getLevel(), false, ItemConveyorWand.ConveyorType.EXPRESS, player,
				c.getX(), c.getY(), c.getZ(), Direction.EAST.get3DDataValue(), d.getX(), d.getY(), d.getZ(), Direction.WEST.get3DDataValue(), 64);
		helper.assertTrue(blocked != 0, "a route around the stone or a failure, got " + blocked);
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void wandBuildsLiftsUp(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.CREATIVE);
		// onto the top of a two block pillar: needs lifts on the way up (this used to crash on the vertical turn)
		helper.setBlock(new BlockPos(5, 1, 3), net.minecraft.world.level.block.Blocks.STONE);
		helper.setBlock(new BlockPos(5, 2, 3), net.minecraft.world.level.block.Blocks.STONE);
		BlockPos a = helper.absolutePos(new BlockPos(1, 0, 3));
		BlockPos b = helper.absolutePos(new BlockPos(5, 2, 3));
		int used = ItemConveyorWand.construct(helper.getLevel(), true, ItemConveyorWand.ConveyorType.REGULAR, player,
				a.getX(), a.getY(), a.getZ(), Direction.UP.get3DDataValue(), b.getX(), b.getY(), b.getZ(), Direction.UP.get3DDataValue(), 64);
		helper.assertTrue(used > 0, "the route got built, " + used);

		int lifts = 0;
		for(int x = 0; x < 8; x++) for(int y = 1; y < 4; y++) for(int z = 0; z < 8; z++) {
			if(helper.getBlockState(new BlockPos(x, y, z)).is(ModBlocks.conveyor_lift.get())) lifts++;
		}
		helper.assertTrue(lifts > 0, "with lifts going up");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void screwdriverTurnsAndBends(GameTestHelper helper) {
		belt(helper, ModBlocks.conveyor.get(), new BlockPos(3, 1, 3), Direction.EAST);
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);

		ModBlocks.conveyor.get().onScrew(helper.getLevel(), player, pos, Direction.UP, 0, 0, 0, com.hbm.blocks.IToolable.ToolType.SCREWDRIVER);
		helper.assertTrue(ModBlocks.conveyor.get().getOutputDirection(helper.getLevel(), pos) == Direction.SOUTH, "turned clockwise: east -> south");

		player.setShiftKeyDown(true);
		for(int i = 0; i < 2; i++) ModBlocks.conveyor.get().onScrew(helper.getLevel(), player, pos, Direction.UP, 0, 0, 0, com.hbm.blocks.IToolable.ToolType.SCREWDRIVER);
		helper.assertTrue(helper.getLevel().getBlockState(pos).getValue(BlockConveyorBendable.CURVE) == BlockConveyorBendable.Curve.RIGHT, "straight -> left -> right");
		ModBlocks.conveyor.get().onScrew(helper.getLevel(), player, pos, Direction.UP, 0, 0, 0, com.hbm.blocks.IToolable.ToolType.SCREWDRIVER);
		Block now = helper.getLevel().getBlockState(pos).getBlock();
		helper.assertTrue(now == ModBlocks.conveyor_lift.get(), "the regular belt turns into a lift after the right bend, is " + now);
		helper.succeed();
	}
}
