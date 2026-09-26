package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/** Electric ground water pump: 1,000 HE for 10,000mB water per tick */
public class TileEntityMachinePumpElectric extends TileEntityMachinePumpBase implements IEnergyReceiverMK2 {

	public long power;
	public static final long maxPower = 10_000;

	public TileEntityMachinePumpElectric(BlockPos pos, BlockState state) {
		super(ModTileEntities.PUMP_ELECTRIC.get(), pos, state);
		water = new FluidTank(Fluids.WATER, electricSpeed * 100);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {
			if(level.getGameTime() % 20 == 0) for(DirPos pos : getConPos()) {
				this.trySubscribe(level, pos, pos.getDir());
			}
		}

		super.updateEntity();
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		power = nbt.getLong("power");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
	}

	@Override
	protected boolean canOperate() {
		return power >= 1_000 && water.getFill() < water.getMaxFill();
	}

	@Override
	protected void operate() {
		this.power -= 1_000;
		water.setFill(Math.min(water.getFill() + electricSpeed, water.getMaxFill()));
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}
}
