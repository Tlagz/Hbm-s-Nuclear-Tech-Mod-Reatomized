package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Leviathan steam turbine: uses all the steam it has every tick, the huge fan spins up while it runs.
 * TODO config (IConfigurableMachine), OpenComputers
 */
public class TileEntityChungus extends TileEntityTurbineBase {

	private int turnTimer;
	public float rotor;
	public float lastRotor;
	public float fanAcceleration = 0F;

	private AudioWrapper audio;
	private float audioDesync;

	//Configurable values
	public static int inputTankSize = 1_000_000_000;
	public static int outputTankSize = 1_000_000_000;
	public static double efficiency = 0.85D;

	public TileEntityChungus(BlockPos pos, BlockState state) {
		super(ModTileEntities.CHUNGUS.get(), pos, state);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.STEAM, inputTankSize);
		tanks[1] = new FluidTank(Fluids.SPENTSTEAM, outputTankSize);

		audioDesync = new java.util.Random().nextFloat() * 0.05F;
	}

	@Override public double consumptionPercent() { return 1D; }
	@Override public double getEfficiency() { return efficiency; }

	@Override
	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		return new DirPos[] {
				new DirPos(worldPosition.relative(dir, 5).above(2), dir),
				new DirPos(worldPosition.relative(rot, 3), rot),
				new DirPos(worldPosition.relative(rot, -3), rot.getOpposite())
		};
	}

	@Override
	public DirPos[] getPowerPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		return new DirPos[] { new DirPos(worldPosition.relative(dir, -11), dir.getOpposite()) };
	}

	@Override
	public void onServerTick() {
		turnTimer--;
		if(operational) turnTimer = 25;
	}

	@Override
	public void onClientTick() {

		this.lastRotor = this.rotor;
		this.rotor += this.fanAcceleration;

		if(this.rotor >= 360) {
			this.rotor -= 360;
			this.lastRotor -= 360;
		}

		if(turnTimer > 0) {
			// Fan accelerates with a random offset to ensure the audio doesn't perfectly align, makes for a more pleasant hum
			this.fanAcceleration = Math.max(0F, Math.min(25F, this.fanAcceleration += 0.075F + audioDesync));

			RandomSource rand = level.random;
			Direction dir = BlockDummyable.getRotation(getBlockState());
			Direction side = dir.getClockWise();

			for(int i = 0; i < 10; i++) {
				level.addParticle(ParticleTypes.CLOUD,
						worldPosition.getX() + 0.5 + dir.getStepX() * (rand.nextDouble() + 1.25) + rand.nextGaussian() * side.getStepX() * 0.65,
						worldPosition.getY() + 2.5 + rand.nextGaussian() * 0.65,
						worldPosition.getZ() + 0.5 + dir.getStepZ() * (rand.nextDouble() + 1.25) + rand.nextGaussian() * side.getStepZ() * 0.65,
						-dir.getStepX() * 0.2, 0, -dir.getStepZ() * 0.2);
			}

			if(audio == null) {
				audio = AudioWrapper.getLoopedSound("hbm:block.chungusTurbineRunning", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1.0F, 20F, 1.0F, 20);
				audio.startSound();
			}

			float turbineSpeed = this.fanAcceleration / 25F;
			audio.updateVolume(getVolume(0.5f * turbineSpeed));
			audio.updatePitch(0.25F + 0.75F * turbineSpeed);
			audio.keepAlive();

		} else {
			this.fanAcceleration = Math.max(0F, Math.min(25F, this.fanAcceleration -= 0.1F));

			if(audio != null) {
				if(this.fanAcceleration > 0) {
					float turbineSpeed = this.fanAcceleration / 25F;
					audio.updateVolume(getVolume(0.5f * turbineSpeed));
					audio.updatePitch(0.25F + 0.75F * turbineSpeed);
				} else {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

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
		buf.writeInt(this.turnTimer);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.turnTimer = buf.readInt();
	}

	public AABB getRenderBoundingBox() {
		return AABB.INFINITE;
	}

	@Override
	public boolean canConnect(Direction dir) {
		return dir.getAxis().isHorizontal();
	}
}
