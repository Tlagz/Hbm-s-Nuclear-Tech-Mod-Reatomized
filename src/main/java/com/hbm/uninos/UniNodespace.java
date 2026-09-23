package com.hbm.uninos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.hbm.util.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Unified Nodespace, a Nodespace for all applications.
 * "Nodespace" is an invisible "dimension" where nodes exist, a node is basically the "soul" of a tile entity with networking capabilities.
 * Instead of tile entities having to find each other which is costly and assumes the tiles are loaded, tiles simply create nodes at their
 * respective position in nodespace, the nodespace itself handles stuff like connections which can also happen in unloaded chunks.
 * A node is so to say the "soul" of a tile entity which can act independent of its "body".
 * @author hbm
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class UniNodespace {

	private record NodeKey(BlockPos pos, INetworkProvider type) { }

	public static Map<Level, UniNodeWorld> worlds = new HashMap<>();
	public static Set<NodeNet> activeNodeNets = new HashSet<>();

	public static GenNode getNode(Level world, BlockPos pos, INetworkProvider type) {
		UniNodeWorld nodeWorld = worlds.get(world);
		if(nodeWorld != null) return nodeWorld.nodes.get(new NodeKey(pos.immutable(), type));
		return null;
	}

	public static void createNode(Level world, GenNode node) {
		worlds.computeIfAbsent(world, w -> new UniNodeWorld()).pushNode(node);
	}

	public static void destroyNode(Level world, BlockPos pos, INetworkProvider type) {
		GenNode node = getNode(world, pos, type);
		if(node != null) {
			worlds.get(world).popNode(node);
		}
	}

	public static void destroyNode(Level world, GenNode node) {
		if(node != null) {
			worlds.get(world).popNode(node);
		}
	}

	private static int reapTimer = 0;
	public static void updateNodespace(MinecraftServer server) {

		for(ServerLevel world : server.getAllLevels()) {
			UniNodeWorld nodeWorld = worlds.get(world);

			if(nodeWorld == null) continue;

			for(Map.Entry<NodeKey, GenNode> entry : nodeWorld.nodes.entrySet()) {
				GenNode node = entry.getValue();
				INetworkProvider provider = entry.getKey().type();
				if(!node.hasValidNet() || node.recentlyChanged) {
					checkNodeConnection(world, node, provider);
					node.recentlyChanged = false;
				}
			}
		}

		updateNetworks();
		updateReapTimer();
	}

	private static void updateNetworks() {

		for(NodeNet net : activeNodeNets) net.resetTrackers(); //reset has to be done before everything else
		for(NodeNet net : activeNodeNets) net.update();

		if(reapTimer <= 0) {
			activeNodeNets.forEach((net) -> net.links.removeIf((link) -> ((GenNode) link).expired));
			activeNodeNets.removeIf((net) -> net.links.size() <= 0); // reap empty networks
		}
	}

	private static void updateReapTimer() {
		if(reapTimer <= 0) reapTimer = 5 * 60 * 20; // 5 minutes is more than plenty
		else reapTimer--;
	}

	/** Goes over each connection point of the given node, tries to find neighbor nodes and to join networks with them */
	private static void checkNodeConnection(Level world, GenNode node, INetworkProvider provider) {

		for(DirPos con : node.connections) {
			GenNode conNode = getNode(world, con, provider); // get whatever neighbor node intersects with that connection
			if(conNode != null) { // if there is a node at that place
				if(conNode.hasValidNet() && conNode.net == node.net) continue; // if the net is valid and both nodes have the same net, skip
				if(checkConnection(conNode, con, false)) {
					connectToNode(node, conNode);
				}
			}
		}

		if(node.net == null || !node.net.isValid()) provider.provideNetwork().joinLink(node);
	}

	/** Checks if the node can be connected to given the DirPos, skipSideCheck will ignore the DirPos' direction value */
	public static boolean checkConnection(GenNode connectsTo, DirPos connectFrom, boolean skipSideCheck) {
		for(DirPos revCon : connectsTo.connections) {
			if(revCon.relative(revCon.getDir().getOpposite()).equals(connectFrom) && (revCon.getDir() == connectFrom.getDir().getOpposite() || skipSideCheck)) {
				return true;
			}
		}
		return false;
	}

	/** Links two nodes with different or potentially no networks */
	private static void connectToNode(GenNode origin, GenNode connection) {

		if(origin.hasValidNet() && connection.hasValidNet()) { // both nodes have nets, but the nets are different (previous assumption), join networks
			if(origin.net.links.size() > connection.net.links.size()) {
				origin.net.joinNetworks(connection.net);
			} else {
				connection.net.joinNetworks(origin.net);
			}
		} else if(!origin.hasValidNet() && connection.hasValidNet()) { // origin has no net, connection does, have origin join connection's net
			connection.net.joinLink(origin);
		} else if(origin.hasValidNet() && !connection.hasValidNet()) { // ...and vice versa
			origin.net.joinLink(connection);
		}
	}

	/** Drops all nodes of a level, used when the level unloads so the level object isn't kept alive */
	public static void unloadWorld(Level world) {
		UniNodeWorld nodeWorld = worlds.remove(world);
		if(nodeWorld != null) nodeWorld.nodes.values().forEach(node -> { if(node.net != null) node.net.destroy(); });
	}

	/** Full reset, used when the server stops (singleplayer can load another world in the same JVM) */
	public static void clear() {
		activeNodeNets.clear();
		worlds.clear();
		reapTimer = 0;
	}

	public static class UniNodeWorld {

		public Map<NodeKey, GenNode> nodes = new LinkedHashMap<>();

		/** Adds a node at all its positions to the nodespace */
		public void pushNode(GenNode node) {
			for(BlockPos pos : node.positions) {
				nodes.put(new NodeKey(pos.immutable(), node.networkProvider), node);
			}
		}

		/** Removes the specified node from all positions from nodespace */
		public void popNode(GenNode node) {
			if(node.net != null) node.net.destroy();
			for(BlockPos pos : node.positions) {
				nodes.remove(new NodeKey(pos.immutable(), node.networkProvider));
			}
			node.expired = true;
		}
	}
}
