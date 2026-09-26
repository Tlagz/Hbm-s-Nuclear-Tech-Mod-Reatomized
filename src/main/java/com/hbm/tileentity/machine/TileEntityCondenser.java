package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Steam condenser: turns spent steam back into water, 1:1, takes and gives on every side.
 * TODO config (IConfigurableMachine), copy tool
 */
public class TileEntityCondenser extends TileEntityLoadedBase implements IFluidStandardTransceiverMK2 {

	public int age = 0;
	public FluidTank[] tanks;

	public int waterTimer = 0;
	protected int throughput;

	//Configurable values
	public static int inputTankSize = 100;
	public static int outputTankSize = 100;

	public TileEntityCondenser(BlockPos pos, BlockState state) {
		this(ModTileEntities.CONDENSER.get(), pos, state);
	}

	protected TileEntityCondenser(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.SPENTSTEAM, inputTankSize);
		tanks[1] = new FluidTank(Fluids.WATER, outputTankSize);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			age++;
			if(age >= 2) {
				age = 0;
			}

			if(this.waterTimer > 0)
				this.waterTimer--;

			int convert = Math.min(tanks[0].getFill(), tanks[1].getMaxFill() - tanks[1].getFill());
			this.throughput = convert;

			if(extraCondition(convert)) {
				tanks[0].setFill(tanks[0].getFill() - convert);

				if(convert > 0)
					this.waterTimer = 20;

				tanks[1].setFill(tanks[1].getFill() + convert);

				postConvert(convert);
			}

			this.subscribeToAllAround();
			this.sendFluidToAll();

			networkPackNT(150);
		}
	}

	public boolean extraCondition(int convert) { return true; }
	public void postConvert(int convert) { }

	/** Spent steam comes in from all six sides, the bigger condensers have their own ports */
	protected void subscribeToAllAround() {
		for(Direction dir : Direction.values()) this.trySubscribe(tanks[0].getTankType(), level, worldPosition.relative(dir), dir);
	}

	protected void sendFluidToAll() {
		for(Direction dir : Direction.values()) this.tryProvide(tanks[1], level, worldPosition.relative(dir), dir);
	}

	@Override
	public void serialize(ByteBuf buf) {
		this.tanks[0].serialize(buf);
		this.tanks[1].serialize(buf);
		buf.writeByte(this.waterTimer);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.tanks[0].deserialize(buf);
		this.tanks[1].deserialize(buf);
		this.waterTimer = buf.readByte();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "steam");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "steam");
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[1]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0]};
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}
}
