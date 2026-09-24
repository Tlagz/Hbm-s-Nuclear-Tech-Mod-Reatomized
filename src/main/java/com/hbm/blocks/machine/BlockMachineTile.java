package com.hbm.blocks.machine;

import java.util.function.Supplier;

import com.hbm.tileentity.TileEntityLoadedBase;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Common machine block behavior (the original's BlockContainer subclasses): creates the tile, ticks it on the
 * server, opens its GUI on right click if it has one and drops its inventory when broken.
 */
public abstract class BlockMachineTile extends Block implements EntityBlock {

	protected final Supplier<? extends BlockEntityType<?>> tileType;

	public BlockMachineTile(Properties properties, Supplier<? extends BlockEntityType<?>> tileType) {
		super(properties);
		this.tileType = tileType;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return tileType.get().create(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide || type != tileType.get() ? null : TileEntityLoadedBase.ticker();
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity tile = world.getBlockEntity(pos);

		if(!(tile instanceof MenuProvider provider)) return InteractionResult.PASS;
		if(player.isShiftKeyDown()) return InteractionResult.PASS;

		if(!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos));
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if(!state.is(newState.getBlock())) {
			if(world.getBlockEntity(pos) instanceof Container container) {
				Containers.dropContents(world, pos, container);
				world.updateNeighbourForOutputSignal(pos, this);
			}
		}
		super.onRemove(state, world, pos, newState, movedByPiston);
	}
}
