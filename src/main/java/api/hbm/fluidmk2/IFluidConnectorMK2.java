package api.hbm.fluidmk2;

import com.hbm.inventory.fluid.FluidType;

import net.minecraft.core.Direction;

public interface IFluidConnectorMK2 {
	
	/**
	 * Whether the given side can be connected to
	 * @param dir
	 * @return
	 */
	public default boolean canConnect(FluidType type, Direction dir) {
		return dir != null;
	}
}
