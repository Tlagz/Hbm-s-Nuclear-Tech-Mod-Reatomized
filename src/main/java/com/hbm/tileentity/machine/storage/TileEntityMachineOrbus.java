package com.hbm.tileentity.machine.storage;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Orbus, a 512,000mB gas tank; ports under and over its 2x2 base corners, holds anything without complaint */
public class TileEntityMachineOrbus extends TileEntityBarrel {

	public TileEntityMachineOrbus(BlockPos pos, BlockState state) {
		super(ModTileEntities.ORBUS.get(), pos, state, 512000);
	}

	@Override
	public String getName() {
		return "container.orbus";
	}

	@Override public long getReceiverSpeed(FluidType type, int pressure) { return Math.max(1_000, (tank.getMaxFill() - tank.getFill()) / 100); }
	@Override public long getProviderSpeed(FluidType type, int pressure) { return Math.max(1_000, tank.getFill() / 100); }

	@Override
	public void checkFluidInteraction() { } //NO!

	protected DirPos[] conPos;

	@Override
	protected DirPos[] getConPos() {

		if(conPos != null)
			return conPos;

		conPos = new DirPos[8];

		Direction dir = BlockDummyable.getRotation(getBlockState()).getOpposite();
		Direction rot = dir.getCounterClockWise(); // getRotation(DOWN)

		for(int i = -1; i < 6; i += 6) {
			Direction out = i == -1 ? Direction.DOWN : Direction.UP;
			int index = i == -1 ? 0 : 4;
			BlockPos p = worldPosition.above(i);
			conPos[index + 0] = new DirPos(p, out);
			conPos[index + 1] = new DirPos(p.relative(dir), out);
			conPos[index + 2] = new DirPos(p.relative(rot), out);
			conPos[index + 3] = new DirPos(p.relative(dir).relative(rot), out);
		}

		return conPos;
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 5, worldPosition.getZ() + 3);
	}
}
