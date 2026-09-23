package api.hbm.energymk2;

import com.hbm.util.DirPos;

import api.hbm.energymk2.Nodespace.PowerNode;
import api.hbm.tile.EnumTransferAction;
import api.hbm.tile.ILoadedTile.TileAccessCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** If it sends energy, use this */
public interface IEnergyProviderMK2 extends IEnergyHandlerMK2 {

	/** Uses up available power, default implementation has no sanity checking, make sure that the requested power is lequal to the current power */
	public default void usePower(long power) {
		this.setPower(this.getPower() - power);
	}

	public default long getProviderSpeed() {
		return this.getMaxPower();
	}

	public default EnumTransferAction tryProvide(Level world, DirPos pos) { return tryProvide(world, pos, pos.getDir()); }

	/** pos is the port position, dir points from this machine towards that position */
	public default EnumTransferAction tryProvide(Level world, BlockPos pos, Direction dir) {

		BlockEntity te = TileAccessCache.getTileOrCache(world, pos);
		EnumTransferAction action = null;

		if(te instanceof IEnergyConductorMK2 con) {
			if(con.canConnect(dir.getOpposite())) {

				PowerNode node = Nodespace.getNode(world, pos);

				if(node != null && node.net != null) {
					node.net.addProvider(this);
					action = EnumTransferAction.CONNECT_NET;
				}
			}
		}

		if(te instanceof IEnergyReceiverMK2 rec && te != this) {
			if(rec.canConnect(dir.getOpposite()) && rec.allowDirectProvision()) {
				long provides = Math.min(this.getPower(), this.getProviderSpeed());
				long receives = Math.min(rec.getMaxPower() - rec.getPower(), rec.getReceiverSpeed());
				long toTransfer = Math.min(provides, receives);
				toTransfer -= rec.transferPower(toTransfer);
				this.usePower(toTransfer);
				if(action == null) return EnumTransferAction.PROVIDE_DIRECT;
			}
		}

		return action == null ? EnumTransferAction.NOTHING : action;
	}
}
