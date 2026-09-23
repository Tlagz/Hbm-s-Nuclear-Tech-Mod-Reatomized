package api.hbm.fluidmk2;

import net.minecraft.core.BlockPos;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.tile.EnumTransferAction;
import api.hbm.tile.ILoadedTile.TileAccessCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

public interface IFluidReceiverMK2 extends IFluidUserMK2 {

	/** Sends fluid of the desired type and pressure to the receiver, returns the remainder */
	public long transferFluid(FluidType type, int pressure, long amount);
	public default long getReceiverSpeed(FluidType type, int pressure) { return 1_000_000_000; }
	public long getDemand(FluidType type, int pressure);
	
	public default int[] getReceivingPressureRange(FluidType type) { return DEFAULT_PRESSURE_RANGE; }
	
	public default EnumTransferAction trySubscribe(FluidType type, Level world, DirPos pos) { return trySubscribe(type, world, pos, pos.getDir()); }
	
	public default EnumTransferAction trySubscribe(FluidType type, Level world, BlockPos pos, Direction dir) {

		BlockEntity te = TileAccessCache.getTileOrCache(world, pos);
				EnumTransferAction action = EnumTransferAction.NOTHING;
		
		if(te instanceof IFluidConnectorMK2) {
			IFluidConnectorMK2 con = (IFluidConnectorMK2) te;
			if(!con.canConnect(type, dir.getOpposite())) return action;
			
			GenNode node = UniNodespace.getNode(world, pos, type.getNetworkProvider());
			
			if(node != null && node.net != null) {
				node.net.addReceiver(this);
				action = EnumTransferAction.CONNECT_NET;
			}
		}
		
		
		return action;
	}
	
	public default ConnectionPriority getFluidPriority() {
		return ConnectionPriority.NORMAL;
	}
}
