package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineExposureChamber;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Exposure chamber: the 5x5 chamber with a long arm to one side (the particle feed), the ports at its end.
 *
 * TODO the detailed hitboxes (getAllDimensions)
 */
public class MachineExposureChamber extends BlockDummyable {

	/** The arm along the counter-clockwise side, relative to the core */
	private static final int[] ARM = new int[] {3, 0, 0, 0, -3, 8};
	/** The two rails on top of the arm, relative to the core two blocks up */
	private static final int[] RAIL_LEFT = new int[] {0, 0, 1, -1, -3, 6};
	private static final int[] RAIL_RIGHT = new int[] {0, 0, -1, 1, -3, 6};
	/** The two pillars at the end of the arm, relative to the core moved 7 blocks along the arm */
	private static final int[] PILLAR_LEFT = new int[] {3, 0, 1, -1, 0, 1};
	private static final int[] PILLAR_RIGHT = new int[] {3, 0, -1, 1, 0, 1};

	public MachineExposureChamber(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineExposureChamber(pos, state);
		if(meta >= 6) return new TileEntityProxyCombo(pos, state).inventory().power();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 2, 2, 2, 2};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	@Override
	protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
		if(!super.checkRequirement(world, core, placed, dir)) return false;
		BlockPos armEnd = core.relative(dir.getCounterClockWise(), 7);
		if(!MultiblockHandlerXR.checkSpace(world, core, ARM, placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, core.above(2), RAIL_LEFT, placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, core.above(2), RAIL_RIGHT, placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, armEnd, PILLAR_LEFT, placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, armEnd, PILLAR_RIGHT, placed, dir)) return false;
		return true;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);

		BlockPos core = pos.relative(dir, o);
		Direction rot = dir.getCounterClockWise(); // ForgeDirection.getRotation(UP).getOpposite()
		BlockPos armEnd = core.relative(rot, 7);

		MultiblockHandlerXR.fillSpace(world, core, ARM, this, dir);
		MultiblockHandlerXR.fillSpace(world, core.above(2), RAIL_LEFT, this, dir);
		MultiblockHandlerXR.fillSpace(world, core.above(2), RAIL_RIGHT, this, dir);
		MultiblockHandlerXR.fillSpace(world, armEnd, PILLAR_LEFT, this, dir);
		MultiblockHandlerXR.fillSpace(world, armEnd, PILLAR_RIGHT, this, dir);

		this.makeExtra(world, armEnd.relative(dir));
		this.makeExtra(world, armEnd.relative(dir, -1));
		this.makeExtra(world, core.relative(rot, 8).relative(dir));
		this.makeExtra(world, core.relative(rot, 8).relative(dir, -1));
		this.makeExtra(world, core.relative(rot, 8));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}
}
