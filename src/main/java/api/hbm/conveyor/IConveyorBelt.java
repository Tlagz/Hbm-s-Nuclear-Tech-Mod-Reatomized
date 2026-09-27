package api.hbm.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Blocks that move conveyor items (the original's api.hbm.conveyor.IConveyorBelt) */
public interface IConveyorBelt {

	public boolean canItemStay(Level world, BlockPos pos, Vec3 itemPos);
	public Vec3 getTravelLocation(Level world, BlockPos pos, Vec3 itemPos, double speed);
	public Vec3 getClosestSnappingPosition(Level world, BlockPos pos, Vec3 itemPos);
}
