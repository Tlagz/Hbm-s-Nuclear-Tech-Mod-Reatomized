package com.hbm.tileentity.machine;

import java.util.HashSet;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Solar tower boiler: every heliostat aimed at it adds heat each tick, every 50 heat boils 1 mB of water into 100 mB
 * of steam. Water in and steam out at the top and the bottom. On the client, mirrors report themselves every tick
 * for the light beams.
 */
public class TileEntitySolarBoiler extends TileEntityLoadedBase implements IFluidStandardTransceiverMK2 {

	private FluidTank water;
	private FluidTank steam;
	public int display;
	public int heat;

	public HashSet<BlockPos> primary = new HashSet<>();
	public HashSet<BlockPos> secondary = new HashSet<>();

	public TileEntitySolarBoiler(BlockPos pos, BlockState state) {
		super(ModTileEntities.SOLAR_BOILER.get(), pos, state);
		water = new FluidTank(Fluids.WATER, 100);
		steam = new FluidTank(Fluids.STEAM, 10_000);
	}

	@Override
	public void updateEntity() {

		if(!level.isClientSide) {

			this.trySubscribe(water.getTankType(), level, worldPosition.above(3), Direction.UP);
			this.trySubscribe(water.getTankType(), level, worldPosition.below(), Direction.DOWN);

			int process = heat / 50;
			this.display = process;
			process = Math.min(process, water.getFill());
			process = Math.min(process, (steam.getMaxFill() - steam.getFill()) / 100);

			if(process < 0) process = 0;

			water.setFill(water.getFill() - process);
			steam.setFill(steam.getFill() + process * 100);

			this.tryProvide(steam, level, worldPosition.above(3), Direction.UP);
			this.tryProvide(steam, level, worldPosition.below(), Direction.DOWN);

			heat = 0;

			networkPackNT(15);
		} else {

			//a delayed queue of mirror positions because we can't expect the boiler to always tick first
			secondary.clear();
			secondary.addAll(primary);
			primary.clear();
		}
	}

	public FluidTank getWater() { return water; }
	public FluidTank getSteam() { return steam; }

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.water.readFromNBT(nbt, "water");
		this.steam.readFromNBT(nbt, "steam");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.water.writeToNBT(nbt, "water");
		this.steam.writeToNBT(nbt, "steam");
	}

	/** The light beams reach far, the mirrors can be 100 blocks away */
	public AABB getRenderBoundingBox() {
		return AABB.INFINITE;
	}

	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { steam }; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { water }; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] { water, steam }; }

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(display);
		water.serialize(buf);
		steam.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.display = buf.readInt();
		water.deserialize(buf);
		steam.deserialize(buf);
	}
}
