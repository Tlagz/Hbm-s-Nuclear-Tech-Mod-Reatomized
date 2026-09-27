package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.machine.TileEntitySlag;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Molten slag poured out by a slag tap: a block whose height follows its amount, flowing down and spreading sideways
 * on scheduled ticks, dropping its contents as scraps. Drawn by RenderSlag in the material's colors.
 */
public class BlockDynamicSlag extends Block implements EntityBlock {

	public BlockDynamicSlag(Properties properties) {
		super(properties.noOcclusion());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntitySlag(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if(world.getBlockEntity(pos) instanceof TileEntitySlag tile) {
			double height = Math.max((double) tile.amount / (double) TileEntitySlag.maxAmount, 0.0625D);
			return Shapes.box(0, 0, 0, 1, Math.min(height, 1D), 1);
		}
		return Shapes.block();
	}

	/** Places a new slag block with the given contents and lets it settle */
	public static TileEntitySlag place(Level world, BlockPos pos, MaterialStack stack, int amount) {
		world.setBlock(pos, ModBlocks.slag.get().defaultBlockState(), 3);
		if(!(world.getBlockEntity(pos) instanceof TileEntitySlag tile)) return null;
		tile.mat = stack.material;
		tile.amount = amount;
		tile.sync();
		world.scheduleTick(pos, ModBlocks.slag.get(), 1);
		return tile;
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {

		/* Error here, delete the block */
		if(!(world.getBlockEntity(pos) instanceof TileEntitySlag self) || self.mat == null) {
			world.removeBlock(pos, false);
			return;
		}

		BlockPos below = pos.below();

		/* Flow down */
		if(world.getBlockState(below).canBeReplaced() && below.getY() >= world.getMinBuildHeight()) {
			int amount = self.amount;
			world.removeBlock(pos, false);
			place(world, below, new MaterialStack(self.mat, amount), amount);
			return;
		} else if(world.getBlockEntity(below) instanceof TileEntitySlag lower) {

			if(lower.mat == self.mat && lower.amount < TileEntitySlag.maxAmount) {
				int transfer = Math.min(TileEntitySlag.maxAmount - lower.amount, self.amount);
				lower.amount += transfer;
				self.amount -= transfer;

				if(self.amount <= 0) {
					world.removeBlock(pos, false);
				} else {
					self.sync();
				}

				lower.sync();
				world.scheduleTick(below, this, 1);
				return;
			}
		}

		/* Flow sideways, no neighbors */
		int count = 0;
		for(Direction dir : Direction.Plane.HORIZONTAL) {
			if(world.getBlockState(pos.relative(dir)).canBeReplaced()) count++;
		}

		if(self.amount >= TileEntitySlag.maxAmount / 5 && count > 0) {
			int toSpread = Math.max(self.amount / (count * 2), 1);

			for(Direction dir : Direction.Plane.HORIZONTAL) {
				BlockPos side = pos.relative(dir);

				if(world.getBlockState(side).canBeReplaced()) {
					place(world, side, new MaterialStack(self.mat, toSpread), toSpread);
					self.amount -= toSpread;
				}
			}
			self.sync();
		}
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		List<ItemStack> ret = new ArrayList<>();
		if(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TileEntitySlag tile && tile.mat != null && tile.amount > 0) {
			ret.add(ItemScraps.create(new MaterialStack(tile.mat, tile.amount)));
		}
		return ret;
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos, Player player) {
		if(world.getBlockEntity(pos) instanceof TileEntitySlag tile && tile.mat != null) {
			return ItemScraps.create(new MaterialStack(tile.mat, tile.amount));
		}
		return ItemStack.EMPTY;
	}
}
