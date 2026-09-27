package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Deuterium extraction tower: the big extractor, 2x2 and 10 tall, 50x the tanks and 10x the power */
public class TileEntityDeuteriumTower extends TileEntityDeuteriumExtractor {

	public TileEntityDeuteriumTower(BlockPos pos, BlockState state) {
		super(ModTileEntities.DEUTERIUM_TOWER.get(), pos, state);
		tanks[0] = new FluidTank(Fluids.WATER, 50000);
		tanks[1] = new FluidTank(Fluids.HEAVYWATER, 5000);
	}

	/** Two ports on each side of the 2x2 base */
	@Override
	protected DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getCounterClockWise(); // ForgeDirection.getRotation(DOWN)
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.relative(dir, -2), dir.getOpposite()),
				new DirPos(p.relative(dir, -2).relative(rot), dir.getOpposite()),
				new DirPos(p.relative(dir), dir),
				new DirPos(p.relative(dir).relative(rot), dir),
				new DirPos(p.relative(rot, -1), rot.getOpposite()),
				new DirPos(p.relative(dir, -1).relative(rot, -1), rot.getOpposite()),
				new DirPos(p.relative(rot, 2), rot),
				new DirPos(p.relative(dir, -1).relative(rot, 2), rot),
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 10, worldPosition.getZ() + 2);
	}

	@Override
	public long getMaxPower() {
		return 100_000;
	}
}
