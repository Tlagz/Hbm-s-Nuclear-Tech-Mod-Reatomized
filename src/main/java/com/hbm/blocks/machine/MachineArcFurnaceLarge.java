package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Electric arc furnace, 5x5 with the spout sticking out the front. Power and items go in through the six ports
 * on the sides.
 * TODO emptying the molten contents with a shovel (ItemScraps)
 */
public class MachineArcFurnaceLarge extends BlockDummyable {

	/** The spout in front of the furnace */
	private static final int[] SPOUT = new int[] {4, 0, 3, -2, 1, 1};

	public MachineArcFurnaceLarge(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineArcFurnaceLarge(pos, state);
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
		return MultiblockHandlerXR.checkSpace(world, core, SPOUT, placed, dir);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		MultiblockHandlerXR.fillSpace(world, core, SPOUT, this, dir);

		Direction rot = dir.getClockWise();
		this.makeExtra(world, core.relative(dir, 2).relative(rot));
		this.makeExtra(world, core.relative(dir, 2).relative(rot.getOpposite()));
		this.makeExtra(world, core.relative(rot, 2).relative(dir));
		this.makeExtra(world, core.relative(rot, 2).relative(dir.getOpposite()));
		this.makeExtra(world, core.relative(rot, -2).relative(dir));
		this.makeExtra(world, core.relative(rot, -2).relative(dir.getOpposite()));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}
}
