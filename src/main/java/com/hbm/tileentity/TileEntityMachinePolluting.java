package com.hbm.tileentity;

import java.util.HashMap;
import java.util.Map;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FluidTrait;

import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Machines that burn things: pollution first goes into small smoke buffers that exhaust pipes can take away,
 * whatever overflows goes into the world's pollution.
 */
public abstract class TileEntityMachinePolluting extends TileEntityMachineBase implements IFluidStandardSenderMK2 {

	public FluidTank smoke;
	public FluidTank smoke_leaded;
	public FluidTank smoke_poison;

	public TileEntityMachinePolluting(BlockEntityType<?> type, BlockPos pos, BlockState state, int scount, int buffer) {
		super(type, pos, state, scount);
		smoke = new FluidTank(Fluids.SMOKE, buffer);
		smoke_leaded = new FluidTank(Fluids.SMOKE_LEADED, buffer);
		smoke_poison = new FluidTank(Fluids.SMOKE_POISON, buffer);
	}

	public void pollute(PollutionType type, float amount) {
		FluidTank tank = type == PollutionType.SOOT ? smoke : type == PollutionType.HEAVYMETAL ? smoke_leaded : smoke_poison;

		int fluidAmount = (int) Math.ceil(amount * 100);
		tank.setFill(tank.getFill() + fluidAmount);

		if(tank.getFill() > tank.getMaxFill()) {
			int overflow = tank.getFill() - tank.getMaxFill();
			tank.setFill(tank.getMaxFill());
			PollutionHandler.incrementPollution(level, worldPosition, type, overflow / 100F);

			if(level.random.nextInt(3) == 0) level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.1F, 1.5F);
		}
	}

	public void pollute(FluidType type, FluidTrait.FluidReleaseType release, float amount) {

		FT_Polluting trait = type.getTrait(FT_Polluting.class);
		if(trait == null) return;
		if(release == FluidTrait.FluidReleaseType.VOID) return;

		HashMap<PollutionType, Float> map = release == FluidTrait.FluidReleaseType.BURN ? trait.burnMap : trait.releaseMap;

		for(Map.Entry<PollutionType, Float> entry : map.entrySet()) {
			pollute(entry.getKey(), entry.getValue());
		}
	}

	public void sendSmoke(BlockPos pos, Direction dir) {
		if(this.smoke.getFill() > 0) this.tryProvide(smoke, level, pos, dir);
		if(this.smoke_leaded.getFill() > 0) this.tryProvide(smoke_leaded, level, pos, dir);
		if(this.smoke_poison.getFill() > 0) this.tryProvide(smoke_poison, level, pos, dir);
	}

	public FluidTank[] getSmokeTanks() {
		return new FluidTank[] {smoke, smoke_leaded, smoke_poison};
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		smoke.readFromNBT(nbt, "smoke0");
		smoke_leaded.readFromNBT(nbt, "smoke1");
		smoke_poison.readFromNBT(nbt, "smoke2");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		smoke.writeToNBT(nbt, "smoke0");
		smoke_leaded.writeToNBT(nbt, "smoke1");
		smoke_poison.writeToNBT(nbt, "smoke2");
	}
}
