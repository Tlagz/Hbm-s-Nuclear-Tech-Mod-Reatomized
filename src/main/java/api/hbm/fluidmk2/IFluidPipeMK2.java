package api.hbm.fluidmk2;

import com.hbm.inventory.fluid.FluidType;
import net.minecraft.core.BlockPos;
import com.hbm.util.DirPos;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * IFluidConductorMK2 with added node creation method
 * @author hbm
 */
public interface IFluidPipeMK2 extends IFluidConnectorMK2 {
	
	public default FluidNode createNode(FluidType type) {
		BlockEntity tile = (BlockEntity) this;
		return new FluidNode(type.getNetworkProvider(), tile.getBlockPos()).setConnections(DirPos.allAround(tile.getBlockPos()));
	}
}
