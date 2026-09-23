package com.hbm.tileentity.network;

import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.energymk2.IEnergyConductorMK2;
import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCableBaseNT extends TileEntityLoadedBase implements IEnergyConductorMK2 {

	protected PowerNode node;

	public TileEntityCableBaseNT(BlockPos pos, BlockState state) {
		this(ModTileEntities.CABLE.get(), pos, state);
	}

	public TileEntityCableBaseNT(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(this.node == null || this.node.expired) {

				if(this.shouldCreateNode()) {
					this.node = Nodespace.getNode(level, worldPosition);

					if(this.node == null || this.node.expired) {
						this.node = this.createNode();
						Nodespace.createNode(level, this.node);
					}
				}
			}
		}
	}

	public boolean shouldCreateNode() {
		return true;
	}

	/** Called when the block is broken, NOT when the chunk unloads (nodes outlive unloading on purpose) */
	@Override
	public void setRemoved() {
		super.setRemoved();

		if(isServer() && this.isLoaded) {
			if(this.node != null) {
				Nodespace.destroyNode(level, worldPosition);
			}
		}
	}

	@Override
	public boolean canConnect(Direction dir) {
		return dir != null;
	}
}
