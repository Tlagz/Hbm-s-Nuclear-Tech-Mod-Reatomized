package com.hbm.test;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.storage.TileEntityCrate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Storage crates: sizes and keeping the contents when broken */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class CrateGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void crateSizes(GameTestHelper helper) {
		int[] sizes = {36, 54, 104, 27, 15};
		for(int i = 0; i < sizes.length; i++) {
			BlockPos pos = helper.absolutePos(new BlockPos(1 + i, 1, 1));
			helper.getLevel().setBlockAndUpdate(pos, ModBlocks.CRATES.get(i).get().defaultBlockState());
			TileEntityCrate crate = (TileEntityCrate) helper.getLevel().getBlockEntity(pos);
			helper.assertTrue(crate.getContainerSize() == sizes[i], ModBlocks.CRATES.get(i).getId() + " has " + sizes[i] + " slots, got " + crate.getContainerSize());
		}
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void crateKeepsContents(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.crate_steel.get().defaultBlockState());
		TileEntityCrate crate = (TileEntityCrate) helper.getLevel().getBlockEntity(pos);
		crate.setItem(0, new ItemStack(Items.DIAMOND, 12));
		crate.setItem(53, new ItemStack(Items.IRON_INGOT, 64));
		crate.setCustomName("Loot");

		helper.getLevel().destroyBlock(pos, true);

		helper.runAfterDelay(1, () -> {
			List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2));
			helper.assertTrue(drops.size() == 1, "only the crate drops, got " + drops.size());
			ItemStack drop = drops.get(0).getItem();
			helper.assertTrue(drop.is(ModBlocks.crate_steel.get().asItem()), "the crate item");

			ItemContainerContents contents = drop.get(DataComponents.CONTAINER);
			helper.assertTrue(contents != null && contents.getSlots() == 54 && contents.getStackInSlot(0).getCount() == 12 && contents.getStackInSlot(53).is(Items.IRON_INGOT), "the contents are on the item");
			helper.assertTrue(Component.literal("Loot").equals(drop.get(DataComponents.CUSTOM_NAME)), "and the name");

			// placing it again restores everything
			BlockPos again = pos.east(2);
			helper.getLevel().setBlockAndUpdate(again, ModBlocks.crate_steel.get().defaultBlockState());
			TileEntityCrate placed = (TileEntityCrate) helper.getLevel().getBlockEntity(again);
			placed.applyComponentsFromItemStack(drop);
			helper.assertTrue(placed.getItem(0).is(Items.DIAMOND) && placed.getItem(0).getCount() == 12 && placed.getItem(53).getCount() == 64, "contents restored");
			helper.assertTrue("Loot".equals(placed.getInventoryName()), "name restored");
			helper.succeed();
		});
	}
}
