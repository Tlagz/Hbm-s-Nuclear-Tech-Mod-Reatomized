package api.hbm.block;

import com.hbm.inventory.material.Mats.MaterialStack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/** Blocks that take molten material: poured into from above (crucibles, arc furnaces) or flowing in from the side (channels) */
public interface ICrucibleAcceptor {

	/*
	 * Pouring: The metal leaves the channel/crucible and usually (but not always) falls down. The additional double coords give a more precise impact location.
	 * Also useful for entities like large crucibles since they are filled from the top.
	 */
	public boolean canAcceptPartialPour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack);
	public MaterialStack pour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack);

	/*
	 * Flowing: The "safe" transfer of metal using a channel or other means, usually from block to block and usually horizontally (but not necessarily).
	 * May also be used for entities like minecarts that could be loaded from the side.
	 */
	public boolean canAcceptPartialFlow(Level world, BlockPos pos, Direction side, MaterialStack stack);
	public MaterialStack flow(Level world, BlockPos pos, Direction side, MaterialStack stack);
}
