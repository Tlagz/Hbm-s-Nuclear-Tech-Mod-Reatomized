package com.hbm.tileentity.machine.oil;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModDataComponents;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Common parts of the powered oil processors (vacuum distiller, catalytic reformer, hydrotreater): power from slot 0,
 * one input tank and several output tanks, ports on the sides but not below, contents kept when broken.
 *
 * TODO copy tool
 */
public abstract class TileEntityOilProcessorBase extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, MenuProvider {

	public long power;
	public static final long maxPower = 1_000_000;
	public FluidTank[] tanks;

	public TileEntityOilProcessorBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int slotCount) {
		super(type, pos, state, slotCount);
	}

	public abstract DirPos[] getConPos();

	/** The names the original saved the tanks under */
	protected abstract String[] getTankKeys();

	protected void chargeAndConnect(boolean connect) {
		if(connect) {
			for(DirPos pos : getConPos()) {
				this.trySubscribe(level, pos, pos.getDir());
				for(FluidTank tank : getReceivingTanks()) this.trySubscribe(tank.getTankType(), level, pos, pos.getDir());
			}
		}
		power = Library.chargeTEFromItems(slots, 0, power, maxPower);
	}

	protected void sendOutputs() {
		for(DirPos pos : getConPos()) {
			for(FluidTank tank : getSendingTanks()) {
				if(tank.getFill() > 0) this.tryProvide(tank, level, pos, pos.getDir());
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		for(FluidTank tank : tanks) tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		for(FluidTank tank : tanks) tank.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		power = nbt.getLong("power");
		String[] keys = getTankKeys();
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, keys[i]);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		String[] keys = getTankKeys();
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, keys[i]);
	}

	/// IPersistentNBT: the tanks stay with the dropped machine ///

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		boolean empty = true;
		for(FluidTank tank : tanks) if(tank.getFill() > 0) empty = false;
		if(empty) return;
		CompoundTag data = new CompoundTag();
		for(int i = 0; i < tanks.length; i++) this.tanks[i].writeToNBT(data, "" + i);
		components.set(ModDataComponents.PERSISTENT.get(), CustomData.of(data));
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		CustomData data = input.get(ModDataComponents.PERSISTENT.get());
		if(data == null) return;
		CompoundTag nbt = data.copyTag();
		for(int i = 0; i < tanks.length; i++) this.tanks[i].readFromNBT(nbt, "" + i);
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public boolean canConnect(Direction dir) {
		return dir != Direction.DOWN;
	}

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		return dir != Direction.DOWN;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}
}
