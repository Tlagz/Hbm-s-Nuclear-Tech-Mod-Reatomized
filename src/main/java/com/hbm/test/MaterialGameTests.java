package com.hbm.test;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.inventory.OreDictManager;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Materials: tags, hazards on tags, ore drops */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class MaterialGameTests {

	private static void near(GameTestHelper helper, float expected, float actual, String what) {
		helper.assertTrue(Math.abs(expected - actual) < 1e-4, what + " should be " + expected + ", is " + actual);
	}

	@GameTest(template = "empty_8x4x8")
	public static void materialTagsExist(GameTestHelper helper) {
		helper.assertTrue(new ItemStack(ModItems.ingot_uranium.get()).is(OreDictManager.tag(OreDictManager.U.ingot())), "uranium ingot should be in c:ingots/uranium");
		helper.assertTrue(new ItemStack(ModItems.ingot_uranium.get()).is(net.neoforged.neoforge.common.Tags.Items.INGOTS), "c:ingots should include it too");
		helper.assertTrue(new ItemStack(ModItems.ingot_u238.get()).is(OreDictManager.tag("ingotU238")), "aliases (U238) should work");
		helper.assertTrue(new ItemStack(ModBlocks.ore_uranium.get()).is(OreDictManager.tag("oreUranium")), "uranium ore should be in c:ores/uranium");
		helper.assertTrue(ModBlocks.ore_uranium.get().defaultBlockState().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, OreDictManager.tagLocation("oreUranium"))), "ore block tag");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void materialHazardsComeFromTags(GameTestHelper helper) {
		near(helper, HazardRegistry.pu * HazardRegistry.ingot, HazardSystem.getHazardLevelFromStack(new ItemStack(ModItems.ingot_plutonium.get()), HazardRegistry.RADIATION), "plutonium ingot radiation");
		near(helper, HazardRegistry.pu * HazardRegistry.nugget, HazardSystem.getHazardLevelFromStack(new ItemStack(ModItems.nugget_plutonium.get()), HazardRegistry.RADIATION), "plutonium nugget radiation");
		near(helper, HazardRegistry.u * HazardRegistry.block, HazardSystem.getHazardLevelFromStack(new ItemStack(ModBlocks.block_uranium.get()), HazardRegistry.RADIATION), "uranium block radiation");
		near(helper, 50F * HazardRegistry.ingot, HazardSystem.getHazardLevelFromStack(new ItemStack(ModItems.ingot_schrabidium.get()), HazardRegistry.BLINDING), "schrabidium blinding");
		near(helper, 3F * HazardRegistry.ingot, HazardSystem.getHazardLevelFromStack(new ItemStack(ModItems.ingot_pu238.get()), HazardRegistry.HOT), "pu238 heat");
		near(helper, 0F, HazardSystem.getHazardLevelFromStack(new ItemStack(ModItems.ingot_steel.get()), HazardRegistry.RADIATION), "steel radiation");
		near(helper, 1F, HazardSystem.getHazardLevelFromStack(new ItemStack(Items.GUNPOWDER), HazardRegistry.EXPLOSIVE), "gunpowder explosive");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void oresDropTheirResources(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);

		for(int i = 0; i < 20; i++) {
			List<ItemStack> sulfur = Block.getDrops(ModBlocks.ore_sulfur.get().defaultBlockState(), helper.getLevel(), pos, null, null, pickaxe);
			helper.assertTrue(sulfur.size() == 1 && sulfur.get(0).is(ModItems.sulfur.get()), "sulfur ore should drop sulfur, got " + sulfur);
			helper.assertTrue(sulfur.get(0).getCount() >= 2 && sulfur.get(0).getCount() <= 4, "sulfur ore drops 2-4, got " + sulfur.get(0).getCount());
		}

		List<ItemStack> titanium = Block.getDrops(ModBlocks.ore_titanium.get().defaultBlockState(), helper.getLevel(), pos, null, null, pickaxe);
		helper.assertTrue(titanium.size() == 1 && titanium.get(0).is(ModBlocks.ore_titanium.get().asItem()), "titanium ore should drop itself, got " + titanium);

		helper.assertTrue(ModBlocks.ore_uranium.get().defaultBlockState().requiresCorrectToolForDrops(), "ores need a pickaxe");
		helper.succeed();
	}
}
