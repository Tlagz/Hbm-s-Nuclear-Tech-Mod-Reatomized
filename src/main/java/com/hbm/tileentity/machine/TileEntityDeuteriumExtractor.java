package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Deuterium extractor: 50mB of water become 1mB of heavy water, burning a 20th of the buffer per operation.
 * No inventory and no GUI, the look overlay shows the tanks.
 */
public class TileEntityDeuteriumExtractor extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {

	public long power = 0;
	public FluidTank[] tanks;

	public TileEntityDeuteriumExtractor(BlockPos pos, BlockState state) {
		this(ModTileEntities.DEUTERIUM_EXTRACTOR.get(), pos, state);
		tanks[0] = new FluidTank(Fluids.WATER, 1000);
		tanks[1] = new FluidTank(Fluids.HEAVYWATER, 100);
	}

	protected TileEntityDeuteriumExtractor(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state, 0);
		tanks = new FluidTank[2];
	}

	@Override
	public String getName() {
		return "container.deuterium";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			DirPos[] con = getConPos();
			for(DirPos pos : con) this.trySubscribe(level, pos);

			if(hasPower() && hasEnoughWater() && tanks[1].getMaxFill() > tanks[1].getFill()) {
				int convert = Math.min(tanks[1].getMaxFill(), tanks[0].getFill()) / 50;
				convert = Math.min(convert, tanks[1].getMaxFill() - tanks[1].getFill());

				tanks[0].setFill(tanks[0].getFill() - convert * 50); //dividing first, then multiplying, will remove any rounding issues
				tanks[1].setFill(tanks[1].getFill() + convert);

				power -= this.getMaxPower() / 20;
			}

			for(DirPos pos : con) {
				this.trySubscribe(tanks[0].getTankType(), level, pos);
				if(tanks[1].getFill() > 0) this.tryProvide(tanks[1], level, pos);
			}

			this.networkPackNT(50);
		}
	}

	/** All six neighbors */
	protected DirPos[] getConPos() {
		return DirPos.allAround(worldPosition);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	public boolean hasPower() {
		return power >= this.getMaxPower() / 20;
	}

	public boolean hasEnoughWater() {
		return tanks[0].getFill() >= 100;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "heavyWater");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "heavyWater");
	}

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return 10_000; }

	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tanks[1] }; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tanks[0] }; }
	@Override public FluidTank[] getAllTanks() { return tanks; }
}
