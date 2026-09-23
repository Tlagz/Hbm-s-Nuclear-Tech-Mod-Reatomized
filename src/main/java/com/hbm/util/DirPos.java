package com.hbm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

/**
 * A block position with a direction attached, used for machine ports.
 * The direction points from the machine towards the port position.
 */
public class DirPos extends BlockPos {

	protected final Direction dir;

	public DirPos(int x, int y, int z, Direction dir) {
		super(x, y, z);
		this.dir = dir;
	}

	public DirPos(BlockPos pos, Direction dir) {
		this(pos.getX(), pos.getY(), pos.getZ(), dir);
	}

	/** The position next to origin in the given direction, pointing that way */
	public static DirPos offset(BlockPos origin, Direction dir) {
		return new DirPos(origin.relative(dir), dir);
	}

	@Override
	public DirPos rotate(Rotation rotation) {
		return switch(rotation) {
			case NONE -> this;
			case CLOCKWISE_90 -> new DirPos(-this.getZ(), this.getY(), this.getX(), rotation.rotate(dir));
			case CLOCKWISE_180 -> new DirPos(-this.getX(), this.getY(), -this.getZ(), rotation.rotate(dir));
			case COUNTERCLOCKWISE_90 -> new DirPos(this.getZ(), this.getY(), -this.getX(), rotation.rotate(dir));
		};
	}

	public Direction getDir() {
		return this.dir;
	}

	/** The six neighbor positions of origin, each pointing away from it */
	public static DirPos[] allAround(BlockPos origin) {
		DirPos[] positions = new DirPos[6];
		for(Direction dir : Direction.values()) positions[dir.ordinal()] = offset(origin, dir);
		return positions;
	}
}
