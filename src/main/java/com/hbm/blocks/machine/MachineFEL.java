package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityFEL;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Free electron laser, 3 wide, 7 long and 3 tall, power goes in at the back */
public class MachineFEL extends BlockDummyable {

	public MachineFEL(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityFEL(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).power();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 4, 2, 1, 1};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		this.makeExtra(world, pos.relative(dir, o - 4).above());
	}
}
