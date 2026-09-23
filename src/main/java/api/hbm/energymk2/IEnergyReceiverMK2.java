package api.hbm.energymk2;

import com.hbm.util.DirPos;

import api.hbm.energymk2.Nodespace.PowerNode;
import api.hbm.tile.EnumTransferAction;
import api.hbm.tile.ILoadedTile.TileAccessCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** If it receives energy, use this */
public interface IEnergyReceiverMK2 extends IEnergyHandlerMK2 {

	public default long transferPower(long power) {
		if(power + this.getPower() <= this.getMaxPower()) {
			this.setPower(power + this.getPower());
			return 0;
		}
		long capacity = this.getMaxPower() - this.getPower();
		long overshoot = power - capacity;
		this.setPower(this.getMaxPower());
		return overshoot;
	}

	public default long getReceiverSpeed() {
		return this.getMaxPower();
	}

	/** Whether a provider can provide power by touching the block (i.e. via proxies), bypassing the need for a network entirely */
	public default boolean allowDirectProvision() { return true; }

	public default EnumTransferAction trySubscribe(Level world, DirPos pos) { return trySubscribe(world, pos, pos.getDir()); }

	/** pos is the port position, dir points from this machine towards that position */
	public default EnumTransferAction trySubscribe(Level world, BlockPos pos, Direction dir) {

		BlockEntity te = TileAccessCache.getTileOrCache(world, pos);

		if(te instanceof IEnergyConductorMK2 con) {
			if(!con.canConnect(dir.getOpposite())) return EnumTransferAction.NOTHING;

			PowerNode node = Nodespace.getNode(world, pos);

			if(node != null && node.net != null) {
				node.net.addReceiver(this);
				return EnumTransferAction.CONNECT_NET;
			}
		}

		return EnumTransferAction.NOTHING;
	}

	public default void tryUnsubscribe(Level world, BlockPos pos) {

		PowerNode node = Nodespace.getNode(world, pos);

		if(node != null && node.net != null) {
			node.net.removeReceiver(this);
		}
	}

	public enum ConnectionPriority {
		LOWEST,
		LOW,
		NORMAL,
		HIGH,
		HIGHEST
	}

	public default ConnectionPriority getPriority() {
		return ConnectionPriority.NORMAL;
	}
}
