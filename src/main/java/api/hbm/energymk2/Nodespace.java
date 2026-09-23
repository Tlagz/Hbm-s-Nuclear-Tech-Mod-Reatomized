package api.hbm.energymk2;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PowerNetProvider;
import com.hbm.util.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The dead fucking corpse of nodespace MK1.
 * A fantastic proof of concept, but ultimately it was killed for being just not that versatile.
 * This class is mostly just a compatibility husk that should allow uninodespace to slide into the mod with as much lubrication as it deserves.
 *
 * @author hbm
 */
public class Nodespace {

	public static final PowerNetProvider THE_POWER_PROVIDER = new PowerNetProvider();

	public static PowerNode getNode(Level world, BlockPos pos) {
		return (PowerNode) UniNodespace.getNode(world, pos, THE_POWER_PROVIDER);
	}

	public static void createNode(Level world, PowerNode node) {
		UniNodespace.createNode(world, node);
	}

	public static void destroyNode(Level world, BlockPos pos) {
		UniNodespace.destroyNode(world, pos, THE_POWER_PROVIDER);
	}

	/**
	 * Nodes that recently changed networks re-check their neighbors once more, see the original's comment:
	 * without that, joining operations would randomly fail in some places.
	 */
	public static class PowerNode extends GenNode<PowerNetMK2> {

		public PowerNode(BlockPos... positions) {
			super(THE_POWER_PROVIDER, positions);
		}

		@Override
		public PowerNode setConnections(DirPos... connections) {
			super.setConnections(connections);
			return this;
		}
	}
}
