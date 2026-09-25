package com.hbm.tileentity.machine;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.tileentity.TileEntityLoadedBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Foundry blocks holding molten material: one material type and an amount in quanta. The contents are sent to the
 * client with the block entity data packet whenever they change (the original's markBlockForUpdate), the blocks
 * (ICrucibleAcceptor) hand pouring and flowing to these methods.
 */
public abstract class TileEntityFoundryBase extends TileEntityLoadedBase {

	public NTMMaterial type;
	protected NTMMaterial lastType;
	public int amount;
	protected int lastAmount;

	public TileEntityFoundryBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {
			if(this.lastType != this.type || this.lastAmount != this.amount) {
				this.lastType = this.type;
				this.lastAmount = this.amount;
				this.sync();
			}
		}
	}

	/** Saves and sends the contents to the clients */
	public void sync() {
		this.setChanged();
		if(level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveWithoutMetadata(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.type = Mats.matById.get(nbt.getInt("type"));
		this.amount = nbt.getInt("amount");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("type", this.type == null ? -1 : this.type.id);
		nbt.putInt("amount", this.amount);
	}

	public abstract int getCapacity();

	/**
	 * Standard check for testing if this material stack can be added to the casting block. Checks:<br>
	 * - type matching<br>
	 * - amount being at max<br>
	 */
	public boolean standardCheck(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		if(this.type != null && this.type != stack.material && this.amount > 0) return false; //reject if there's already a different material
		if(this.amount >= this.getCapacity()) return false; //reject if the buffer is already full
		return true;
	}

	/**
	 * Standardized adding of material via pouring or flowing. Does:<br>
	 * - sets material to match the input
	 * - adds the amount, not exceeding the maximum
	 * - returns the amount that cannot be added
	 */
	public MaterialStack standardAdd(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		this.type = stack.material;

		if(stack.amount + this.amount <= this.getCapacity()) {
			this.amount += stack.amount;
			return null;
		}

		int required = this.getCapacity() - this.amount;
		this.amount = this.getCapacity();

		stack.amount -= required;

		return stack;
	}

	/** Standard check with no additional limitations added */
	public boolean canAcceptPartialFlow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		return this.standardCheck(world, pos, side, stack);
	}

	/** Standard flow, no special handling required */
	public MaterialStack flow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		return standardAdd(world, pos, side, stack);
	}

	/** Standard check, but with the additional limitation that the only valid source direction is UP */
	public boolean canAcceptPartialPour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		if(side != Direction.UP) return false;
		return this.standardCheck(world, pos, side, stack);
	}

	/** Standard pour, no special handling required */
	public MaterialStack pour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		return standardAdd(world, pos, side, stack);
	}
}
