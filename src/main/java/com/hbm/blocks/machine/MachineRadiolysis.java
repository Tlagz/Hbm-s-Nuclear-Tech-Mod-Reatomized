package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineRadiolysis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * RTG and radiolysis chamber, 3x3 and 3 tall, the four bottom blocks next to the core are ports.
 *
 * TODO the original also marked the player for the FBI raid (BossSpawnHandler.markFBI)
 */
public class MachineRadiolysis extends BlockDummyable {

	public MachineRadiolysis(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineRadiolysis(pos, state);
		if(meta >= 6) return new TileEntityProxyCombo(pos, state).inventory().power().fluid();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);

		BlockPos core = pos.relative(dir, o);
		this.makeExtra(world, core.east());
		this.makeExtra(world, core.west());
		this.makeExtra(world, core.south());
		this.makeExtra(world, core.north());
	}
}
