package com.hbm.blocks.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Two lanes, items keep to the half they were put on */
public class BlockConveyorDouble extends BlockConveyorBendable {

	public BlockConveyorDouble(Properties properties) {
		super(properties);
	}

	@Override
	public Vec3 getClosestSnappingPosition(Level world, BlockPos pos, Vec3 itemPos) {

		Direction dir = this.getTravelDirection(world, pos, itemPos);

		double ix = Mth.clamp(itemPos.x, pos.getX(), pos.getX() + 1);
		double iz = Mth.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1);

		double posX = pos.getX() + 0.5;
		double posZ = pos.getZ() + 0.5;

		if(dir.getStepX() != 0) {
			posX = ix;
			posZ += iz > posZ ? 0.25 : -0.25;
		}
		if(dir.getStepZ() != 0) {
			posZ = iz;
			posX += ix > posX ? 0.25 : -0.25;
		}

		return new Vec3(posX, pos.getY() + 0.25, posZ);
	}
}
