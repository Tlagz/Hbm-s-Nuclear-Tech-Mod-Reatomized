package com.hbm.test;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockScaffold;
import com.hbm.lib.RefStrings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Steel scaffolds: orientation from the clicked face and look direction, recoloring any scaffold with dye */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ScaffoldGameTests {

	private static int placedOrientation(GameTestHelper helper, Player player, Direction face, float yaw) {
		player.setYRot(yaw);
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), face, pos.relative(face.getOpposite()), false);
		BlockPlaceContext context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, new ItemStack(ModBlocks.steel_scaffold.get()), hit);
		return ModBlocks.steel_scaffold.get().getStateForPlacement(context).getValue(BlockScaffold.ORIENTATION);
	}

	@GameTest(template = "empty_8x4x8")
	public static void placementAndDyeing(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.CREATIVE);
		helper.assertTrue(placedOrientation(helper, player, Direction.UP, 0) == 0, "placed on the ground looking south: upright along x");
		helper.assertTrue(placedOrientation(helper, player, Direction.UP, 90) == 2, "placed on the ground looking west: upright along z");
		helper.assertTrue(placedOrientation(helper, player, Direction.NORTH, 0) == 1, "placed against a north face: flat");
		helper.assertTrue(placedOrientation(helper, player, Direction.EAST, 0) == 3, "placed against an east face: flat, turned");

		// any color can be recolored, the original matched the plain block with any metadata
		List<ItemStack> grid = new ArrayList<>();
		for(int i = 0; i < 9; i++) grid.add(new ItemStack(i == 4 ? Items.RED_DYE : (i % 2 == 0 ? ModBlocks.steel_scaffold_white.get() : ModBlocks.steel_scaffold_yellow.get()).asItem()));
		CraftingInput input = CraftingInput.of(3, 3, grid);
		ItemStack result = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
				.map(r -> r.value().assemble(input, helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
		helper.assertTrue(result.is(ModBlocks.steel_scaffold_red.get().asItem()) && result.getCount() == 8, "8 scaffolds of any color and red dye should make 8 red scaffolds, got " + result);
		helper.succeed();
	}
}
