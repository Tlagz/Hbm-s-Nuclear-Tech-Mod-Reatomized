package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityCrucible;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * Crucible, 3x3 and 2 high on top of a heater. Items dropped into it fall in (detailed hitboxes: a floor and four
 * walls), molten material can be poured in from above. A shovel empties it as scraps.
 */
public class MachineCrucible extends BlockDummyable implements ICrucibleAcceptor {

	public MachineCrucible(Properties properties) {
		super(properties);
		this.bounding.add(new AABB(-1.5D, 0D, -1.5D, 1.5D, 0.5D, 1.5D));
		this.bounding.add(new AABB(-1.25D, 0.5D, -1.25D, 1.25D, 1.5D, -1D));
		this.bounding.add(new AABB(-1.25D, 0.5D, -1.25D, -1D, 1.5D, 1.25D));
		this.bounding.add(new AABB(-1.25D, 0.5D, 1D, 1.25D, 1.5D, 1.25D));
		this.bounding.add(new AABB(1D, 0.5D, -1.25D, 1.25D, 1.5D, 1.25D));
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityCrucible(pos, state);
		return new TileEntityProxyCombo(pos, state).inventory();
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	private TileEntityCrucible getCrucible(Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		return core != null && world.getBlockEntity(core) instanceof TileEntityCrucible crucible ? crucible : null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(player.isShiftKeyDown() || !held.canPerformAction(ItemAbilities.SHOVEL_DIG)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if(world.isClientSide) return ItemInteractionResult.SUCCESS;

		TileEntityCrucible crucible = getCrucible(world, pos);
		if(crucible != null) {
			List<MaterialStack> stacks = new ArrayList<>();
			stacks.addAll(crucible.recipeStack);
			stacks.addAll(crucible.wasteStack);

			for(MaterialStack stack : stacks) {
				ItemStack scrap = ItemScraps.create(new MaterialStack(stack.material, stack.amount));
				if(!player.getInventory().add(scrap)) player.drop(scrap, false);
			}

			crucible.recipeStack.clear();
			crucible.wasteStack.clear();
			crucible.setChanged();
		}
		return ItemInteractionResult.SUCCESS;
	}

	/** The contents drop as scraps */
	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if(!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileEntityCrucible crucible) {
			List<MaterialStack> stacks = new ArrayList<>();
			stacks.addAll(crucible.recipeStack);
			stacks.addAll(crucible.wasteStack);
			for(MaterialStack stack : stacks) {
				Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ItemScraps.create(new MaterialStack(stack.material, stack.amount)));
			}
			crucible.recipeStack.clear();
			crucible.wasteStack.clear();
		}
		super.onRemove(state, world, pos, newState, moved);
	}

	@Override
	public boolean canAcceptPartialPour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		TileEntityCrucible crucible = getCrucible(world, pos);
		return crucible != null && crucible.canAcceptPartialPour(stack);
	}

	@Override
	public MaterialStack pour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		TileEntityCrucible crucible = getCrucible(world, pos);
		return crucible != null ? crucible.pour(stack) : stack;
	}

	@Override public boolean canAcceptPartialFlow(Level world, BlockPos pos, Direction side, MaterialStack stack) { return false; }
	@Override public MaterialStack flow(Level world, BlockPos pos, Direction side, MaterialStack stack) { return null; }
}
