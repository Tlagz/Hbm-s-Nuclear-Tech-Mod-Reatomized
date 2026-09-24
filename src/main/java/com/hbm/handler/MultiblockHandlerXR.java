package com.hbm.handler;

import com.hbm.blocks.BlockDummyable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Space checks and filling for BlockDummyable multiblocks. Dimensions are
 * { up, down, forward, backward, left, right } as seen from a machine facing south.
 */
public class MultiblockHandlerXR {

	//when looking north
	//											U  D  N  S  W  E
	public static int[] uni = 		new int[] { 3, 0, 4, 4, 4, 4 };

	public static boolean checkSpace(Level world, BlockPos core, int[] dim, BlockPos placed, Direction dir) {

		if(dim == null || dim.length != 6)
			return false;

		int count = 0;

		int[] rot = rotate(dim, dir);
		int x = core.getX(), y = core.getY(), z = core.getZ();

		for(int a = x - rot[4]; a <= x + rot[5]; a++) {
			for(int b = y - rot[1]; b <= y + rot[0]; b++) {
				for(int c = z - rot[2]; c <= z + rot[3]; c++) {

					//if the position matches the just placed block, the space counts as unoccupied
					if(a == placed.getX() && b == placed.getY() && c == placed.getZ())
						continue;

					BlockPos pos = new BlockPos(a, b, c);
					if(world.isOutsideBuildHeight(pos) || !world.getBlockState(pos).canBeReplaced()) {
						return false;
					}

					count++;

					if(count > 2000) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public static void fillSpace(Level world, BlockPos core, int[] dim, Block block, Direction dir) {

		if(dim == null || dim.length != 6)
			return;

		int count = 0;

		int[] rot = rotate(dim, dir);
		int x = core.getX(), y = core.getY(), z = core.getZ();

		BlockDummyable.safeRem = true;

		for(int a = x - rot[4]; a <= x + rot[5]; a++) {
			for(int b = y - rot[1]; b <= y + rot[0]; b++) {
				for(int c = z - rot[2]; c <= z + rot[3]; c++) {

					Direction meta;

					if(b < y) {
						meta = Direction.DOWN;
					} else if(b > y) {
						meta = Direction.UP;
					} else if(a < x) {
						meta = Direction.WEST;
					} else if(a > x) {
						meta = Direction.EAST;
					} else if(c < z) {
						meta = Direction.NORTH;
					} else if(c > z) {
						meta = Direction.SOUTH;
					} else {
						continue;
					}

					world.setBlock(new BlockPos(a, b, c), block.defaultBlockState().setValue(BlockDummyable.META, meta.get3DDataValue()), Block.UPDATE_ALL);

					count++;

					if(count > 2000) {
						BlockDummyable.safeRem = false;
						return;
					}
				}
			}
		}

		BlockDummyable.safeRem = false;
	}

	public static int[] rotate(int[] dim, Direction dir) {

		if(dim == null) return null;
		if(dir == Direction.SOUTH) return dim;

		if(dir == Direction.NORTH) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[3], dim[2], dim[5], dim[4] };
		}

		if(dir == Direction.EAST) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[5], dim[4], dim[2], dim[3] };
		}

		if(dir == Direction.WEST) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[4], dim[5], dim[3], dim[2] };
		}

		return dim;
	}
}
