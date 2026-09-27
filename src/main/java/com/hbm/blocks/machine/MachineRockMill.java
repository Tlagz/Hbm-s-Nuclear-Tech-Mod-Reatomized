package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineRockMill;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Rock mill, 5x5 and 3 tall, two ports on every side */
public class MachineRockMill extends BlockDummyable {

	public MachineRockMill(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineRockMill(pos, state);
		if(meta >= 6) return new TileEntityProxyCombo(pos, state).inventory().power().fluid();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override public int[] getDimensions() { return new int[] {2, 0, 2, 2, 2, 2}; }
	@Override public int getOffset() { return 2; }

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);

		BlockPos core = pos.relative(dir, o);
		this.makeExtra(world, core.offset(2, 0, 1));
		this.makeExtra(world, core.offset(-2, 0, 1));
		this.makeExtra(world, core.offset(2, 0, -1));
		this.makeExtra(world, core.offset(-2, 0, -1));
		this.makeExtra(world, core.offset(1, 0, 2));
		this.makeExtra(world, core.offset(1, 0, -2));
		this.makeExtra(world, core.offset(-1, 0, 2));
		this.makeExtra(world, core.offset(-1, 0, -2));
	}
}
