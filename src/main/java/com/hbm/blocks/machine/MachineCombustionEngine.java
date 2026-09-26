package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineCombustionEngine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Combustion engine, 6x2x2; power and fluid ports at the four corner extras */
public class MachineCombustionEngine extends BlockDummyable {

	public MachineCombustionEngine(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineCombustionEngine(pos, state);
		if(hasExtra(meta)) return new TileEntityProxyCombo(pos, state).power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 1, 0, 3, 2};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		Direction rot = dir.getClockWise();
		this.makeExtra(world, pos.relative(rot));
		this.makeExtra(world, pos.relative(rot, -1));
		this.makeExtra(world, pos.relative(dir, -1).relative(rot));
		this.makeExtra(world, pos.relative(dir, -1).relative(rot, -1));
	}
}
