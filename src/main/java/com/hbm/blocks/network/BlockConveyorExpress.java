package com.hbm.blocks.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Three times as fast */
public class BlockConveyorExpress extends BlockConveyorBendable {

	public BlockConveyorExpress(Properties properties) {
		super(properties);
	}

	@Override
	public Vec3 getTravelLocation(Level world, BlockPos pos, Vec3 itemPos, double speed) {
		return super.getTravelLocation(world, pos, itemPos, speed * 3);
	}
}
