package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineSiren;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Siren: loops play while powered, one-shots fire once per rising redstone edge */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class SirenGameTests {

	private static TileEntityMachineSiren place(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_siren.get().defaultBlockState());
		return (TileEntityMachineSiren) helper.getLevel().getBlockEntity(pos);
	}

	@GameTest(template = "empty_8x4x8")
	public static void sirenLoopsWhilePowered(GameTestHelper helper) {
		TileEntityMachineSiren siren = place(helper);
		siren.setItem(0, ModItems.siren_track.stack(TrackType.AIR_RAID));

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(siren.trackId == TrackType.AIR_RAID.ordinal() && !siren.active, "silent without redstone");
			helper.setBlock(new BlockPos(4, 1, 3), Blocks.REDSTONE_BLOCK);
		});

		helper.runAfterDelay(4, () -> {
			helper.assertTrue(siren.active, "the air raid siren runs while powered");
			helper.setBlock(new BlockPos(4, 1, 3), Blocks.AIR);
		});

		helper.runAfterDelay(6, () -> {
			helper.assertTrue(!siren.active, "and stops without it");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void sirenOneShotPerPulse(GameTestHelper helper) {
		TileEntityMachineSiren siren = place(helper);
		siren.setItem(0, ModItems.siren_track.stack(TrackType.RAZORTRAIN));
		helper.setBlock(new BlockPos(4, 1, 3), Blocks.REDSTONE_BLOCK);

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(siren.triggers == 1, "one horn per signal, not every tick, has " + siren.triggers);
			helper.setBlock(new BlockPos(4, 1, 3), Blocks.AIR);
		});

		helper.runAfterDelay(7, () -> helper.setBlock(new BlockPos(4, 1, 3), Blocks.REDSTONE_BLOCK));

		helper.runAfterDelay(9, () -> {
			helper.assertTrue(siren.triggers == 2, "the next pulse plays it again, has " + siren.triggers);
			helper.succeed();
		});
	}
}
