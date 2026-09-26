package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineCompressor;
import com.hbm.tileentity.machine.TileEntityMachineCompressorCompact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The original's MachineCompressor (tower) and MachineCompressorCompact */
public abstract class MachineCompressor extends BlockDummyable {

	public MachineCompressor(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	/** 3x4 base with a 3x1x3 tower rising to 9 blocks */
	public static class Tower extends MachineCompressor {

		private static final int[] UPPER = new int[] {3, -3, 1, 1, 1, 1};
		private static final int[] TOWER = new int[] {8, -4, 0, 0, 1, 1};

		public Tower(Properties properties) { super(properties); }

		@Override
		public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
			if(meta >= 12) return new TileEntityMachineCompressor(pos, state);
			if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid().power();
			return null;
		}

		@Override public int[] getDimensions() { return new int[] {2, 0, 1, 2, 1, 1}; }
		@Override public int getOffset() { return 2; }

		@Override
		protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
			return super.checkRequirement(world, core, placed, dir) &&
					MultiblockHandlerXR.checkSpace(world, core, UPPER, placed, dir) &&
					MultiblockHandlerXR.checkSpace(world, core, TOWER, placed, dir);
		}

		@Override
		protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
			super.fillSpace(world, pos, dir, o);
			BlockPos core = pos.relative(dir, o);
			MultiblockHandlerXR.fillSpace(world, core, UPPER, this, dir);
			MultiblockHandlerXR.fillSpace(world, core, TOWER, this, dir);

			Direction rot = dir.getClockWise();
			this.makeExtra(world, core.relative(dir, -1));
			this.makeExtra(world, core.relative(rot));
			this.makeExtra(world, core.relative(rot, -1));
		}
	}

	/** 7x3x3 with the ports on the second layer */
	public static class Compact extends MachineCompressor {

		public Compact(Properties properties) { super(properties); }

		@Override
		public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
			if(meta >= 12) return new TileEntityMachineCompressorCompact(pos, state);
			if(meta >= extra) return new TileEntityProxyCombo(pos, state).power().fluid();
			return null;
		}

		@Override public int[] getDimensions() { return new int[] {2, 0, 1, 1, 3, 3}; }
		@Override public int getOffset() { return 1; }

		@Override
		protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
			super.fillSpace(world, pos, dir, o);
			BlockPos core = pos.relative(dir, o).above();
			Direction rot = dir.getClockWise();

			this.makeExtra(world, core.relative(rot, 3));
			this.makeExtra(world, core.relative(rot, -3));
			this.makeExtra(world, core.relative(dir).relative(rot));
			this.makeExtra(world, core.relative(dir).relative(rot, -1));
			this.makeExtra(world, core.relative(dir, -1).relative(rot));
			this.makeExtra(world, core.relative(dir, -1).relative(rot, -1));
		}
	}
}
