package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/** Steam powered ground water pump: 100mB steam for 1,000mB water per tick */
public class TileEntityMachinePumpSteam extends TileEntityMachinePumpBase {

	public FluidTank steam;
	public FluidTank lps;

	public TileEntityMachinePumpSteam(BlockPos pos, BlockState state) {
		super(ModTileEntities.PUMP_STEAM.get(), pos, state);
		water = new FluidTank(Fluids.WATER, steamSpeed * 100);
		steam = new FluidTank(Fluids.STEAM, 1_000);
		lps = new FluidTank(Fluids.SPENTSTEAM, 10);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {
			for(DirPos pos : getConPos()) {
				this.trySubscribe(steam.getTankType(), level, pos, pos.getDir());
				if(lps.getFill() > 0) {
					this.tryProvide(lps, level, pos, pos.getDir());
				}
			}
		}

		super.updateEntity();
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {water, steam, lps};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {water, lps};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {steam};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		steam.serialize(buf);
		lps.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		steam.deserialize(buf);
		lps.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		steam.readFromNBT(nbt, "steam");
		lps.readFromNBT(nbt, "lps");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		steam.writeToNBT(nbt, "steam");
		lps.writeToNBT(nbt, "lps");
	}

	@Override
	protected boolean canOperate() {
		return steam.getFill() >= 100 && lps.getMaxFill() - lps.getFill() > 0 && water.getFill() < water.getMaxFill();
	}

	@Override
	protected void operate() {
		steam.setFill(steam.getFill() - 100);
		lps.setFill(lps.getFill() + 1);
		water.setFill(Math.min(water.getFill() + steamSpeed, water.getMaxFill()));
	}
}
