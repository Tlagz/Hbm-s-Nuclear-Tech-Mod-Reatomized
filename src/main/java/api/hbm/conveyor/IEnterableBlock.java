package api.hbm.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Blocks conveyor items and packages can move into (inserters, boxers...). The direction is the side of the block
 * the object enters from.
 */
public interface IEnterableBlock {

	public boolean canItemEnter(Level world, BlockPos pos, Direction dir, IConveyorItem entity);
	public void onItemEnter(Level world, BlockPos pos, Direction dir, IConveyorItem entity);

	public boolean canPackageEnter(Level world, BlockPos pos, Direction dir, IConveyorPackage entity);
	public void onPackageEnter(Level world, BlockPos pos, Direction dir, IConveyorPackage entity);
}
