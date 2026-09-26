package com.hbm.blocks.machine;

import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityMachineOrbus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Orbus: a 4x4x5 gas tank, the 2x2 core column is connectable at the bottom and the top */
public class MachineOrbus extends BlockBigBarrelTank {

	public MachineOrbus(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineOrbus(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 2, 1, 2, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		Direction d2 = dir.getClockWise();
		Direction back = dir.getOpposite();

		for(int i = 0; i < 5; i += 4) {
			BlockPos p = core.above(i);
			this.makeExtra(world, p);
			this.makeExtra(world, p.relative(back));
			this.makeExtra(world, p.relative(d2));
			this.makeExtra(world, p.relative(back).relative(d2));
		}
	}
}
