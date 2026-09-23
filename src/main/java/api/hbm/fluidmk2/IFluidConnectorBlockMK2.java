package api.hbm.fluidmk2;

import net.minecraft.core.BlockPos;
import com.hbm.inventory.fluid.FluidType;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.Direction;

public interface IFluidConnectorBlockMK2 {

	/** dir is the face that is connected to, the direction going outwards from the block */
	public boolean canConnect(FluidType type, BlockGetter world, BlockPos pos, Direction dir);
}
