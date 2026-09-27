package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.BlockCraneBase;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityCraneExtractor;
import com.hbm.tileentity.network.TileEntityCraneGrabber;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Cranes: inserters fill inventories from belts, extractors put items onto belts, grabbers pull them off */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class CraneGameTests {

	private static void belt(GameTestHelper helper, int x, int z, Direction travel) {
		helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)), ModBlocks.conveyor.get().getStateForMeta(travel.getOpposite().get3DDataValue()));
	}

	private static void crane(GameTestHelper helper, Block crane, int x, int z, Direction input, Direction output) {
		BlockState state = crane.defaultBlockState().setValue(BlockCraneBase.INPUT, input).setValue(BlockCraneBase.OUTPUT, output);
		helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)), state);
	}

	private static ChestBlockEntity chest(GameTestHelper helper, int x, int z) {
		helper.setBlock(new BlockPos(x, 1, z), Blocks.CHEST);
		return (ChestBlockEntity) helper.getBlockEntity(new BlockPos(x, 1, z));
	}

	private static int count(ChestBlockEntity chest, net.minecraft.world.item.Item item) {
		int n = 0;
		for(int i = 0; i < chest.getContainerSize(); i++) if(chest.getItem(i).is(item)) n += chest.getItem(i).getCount();
		return n;
	}

	private static void ride(GameTestHelper helper, int x, int z, ItemStack stack) {
		EntityMovingItem item = new EntityMovingItem(helper.getLevel());
		item.setItemStack(stack);
		Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(x, 1, z))).add(0, 0.25, 0);
		item.moveTo(at.x, at.y, at.z, 0, 0);
		helper.getLevel().addFreshEntity(item);
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void inserterFillsChest(GameTestHelper helper) {
		for(int x = 1; x <= 3; x++) belt(helper, x, 3, Direction.EAST);
		crane(helper, ModBlocks.crane_inserter.get(), 4, 3, Direction.WEST, Direction.EAST);
		ChestBlockEntity target = chest(helper, 5, 3);
		ride(helper, 1, 3, new ItemStack(Items.DIAMOND, 3));

		helper.runAfterDelay(70, () -> {
			helper.assertTrue(count(target, Items.DIAMOND) == 3, "the diamonds went through the inserter into the chest, has " + count(target, Items.DIAMOND));
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 160)
	public static void extractorToInserter(GameTestHelper helper) {
		ChestBlockEntity source = chest(helper, 1, 3);
		source.setItem(0, new ItemStack(Items.IRON_INGOT, 2));
		// pulls from the chest at its output side (west), puts onto the belt at its input side (east)
		crane(helper, ModBlocks.crane_extractor.get(), 2, 3, Direction.EAST, Direction.WEST);
		for(int x = 3; x <= 4; x++) belt(helper, x, 3, Direction.EAST);
		crane(helper, ModBlocks.crane_inserter.get(), 5, 3, Direction.WEST, Direction.EAST);
		ChestBlockEntity target = chest(helper, 6, 3);

		helper.runAfterDelay(140, () -> {
			helper.assertTrue(count(source, Items.IRON_INGOT) == 0, "the extractor emptied the chest, one per second");
			helper.assertTrue(count(target, Items.IRON_INGOT) == 2, "and both ingots arrived at the other end, has " + count(target, Items.IRON_INGOT));
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void extractorWhitelist(GameTestHelper helper) {
		ChestBlockEntity source = chest(helper, 1, 3);
		source.setItem(0, new ItemStack(Items.DIRT, 8));
		source.setItem(1, new ItemStack(Items.GOLD_INGOT, 2));
		crane(helper, ModBlocks.crane_extractor.get(), 2, 3, Direction.EAST, Direction.WEST);
		TileEntityCraneExtractor extractor = (TileEntityCraneExtractor) helper.getBlockEntity(new BlockPos(2, 1, 3));
		extractor.setItem(0, new ItemStack(Items.GOLD_INGOT));
		extractor.matcher.initPatternStandard(helper.getLevel(), extractor.getItem(0), 0);
		extractor.isWhitelist = true;
		// no belt: it fills its own buffer

		helper.runAfterDelay(60, () -> {
			helper.assertTrue(count(source, Items.DIRT) == 8, "dirt isn't on the whitelist");
			helper.assertTrue(count(source, Items.GOLD_INGOT) == 0, "the gold got taken");
			int buffered = 0;
			for(int i = 9; i < 18; i++) if(extractor.getItem(i).is(Items.GOLD_INGOT)) buffered += extractor.getItem(i).getCount();
			helper.assertTrue(buffered == 2, "into the buffer, has " + buffered);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 120)
	public static void grabberPullsOffBelt(GameTestHelper helper) {
		for(int x = 0; x <= 6; x++) belt(helper, x, 3, Direction.EAST);
		// facing the belt with its input, the chest behind it
		crane(helper, ModBlocks.crane_grabber.get(), 3, 4, Direction.NORTH, Direction.SOUTH);
		ChestBlockEntity target = chest(helper, 3, 5);
		TileEntityCraneGrabber grabber = (TileEntityCraneGrabber) helper.getBlockEntity(new BlockPos(3, 1, 4));
		grabber.setItem(10, new ItemStack(com.hbm.items.ModItems.upgrade_ejector_3.get()));
		ride(helper, 0, 3, new ItemStack(Items.EMERALD));

		helper.runAfterDelay(90, () -> {
			helper.assertTrue(count(target, Items.EMERALD) == 1, "the grabber took the emerald off the belt, chest has " + count(target, Items.EMERALD));
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void craneSidesAndRotation(GameTestHelper helper) {
		// the original's ForgeDirection rotation table
		helper.assertTrue(BlockCraneBase.rotate(Direction.NORTH, Direction.UP) == Direction.EAST, "north around up is east");
		helper.assertTrue(BlockCraneBase.rotate(Direction.NORTH, Direction.DOWN) == Direction.WEST, "north around down is west");
		helper.assertTrue(BlockCraneBase.rotate(Direction.DOWN, Direction.NORTH) == Direction.EAST, "down around north is east");
		helper.assertTrue(BlockCraneBase.rotate(Direction.UP, Direction.WEST) == Direction.SOUTH, "up around west is south");

		BlockState state = ModBlocks.crane_inserter.get().defaultBlockState().setValue(BlockCraneBase.INPUT, Direction.WEST).setValue(BlockCraneBase.OUTPUT, Direction.EAST);
		BlockState turned = BlockCraneBase.withOutput(state, Direction.UP);
		helper.assertTrue(BlockCraneBase.getOutputSide(turned) == Direction.UP && BlockCraneBase.getInputSide(turned) == Direction.WEST, "sneak-screwdriver sets the output");
		BlockState swapped = BlockCraneBase.withInput(turned, Direction.UP);
		helper.assertTrue(BlockCraneBase.getInputSide(swapped) == Direction.UP && BlockCraneBase.getOutputSide(swapped) == Direction.WEST, "input onto the output side swaps them");
		BlockState flipped = BlockCraneBase.withInput(swapped, Direction.UP);
		helper.assertTrue(BlockCraneBase.getInputSide(flipped) == Direction.DOWN, "clicking the input side again flips it");
		helper.succeed();
	}
}
