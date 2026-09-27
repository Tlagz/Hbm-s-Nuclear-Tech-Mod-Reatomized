package com.hbm.tileentity.machine;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.fluidmk2.IFluidReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Smokestacks: take any amount of smoke from the exhaust network, release only a fraction of its pollution and
 * collect fly ash (and soot for the industrial one) in an ashpit below.
 *
 * TODO rampant mode smoke stack override once MobConfig has it
 */
public abstract class TileEntityChimneyBase extends TileEntityLoadedBase implements IFluidReceiverMK2 {

	public long ashTick = 0;
	public long sootTick = 0;
	public int onTicks;

	public TileEntityChimneyBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	private static boolean isSmoke(FluidType type) {
		return type == Fluids.SMOKE || type == Fluids.SMOKE_LEADED || type == Fluids.SMOKE_POISON;
	}

	@Override
	public void updateEntity() {

		if(!level.isClientSide) {

			if(level.getGameTime() % 20 == 0) {
				FluidType[] types = new FluidType[] {Fluids.SMOKE, Fluids.SMOKE_LEADED, Fluids.SMOKE_POISON};

				for(FluidType type : types) {
					for(Direction dir : Direction.Plane.HORIZONTAL) this.trySubscribe(type, level, worldPosition.relative(dir, 2), dir);
				}
			}

			if(ashTick > 0 || sootTick > 0) {

				if(level.getBlockEntity(worldPosition.below()) instanceof TileEntityAshpit ashpit) {
					ashpit.ashLevelFly += ashTick;
					ashpit.ashLevelSoot += sootTick;
				}
				this.ashTick = 0;
				this.sootTick = 0;
			}

			networkPackNT(150);

			if(onTicks > 0) onTicks--;

		} else {

			if(onTicks > 0) {
				this.spawnParticles();
			}
		}
	}

	public boolean cpaturesAsh() {
		return true;
	}

	public boolean cpaturesSoot() {
		return false;
	}

	public void spawnParticles() { }

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.onTicks);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.onTicks = buf.readInt();
	}

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		return dir.getAxis().isHorizontal() && isSmoke(type);
	}

	@Override
	public long transferFluid(FluidType type, int pressure, long fluid) {

		if(!isSmoke(type)) return fluid;

		onTicks = 20;

		if(cpaturesAsh()) ashTick += fluid;
		if(cpaturesSoot()) sootTick += fluid;

		fluid *= getPollutionMod();

		if(type == Fluids.SMOKE) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, fluid / 100F);
		if(type == Fluids.SMOKE_LEADED) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.HEAVYMETAL, fluid / 100F);
		if(type == Fluids.SMOKE_POISON) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.POISON, fluid / 100F);

		return 0;
	}

	public abstract double getPollutionMod();

	@Override
	public long getDemand(FluidType type, int pressure) {
		return 1_000_000;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {};
	}
}
