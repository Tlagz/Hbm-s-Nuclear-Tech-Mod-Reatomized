package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntitySolarBoiler;
import com.hbm.tileentity.machine.TileEntitySolarMirror;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Solar tower: heliostats heat the boiler, the mirror tool aims them */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class SolarGameTests {

	private static TileEntitySolarBoiler placeBoiler(GameTestHelper helper) {
		return placeBoiler(helper, 1);
	}

	private static TileEntitySolarBoiler placeBoiler(GameTestHelper helper, int y) {
		BlockPos core = ModBlocks.machine_solar_boiler.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(8, y, 8)), Direction.NORTH);
		helper.assertTrue(core != null, "the boiler should fit");
		return (TileEntitySolarBoiler) helper.getLevel().getBlockEntity(core);
	}

	private static TileEntitySolarMirror placeMirror(GameTestHelper helper, BlockPos rel) {
		BlockPos pos = helper.absolutePos(rel);
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.solar_mirror.get().defaultBlockState());
		return (TileEntitySolarMirror) helper.getLevel().getBlockEntity(pos);
	}

	@GameTest(template = "empty_16x8x16", timeoutTicks = 60)
	public static void mirrorsBoilWater(GameTestHelper helper) {
		helper.getLevel().setDayTime(6000);
		TileEntitySolarBoiler boiler = placeBoiler(helper);
		BlockPos target = boiler.getBlockPos().above();

		// a 4x4 field of heliostats in the corner, 16 mirrors give at least 50 heat around noon
		for(int x = 1; x <= 4; x++) for(int z = 1; z <= 4; z++) {
			placeMirror(helper, new BlockPos(x, 1, z)).setTarget(target.getX(), target.getY(), target.getZ());
		}

		helper.onEachTick(() -> boiler.getWater().setFill(boiler.getWater().getMaxFill()));

		helper.runAfterDelay(40, () -> {
			TileEntitySolarMirror mirror = (TileEntitySolarMirror) helper.getBlockEntity(new BlockPos(1, 1, 1));
			helper.assertTrue(mirror.isOn && mirror.getSun() > 0, "mirrors work in daylight, sun " + mirror.getSun());
			helper.assertTrue(boiler.getSteam().getFill() > 0, "the boiler makes steam, has " + boiler.getSteam().getFill() + ", heat display " + boiler.display);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_16x8x16")
	public static void mirrorsNeedTheSky(GameTestHelper helper) {
		TileEntitySolarBoiler boiler = placeBoiler(helper);
		BlockPos target = boiler.getBlockPos().above();
		TileEntitySolarMirror mirror = placeMirror(helper, new BlockPos(2, 1, 2));
		mirror.setTarget(target.getX(), target.getY(), target.getZ());
		// a roof over the mirror instead of changing the time, other tests run in the same world at the same time
		helper.setBlock(new BlockPos(2, 3, 2), net.minecraft.world.level.block.Blocks.STONE);

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(!mirror.isOn, "no sun without a view of the sky");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_16x8x16")
	public static void mirrorToolLinksAndChecksTheAngle(GameTestHelper helper) {
		// up in the air so the close mirror looks up steeply enough
		TileEntitySolarBoiler boiler = placeBoiler(helper, 4);
		TileEntitySolarMirror close = placeMirror(helper, new BlockPos(7, 1, 7));
		TileEntitySolarMirror far = placeMirror(helper, new BlockPos(1, 1, 1));
		ItemStack tool = new ItemStack(ModItems.mirror_tool.get());

		// link to the boiler by clicking its top
		BlockPos top = boiler.getBlockPos().above(2);
		tool.getItem().useOn(new UseOnContext(helper.getLevel(), null, InteractionHand.MAIN_HAND, tool, new BlockHitResult(top.getCenter(), Direction.UP, top, false)));
		helper.assertTrue(boiler.getBlockPos().above().equals(tool.get(ModDataComponents.LINKED_POS.get())), "the tool remembers the block above the boiler core");

		tool.getItem().useOn(new UseOnContext(helper.getLevel(), null, InteractionHand.MAIN_HAND, tool, new BlockHitResult(close.getBlockPos().getCenter(), Direction.UP, close.getBlockPos(), false)));
		tool.getItem().useOn(new UseOnContext(helper.getLevel(), null, InteractionHand.MAIN_HAND, tool, new BlockHitResult(far.getBlockPos().getCenter(), Direction.UP, far.getBlockPos(), false)));

		helper.assertTrue(close.tY == boiler.getBlockPos().getY() + 1, "a mirror at a steep angle gets aimed");
		helper.assertTrue(far.tY == 0, "one too far to the side (under 45 degrees) doesn't");
		helper.succeed();
	}
}
