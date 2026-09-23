package com.hbm.uninos;

import com.hbm.util.DirPos;

import net.minecraft.core.BlockPos;

public class GenNode<N extends NodeNet<?, ?, ?>> {

	public BlockPos[] positions;
	public DirPos[] connections = new DirPos[0];
	/** Quick reminder that this CAN and WILL be null for the first tick between the node being created
	 * and the nodepsace update loop establishing a network. always check hasValidNet beforehand! */
	public N net;
	public boolean expired = false;
	public boolean recentlyChanged = true;
	/** Used for distinguishing the node type when saving it to UNINOS' node map */
	public INetworkProvider<N> networkProvider;

	public GenNode(INetworkProvider<N> provider, BlockPos... positions) {
		this.networkProvider = provider;
		this.positions = positions;
	}

	public GenNode<N> setConnections(DirPos... connections) {
		this.connections = connections;
		return this;
	}

	public GenNode<N> setStandardConnections(BlockPos pos) {
		return this.setConnections(DirPos.allAround(pos));
	}

	public GenNode<N> addConnection(DirPos connection) {
		DirPos[] newCons = new DirPos[this.connections.length + 1];
		System.arraycopy(this.connections, 0, newCons, 0, this.connections.length);
		newCons[newCons.length - 1] = connection;
		this.connections = newCons;
		return this;
	}

	public boolean hasValidNet() {
		return this.net != null && this.net.isValid();
	}

	public void setNet(N net) {
		this.net = net;
		this.recentlyChanged = true;
	}
}
