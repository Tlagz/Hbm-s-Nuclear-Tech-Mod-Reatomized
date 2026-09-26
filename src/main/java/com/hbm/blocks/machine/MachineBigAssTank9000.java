package com.hbm.blocks.machine;

import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityMachineBAT9000;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Big-Ass Tank 9000: a 5x5 cross, 5 tall; the eight side pillars take fluids */
public class MachineBigAssTank9000 extends BlockBigBarrelTank {

	private static final int[] ARM_EAST = new int[] {4, 0, 1, 1, 2, -2};
	private static final int[] ARM_WEST = new int[] {4, 0, 1, 1, -2, 2};

	public MachineBigAssTank9000(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineBAT9000(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 2, 2, 1, 1};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		MultiblockHandlerXR.fillSpace(world, core, ARM_EAST, this, dir);
		MultiblockHandlerXR.fillSpace(world, core, ARM_WEST, this, dir);

		this.makeExtra(world, core.offset(1, 0, 2));
		this.makeExtra(world, core.offset(-1, 0, 2));
		this.makeExtra(world, core.offset(1, 0, -2));
		this.makeExtra(world, core.offset(-1, 0, -2));
		this.makeExtra(world, core.offset(2, 0, 1));
		this.makeExtra(world, core.offset(-2, 0, 1));
		this.makeExtra(world, core.offset(2, 0, -1));
		this.makeExtra(world, core.offset(-2, 0, -1));
	}

	@Override
	protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
		if(!MultiblockHandlerXR.checkSpace(world, core, getDimensions(), placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, core, ARM_EAST, placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, core, ARM_WEST, placed, dir)) return false;
		return true;
	}
}
