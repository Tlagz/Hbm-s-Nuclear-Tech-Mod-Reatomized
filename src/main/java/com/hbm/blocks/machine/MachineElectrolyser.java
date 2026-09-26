package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityElectrolyser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Electrolysis machine: an 11 long base with five cells on top and the metal spouts on both ends, ports on the ends.
 */
public class MachineElectrolyser extends BlockDummyable {

	private static final int[][] PARTS = new int[][] {
		new int[] {2, -1, 5, 5, 1, 1},
		new int[] {3, -3, 5, 5, 0, 0},
		new int[] {3, -1, 4, -4, -3, 3},
		new int[] {3, -1, 2, -2, -3, 3},
		new int[] {3, -1, 0, 0, -3, 3},
		new int[] {3, -1, -2, 2, -3, 3},
		new int[] {3, -1, -4, 4, -3, 3},
	};
	private static final int[] RAIL = new int[] {0, 0, 0, 0, -1, 2};

	public MachineElectrolyser(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityElectrolyser(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).inventory().power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 5, 5, 1, 3};
	}

	@Override
	public int getOffset() {
		return 5;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
		if(!super.checkRequirement(world, core, placed, dir)) return false;
		for(int[] part : PARTS) if(!MultiblockHandlerXR.checkSpace(world, core, part, placed, dir)) return false;
		for(int i = -4; i <= 4; i += 2) if(!MultiblockHandlerXR.checkSpace(world, core.relative(dir, i).above(3), RAIL, placed, dir)) return false;
		return true;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);

		for(int[] part : PARTS) MultiblockHandlerXR.fillSpace(world, core, part, this, dir);
		for(int i = -4; i <= 4; i += 2) MultiblockHandlerXR.fillSpace(world, core.relative(dir, i).above(3), RAIL, this, dir);

		Direction rot = dir.getClockWise();

		for(int end : new int[] {-5, 5}) {
			BlockPos e = core.relative(dir, end);
			this.makeExtra(world, e);
			this.makeExtra(world, e.relative(rot));
			this.makeExtra(world, e.relative(rot, -1));
		}
	}
}
