package com.hbm.tileentity.network;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;

import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidPipeMK2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fluid pipe: joins the fluid network (UNINOS) of its fluid type. The type is synced to the client for the
 * tinted overlay, a change re-renders the block (the original's lastType check + markBlockForUpdate).
 *
 * TODO copy/paste settings (IFluidCopiable)
 */
public class TileEntityPipeBaseNT extends TileEntityLoadedBase implements IFluidPipeMK2 {

	protected FluidNode node;
	protected FluidType type = Fluids.NONE;

	public TileEntityPipeBaseNT(BlockPos pos, BlockState state) {
		this(ModTileEntities.PIPE.get(), pos, state);
	}

	public TileEntityPipeBaseNT(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(this.node == null || this.node.expired) {

				if(this.shouldCreateNode()) {
					this.node = (FluidNode) UniNodespace.getNode(level, worldPosition, type.getNetworkProvider());

					if(this.node == null || this.node.expired) {
						this.node = this.createNode(type);
						UniNodespace.createNode(level, this.node);
					}
				}
			}
		}
	}

	public boolean shouldCreateNode() {
		return true;
	}

	public FluidType getFluidType() {
		return this.type;
	}

	public void setType(FluidType type) {
		FluidType prev = this.type;
		this.type = type;
		this.setChanged();

		if(level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
			UniNodespace.destroyNode(level, worldPosition, prev.getNetworkProvider());
		}

		this.node = null;
	}

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		return dir != null && type == this.type;
	}

	/** Called when the block is broken, NOT when the chunk unloads (nodes outlive unloading on purpose) */
	@Override
	public void setRemoved() {
		super.setRemoved();

		if(isServer() && this.isLoaded && this.node != null) {
			UniNodespace.destroyNode(level, worldPosition, type.getNetworkProvider());
		}
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.type = Fluids.fromID(nbt.getInt("type"));
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("type", this.type.getID());
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("type", this.type.getID());
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		FluidType prev = this.type;
		super.onDataPacket(net, pkt, registries);

		// the overlay color comes from the tile, re-mesh when it changes
		if(prev != this.type && level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
		}
	}
}
