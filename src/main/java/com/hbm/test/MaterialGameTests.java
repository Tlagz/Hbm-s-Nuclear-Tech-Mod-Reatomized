package com.hbm.test;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
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

	@GameTest(template = "empty_8x4x8")
	public static void autogenItemsNamesAndTags(GameTestHelper helper) {
		helper.assertTrue(Mats.orderedList.size() > 100, "the materials should be defined, " + Mats.orderedList.size());
		helper.assertTrue(ModItems.wire_fine.materials().contains(Mats.MAT_COPPER) && !ModItems.wire_fine.materials().contains(Mats.MAT_IRON), "only materials with the wire shape get wires");

		ItemStack wire = ModItems.wire_fine.stack(Mats.MAT_COPPER);
		helper.assertTrue(wire.getHoverName().getString().equals("Copper Wire"), "the name should combine shape and material, is " + wire.getHoverName().getString());
		helper.assertTrue(wire.is(OreDictManager.tag("wireFineCopper")), "copper wire should be in hbm:fine_wires/copper");
		helper.assertTrue(ModItems.plate_cast.stack(Mats.MAT_STEEL).is(OreDictManager.tag("plateTripleSteel")), "cast steel plates should be in hbm:cast_plates/steel");
		// the original's cast plate texture override for bismuth is never used, bismuth has no cast plate shape
		helper.assertTrue(ModItems.plate_cast.stack(Mats.MAT_BISMUTH).isEmpty(), "bismuth has no cast plates");
		helper.assertFalse(ModItems.bedrock_ore_fragment.stack(Mats.MAT_BISMUTH).isEmpty(), "bismuth has bedrock ore fragments (with their own texture)");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void materialRecipes(GameTestHelper helper) {
		// crafting: 2 steel ingots on top of each other -> 16 bolts
		List<ItemStack> grid = new ArrayList<>();
		for(int i = 0; i < 9; i++) grid.add(i == 0 || i == 3 ? new ItemStack(ModItems.ingot_steel.get()) : ItemStack.EMPTY);
		CraftingInput input = CraftingInput.of(3, 3, grid);
		ItemStack bolts = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
				.map(r -> r.value().assemble(input, helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
		helper.assertTrue(bolts.is(ModItems.bolt.get(Mats.MAT_STEEL).get()) && bolts.getCount() == 16, "two steel ingots should make 16 steel bolts, got " + bolts);

		// press: wire stamp, copper ingot -> 8 copper wires
		ItemStack wires = PressRecipes.getOutput(new ItemStack(ModItems.ingot_copper.get()), new ItemStack(ModItems.stamp_iron_wire.get()));
		helper.assertTrue(wires != null && wires.is(ModItems.wire_fine.get(Mats.MAT_COPPER).get()) && wires.getCount() == 8, "the wire stamp should press copper into 8 wires, got " + wires);

		// anvil: 4 steel plates -> steel shell, 3 copper plates -> copper pipe
		boolean shell = AnvilRecipes.getConstruction().stream().anyMatch(r -> r.output.get(0).stack.is(ModItems.shell.get(Mats.MAT_STEEL).get()) && r.input.get(0).stacksize == 4);
		boolean pipe = AnvilRecipes.getConstruction().stream().anyMatch(r -> r.output.get(0).stack.is(ModItems.pipe.get(Mats.MAT_COPPER).get()) && r.input.get(0).stacksize == 3);
		helper.assertTrue(shell && pipe, "the anvil should make shells and pipes (shell " + shell + ", pipe " + pipe + ")");
		helper.succeed();
	}
}
