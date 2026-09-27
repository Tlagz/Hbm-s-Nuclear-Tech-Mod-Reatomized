package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineMiningLaser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Mining laser: strips the layer below it, collects the drops, stops with redstone */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class MiningLaserGameTests {

	/** Hangs from y 7, the core ends up at y 6, the laser starts two blocks below at y 4 */
	private static TileEntityMachineMiningLaser place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_mining_laser.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(8, 7, 8)), Direction.NORTH);
		helper.assertTrue(core != null, "the mining laser should fit");
		TileEntityMachineMiningLaser laser = (TileEntityMachineMiningLaser) helper.getLevel().getBlockEntity(core);
		laser.setPower(TileEntityMachineMiningLaser.maxPower);
		laser.isOn = true;
		return laser;
	}

	@GameTest(template = "empty_16x8x16", timeoutTicks = 200)
	public static void laserMinesTheLayerBelow(GameTestHelper helper) {
		for(int x = 7; x <= 9; x++) for(int z = 7; z <= 9; z++) helper.setBlock(new BlockPos(x, 4, z), Blocks.DIRT);
		TileEntityMachineMiningLaser laser = place(helper);

		helper.runAfterDelay(150, () -> {
			for(int x = 7; x <= 9; x++) for(int z = 7; z <= 9; z++) helper.assertBlockNotPresent(Blocks.DIRT, new BlockPos(x, 4, z));
			int dirt = 0;
			for(int i = 9; i <= 29; i++) if(laser.getItem(i).is(Items.DIRT)) dirt += laser.getItem(i).getCount();
			helper.assertTrue(dirt >= 9, "all nine dirt blocks end up in the output, has " + dirt);
			helper.assertTrue(laser.getPower() < TileEntityMachineMiningLaser.maxPower, "mining uses power");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_16x8x16")
	public static void laserStopsWithRedstone(GameTestHelper helper) {
		helper.setBlock(new BlockPos(8, 4, 8), Blocks.DIRT);
		TileEntityMachineMiningLaser laser = place(helper);
		// right next to the east port
		helper.getLevel().setBlockAndUpdate(laser.getBlockPos().east(2), Blocks.REDSTONE_BLOCK.defaultBlockState());

		helper.runAfterDelay(20, () -> {
			helper.assertTrue(!laser.beam && laser.getPower() == TileEntityMachineMiningLaser.maxPower, "redstone stops it");
			helper.assertBlockPresent(Blocks.DIRT, new BlockPos(8, 4, 8));
			helper.succeed();
		});
	}

	@GameTest(template = "empty_16x8x16", timeoutTicks = 200)
	public static void laserMinesOilOreIntoTar(GameTestHelper helper) {
		helper.setBlock(new BlockPos(8, 4, 8), ModBlocks.ore_oil.get());
		TileEntityMachineMiningLaser laser = place(helper);

		helper.runAfterDelay(150, () -> {
			helper.assertBlockNotPresent(ModBlocks.ore_oil.get(), new BlockPos(8, 4, 8));
			// like the original: oil ore drops crude tar, the 500 mB of oil only come from the ore block itself (e.g. dropped by something else)
			boolean tar = false;
			for(int i = 9; i <= 29; i++) if(laser.getItem(i).is(com.hbm.items.ModItems.oil_tar.get(com.hbm.items.ItemEnums.EnumTarType.CRUDE).get())) tar = true;
			helper.assertTrue(tar, "oil ore gives crude tar");
			helper.succeed();
		});
	}
}
