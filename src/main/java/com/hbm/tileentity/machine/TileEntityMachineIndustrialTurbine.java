package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Coolable.CoolingType;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Industrial steam turbine: takes 20% of its steam each tick, the power goes into a flywheel that has to spool up
 * first (dense steam spools up a lot slower).
 *
 * TODO config (IConfigurableMachine), OpenComputers
 */
public class TileEntityMachineIndustrialTurbine extends TileEntityTurbineBase {

	public static int inputTankSize = 750_000;
	public static int outputTankSize = 3_000_000;
	public static double efficiency = 1D;

	public float rotor;
	public float lastRotor;

	public double spin = 0;
	public static double FLYWHEEL_MAX_ENERGY = 0.5e8; //aka flywheel mass
	public long maxPower = 0;
	public long lastPowerTarget = 0;
	public long flywheel_energy = 0;

	private AudioWrapper audio;
	private float audioDesync;

	public TileEntityMachineIndustrialTurbine(BlockPos pos, BlockState state) {
		super(ModTileEntities.INDUSTRIAL_TURBINE.get(), pos, state);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.STEAM, inputTankSize);
		tanks[1] = new FluidTank(Fluids.SPENTSTEAM, outputTankSize);

		audioDesync = new java.util.Random().nextFloat() * 0.05F;
	}

	// sets the power target so we know how much this steam type can theoretically make, and increments the spin based on actual throughput
	@Override
	public void generatePower(long power, int steamConsumed) {
		FT_Coolable trait = tanks[0].getTankType().getTrait(FT_Coolable.class);
		double eff = trait.getEfficiency(CoolingType.TURBINE) * getEfficiency();
		int maxOps = (int) Math.ceil((tanks[0].getMaxFill() * consumptionPercent()) / trait.amountReq);
		this.maxPower = (long) (maxOps * trait.heatEnergy * eff);

		this.flywheel_energy += power;
	}

	@Override
	public void onServerTick() {
		this.spin = (double) flywheel_energy / FLYWHEEL_MAX_ENERGY; //because dense steams have way lower energy output, turbines running them take a lot longer to spool up
		this.lastPowerTarget = Math.min((long) (Math.max(this.spin, 0.05) * maxPower), this.flywheel_energy);
		this.flywheel_energy -= this.lastPowerTarget;
		this.powerBuffer = (long) (this.lastPowerTarget);
	}

	@Override
	public void onClientTick() {

		this.lastRotor = this.rotor;
		float speed = this.spin >= 0.5 ? 30 : (float) (Math.pow(this.spin * 2, 0.5) * 30);
		this.rotor += speed;

		if(this.rotor >= 360) {
			this.lastRotor -= 360;
			this.rotor -= 360;
		}

		if(this.spin > 0 && com.hbm.main.ClientHooks.distanceToPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 35) {

			float spinNum = (float) Math.min(1F, spin * 2);
			float volume = this.getVolume(0.25F + spinNum * 0.75F);
			float pitch = 0.5F + spinNum * 0.5F + this.audioDesync;

			if(audio == null) {
				audio = AudioWrapper.getLoopedSound("hbm:block.largeTurbineRunning", worldPosition.getX() + 0.5F, worldPosition.getY() + 0.5F, worldPosition.getZ() + 0.5F, volume, 20F, pitch, 20);
				audio.startSound();
			}

			audio.keepAlive();
			audio.updatePitch(pitch);
			audio.updateVolume(volume);

		} else {
			if(audio != null) {
				audio.stopSound();
				audio = null;
			}
		}
	}

	@Override
	public boolean canConnect(Direction dir) {
		return dir == BlockDummyable.getRotation(getBlockState()).getOpposite();
	}

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		if(!type.hasTrait(FT_Coolable.class) && type != Fluids.SPENTSTEAM) return false;
		Direction myDir = BlockDummyable.getRotation(getBlockState());
		return dir != myDir && dir != myDir.getOpposite();
	}

	@Override public double consumptionPercent() { return 0.2D; }
	@Override public double getEfficiency() { return efficiency; }
	@Override public boolean doesResizeCompressor() { return true; }

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeDouble(this.spin);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.spin = buf.readDouble();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		lastPowerTarget = nbt.getLong("lastPowerTarget");
		flywheel_energy = nbt.getLong("flywheel_energy");
		maxPower = nbt.getLong("maxPower");
		spin = nbt.getDouble("spin");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("lastPowerTarget", lastPowerTarget);
		nbt.putLong("flywheel_energy", flywheel_energy);
		nbt.putLong("maxPower", maxPower);
		nbt.putDouble("spin", spin);
	}

	@Override
	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.relative(dir, 3).relative(rot, 2), rot),
				new DirPos(p.relative(dir, 3).relative(rot, -2), rot.getOpposite()),
				new DirPos(p.relative(dir, -1).relative(rot, 2), rot),
				new DirPos(p.relative(dir, -1).relative(rot, -2), rot.getOpposite()),
				new DirPos(p.relative(dir, 3).above(3), Direction.UP),
				new DirPos(p.relative(dir, -1).above(3), Direction.UP),
		};
	}

	@Override
	public DirPos[] getPowerPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		return new DirPos[] {
				new DirPos(worldPosition.relative(dir, -4).above(), dir.getOpposite())
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 3, worldPosition.getY(), worldPosition.getZ() - 3, worldPosition.getX() + 4, worldPosition.getY() + 3, worldPosition.getZ() + 4);
	}
}
