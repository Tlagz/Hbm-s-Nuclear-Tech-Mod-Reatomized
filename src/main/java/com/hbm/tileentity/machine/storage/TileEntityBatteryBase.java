package com.hbm.tileentity.machine.storage;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyConductorMK2;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Energy storage blocks that are part of the power network themselves (the battery socket, the FEnSU): the block's
 * ports form one power node, the redstone signal picks between two modes (input, buffer, output, none) and the
 * charge priority decides the order of charging.
 *
 * TODO copy tool, OpenComputers
 */
public abstract class TileEntityBatteryBase extends TileEntityMachineBase implements IEnergyConductorMK2, IEnergyProviderMK2, IEnergyReceiverMK2, IControlReceiver {

	public byte lastRedstone = 0;
	public long prevPowerState = 0;

	public static final int mode_input = 0;
	public static final int mode_buffer = 1;
	public static final int mode_output = 2;
	public static final int mode_none = 3;
	public short redLow = 0;
	public short redHigh = 2;
	public ConnectionPriority priority = ConnectionPriority.LOW;

	protected PowerNode node;

	public TileEntityBatteryBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int slotCount) {
		super(type, pos, state, slotCount);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(priority == null || priority == ConnectionPriority.LOWEST || priority == ConnectionPriority.HIGHEST) {
				priority = ConnectionPriority.LOW;
			}

			if(this.node == null || this.node.expired) {
				this.node = Nodespace.getNode(level, worldPosition);

				if(this.node == null || this.node.expired) {
					this.node = this.createNode();
					Nodespace.createNode(level, this.node);
				}
			}

			if(this.node != null && this.node.hasValidNet()) switch(this.getRelevantMode(false)) {
			case mode_input: this.node.net.removeProvider(this); this.node.net.addReceiver(this); break;
			case mode_output: this.node.net.addProvider(this); this.node.net.removeReceiver(this); break;
			case mode_buffer: this.node.net.addProvider(this); this.node.net.addReceiver(this); break;
			case mode_none: this.node.net.removeProvider(this); this.node.net.removeReceiver(this); break;
			}

			byte comp = this.getComparatorPower();
			if(comp != this.lastRedstone) {
				for(BlockPos port : this.getPortPos()) level.updateNeighbourForOutputSignal(port, level.getBlockState(port).getBlock());
			}
			this.lastRedstone = comp;

			prevPowerState = this.getPower();

			this.networkPackNT(100);
		}
	}

	public byte getComparatorPower() {
		double frac = (double) this.getPower() / (double) Math.max(this.getMaxPower(), 1) * 15D;
		return (byte) (Mth.clamp((int) Math.round(frac), 0, 15));
	}

	@Override
	public PowerNode createNode() {
		return new PowerNode(this.getPortPos()).setConnections(this.getConPos());
	}

	/** Only when the block is broken, nodes outlive chunk unloading */
	@Override
	public void setRemoved() {
		super.setRemoved();

		if(isServer() && this.isLoaded && this.node != null) {
			Nodespace.destroyNode(level, worldPosition);
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return stack.getItem() instanceof IBatteryItem;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeShort(redLow);
		buf.writeShort(redHigh);
		buf.writeByte(priority.ordinal());
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		redLow = buf.readShort();
		redHigh = buf.readShort();
		priority = priorityOf(buf.readByte());
	}

	private static ConnectionPriority priorityOf(int ordinal) {
		ConnectionPriority[] values = ConnectionPriority.values();
		return values[Mth.clamp(ordinal, 0, values.length - 1)];
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.redLow = nbt.getShort("redLow");
		this.redHigh = nbt.getShort("redHigh");
		this.lastRedstone = nbt.getByte("lastRedstone");
		this.priority = priorityOf(nbt.getByte("priority"));
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putShort("redLow", redLow);
		nbt.putShort("redHigh", redHigh);
		nbt.putByte("lastRedstone", lastRedstone);
		nbt.putByte("priority", (byte) this.priority.ordinal());
	}

	@Override public boolean allowDirectProvision() { return false; }
	@Override public ConnectionPriority getPriority() { return this.priority; }

	public abstract BlockPos[] getPortPos();
	public abstract DirPos[] getConPos();

	private short modeCache = 0;

	/** The mode picked by the redstone signal on any of the ports, the cached one is used by the network */
	public short getRelevantMode(boolean useCache) {
		if(useCache) return this.modeCache;
		boolean powered = false;
		for(BlockPos pos : getPortPos()) if(level.hasNeighborSignal(pos)) { powered = true; break; }
		this.modeCache = powered ? this.redHigh : this.redLow;
		return this.modeCache;
	}

	@Override
	public boolean hasPermission(Player player) {
		return this.stillValid(player);
	}

	/** The GUI buttons cycle the modes and the priority (low, normal, high) */
	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("low")) {
			this.redLow++;
			if(this.redLow > 3) this.redLow = 0;
		}
		if(data.contains("high")) {
			this.redHigh++;
			if(this.redHigh > 3) this.redHigh = 0;
		}
		if(data.contains("priority")) {
			int ordinal = this.priority.ordinal() + 1;
			if(ordinal > ConnectionPriority.HIGH.ordinal()) ordinal = ConnectionPriority.LOW.ordinal();
			this.priority = priorityOf(ordinal);
		}
		this.setChanged();
	}
}
