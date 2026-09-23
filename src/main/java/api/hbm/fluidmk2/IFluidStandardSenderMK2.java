package api.hbm.fluidmk2;

import net.minecraft.core.BlockPos;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.DirPos;

import api.hbm.tile.EnumTransferAction;
import api.hbm.tile.ILoadedTile.TileAccessCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

/**
 * IFluidProviderMK2 with standard implementation for fluid provision and fluid removal.
 * @author hbm
 */
public interface IFluidStandardSenderMK2 extends IFluidProviderMK2 {

	public default EnumTransferAction tryProvide(FluidTank tank, Level world, DirPos pos) { return tryProvide(tank.getTankType(), tank.getPressure(), world, pos, pos.getDir()); }
	public default EnumTransferAction tryProvide(FluidType type, Level world, DirPos pos) { return tryProvide(type, 0, world, pos, pos.getDir()); }
	public default EnumTransferAction tryProvide(FluidType type, int pressure, Level world, DirPos pos) { return tryProvide(type, pressure, world, pos, pos.getDir()); }

	public default EnumTransferAction tryProvide(FluidTank tank, Level world, BlockPos pos, Direction dir) { return tryProvide(tank.getTankType(), tank.getPressure(), world, pos, dir); }
	public default EnumTransferAction tryProvide(FluidType type, Level world, BlockPos pos, Direction dir) { return tryProvide(type, 0, world, pos, dir); }

	public default EnumTransferAction tryProvide(FluidType type, int pressure, Level world, BlockPos pos, Direction dir) {

		BlockEntity te = TileAccessCache.getTileOrCache(world, pos);
				
		EnumTransferAction action = null;

		if(te instanceof IFluidConnectorMK2) {
			IFluidConnectorMK2 con = (IFluidConnectorMK2) te;
			if(con.canConnect(type, dir.getOpposite())) {

				GenNode<FluidNetMK2> node = UniNodespace.getNode(world, pos, type.getNetworkProvider());

				if(node != null && node.net != null) {
					node.net.addProvider(this);
					action = EnumTransferAction.CONNECT_NET;
				}
			}
		}

		if(te != this && te instanceof IFluidReceiverMK2) {
			IFluidReceiverMK2 rec = (IFluidReceiverMK2) te;
			if(rec.canConnect(type, dir.getOpposite())) {
				long provides = Math.min(this.getFluidAvailable(type, pressure), this.getProviderSpeed(type, pressure));
				long receives = Math.min(rec.getDemand(type, pressure), rec.getReceiverSpeed(type, pressure));
				long toTransfer = Math.min(provides, receives);
				toTransfer -= rec.transferFluid(type, pressure, toTransfer);
				this.useUpFluid(type, pressure, toTransfer);
				if(action == null) action = EnumTransferAction.PROVIDE_DIRECT;
			}
		}

		
		return action == null ? EnumTransferAction.NOTHING : action;
	}

	public FluidTank[] getSendingTanks();

	@Override
	public default long getFluidAvailable(FluidType type, int pressure) {
		long amount = 0;
		for(FluidTank tank : getSendingTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) amount += tank.getFill();
		}
		return amount;
	}

	@Override
	public default void useUpFluid(FluidType type, int pressure, long amount) {
		int tanks = 0;
		for(FluidTank tank : getSendingTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) tanks++;
		}
		if(tanks > 1) {
			int firstRound = (int) Math.floor((double) amount / (double) tanks);
			for(FluidTank tank : getSendingTanks()) {
				if(tank.getTankType() == type && tank.getPressure() == pressure) {
					int toRem = Math.min(firstRound, tank.getFill());
					tank.setFill(tank.getFill() - toRem);
					amount -= toRem;
				}
			}
		}
		if(amount > 0) for(FluidTank tank : getSendingTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) {
				int toRem = (int) Math.min(amount, tank.getFill());
				tank.setFill(tank.getFill() - toRem);
				amount -= toRem;
			}
		}
	}

	@Override
	public default int[] getProvidingPressureRange(FluidType type) {
		int lowest = HIGHEST_VALID_PRESSURE;
		int highest = 0;

		for(FluidTank tank : getSendingTanks()) {
			if(tank.getTankType() == type) {
				if(tank.getPressure() < lowest) lowest = tank.getPressure();
				if(tank.getPressure() > highest) highest = tank.getPressure();
			}
		}

		return lowest <= highest ? new int[] {lowest, highest} : DEFAULT_PRESSURE_RANGE;
	}

	@Override
	public default long getProviderSpeed(FluidType type, int pressure) {
		return 1_000_000_000;
	}
}
