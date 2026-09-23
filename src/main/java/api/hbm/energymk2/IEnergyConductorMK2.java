package api.hbm.energymk2;

import com.hbm.util.DirPos;

import api.hbm.energymk2.Nodespace.PowerNode;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IEnergyConductorMK2 extends IEnergyConnectorMK2 {

	public default PowerNode createNode() {
		BlockEntity tile = (BlockEntity) this;
		return new PowerNode(tile.getBlockPos()).setConnections(DirPos.allAround(tile.getBlockPos()));
	}
}
