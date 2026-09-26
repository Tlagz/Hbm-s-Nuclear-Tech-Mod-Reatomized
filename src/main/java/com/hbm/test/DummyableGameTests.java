package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.TileEntityProxyCombo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** BlockDummyable internals: proxy tiles (absolute positions) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class DummyableGameTests {

	private static BlockPos min(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		return new BlockPos(Math.min(a.getX(), b.getX()), a.getY() + 1, Math.min(a.getZ(), b.getZ()));
	}

	/** Chunk loading recreates tiles through the type factory and then loads the tag, the proxy flags must come back */
	@GameTest(template = "empty_8x4x8")
	public static void proxyFlagsSurviveReload(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_steam_engine.get().placeMultiblock(helper.getLevel(), min(helper).offset(3, 0, 7), Direction.SOUTH);
		helper.assertTrue(core != null, "the engine should fit");

		// the port side extra, one up and to the west of a south facing engine
		BlockPos extra = core.above().relative(Direction.SOUTH.getClockWise());
		helper.assertTrue(helper.getLevel().getBlockEntity(extra) instanceof TileEntityProxyCombo proxy && proxy.power && proxy.fluid, "the port block should be a power and fluid proxy");

		BlockEntity original = helper.getLevel().getBlockEntity(extra);
		CompoundTag tag = original.saveWithFullMetadata(helper.getLevel().registryAccess());
		BlockEntity reloaded = BlockEntity.loadStatic(extra, original.getBlockState(), tag, helper.getLevel().registryAccess());

		helper.assertTrue(reloaded instanceof TileEntityProxyCombo proxy && proxy.power && proxy.fluid && !proxy.inventory && !proxy.heat, "the flags should survive saving and loading");
		helper.succeed();
	}
}
