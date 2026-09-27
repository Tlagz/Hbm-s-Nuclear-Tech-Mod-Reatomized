package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Air intake: a powered fan filling a small tank with compressed air, 100 HE per tick */
public class TileEntityMachineIntake extends TileEntityLoadedBase implements IEnergyReceiverMK2, IFluidStandardSenderMK2 {

	public FluidTank compair;
	public long power;

	public float fan = 0;
	public float prevFan = 0;
	private AudioWrapper audio;

	public TileEntityMachineIntake(BlockPos pos, BlockState state) {
		super(ModTileEntities.INTAKE.get(), pos, state);
		this.compair = new FluidTank(Fluids.AIR, 1_000);
	}

	@Override
	public void updateEntity() {

		if(!level.isClientSide) {

			if(this.power >= this.getMaxPower() / 20) {
				this.compair.setFill(this.compair.getMaxFill());
				this.power -= this.getMaxPower() / 20;
			}

			this.autoPort(getConPos());

			this.networkPackNT(50);

		} else {

			this.prevFan = this.fan;

			if(this.power >= this.getMaxPower() / 20) {
				this.fan += 45;

				if(this.fan >= 360) {
					this.fan -= 360;
					this.prevFan -= 360;
				}

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.keepAlive();
				audio.updateVolume(this.getVolume(0.25F));

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.relative(dir), dir),
				new DirPos(p.relative(dir).relative(rot), dir),
				new DirPos(p.relative(dir, -2), dir.getOpposite()),
				new DirPos(p.relative(dir, -2).relative(rot), dir.getOpposite()),
				new DirPos(p.relative(rot, 2), rot),
				new DirPos(p.relative(rot, 2).relative(dir, -1), rot),
				new DirPos(p.relative(rot, -1), rot.getOpposite()),
				new DirPos(p.relative(rot, -1).relative(dir, -1), rot.getOpposite())
		};
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.motor", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 0.25F, 10F, 1.0F, 20);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(audio != null) { audio.stopSound(); audio = null; }
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(audio != null) { audio.stopSound(); audio = null; }
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		compair.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		compair.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
		compair.readFromNBT(nbt, "compair");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		compair.writeToNBT(nbt, "compair");
	}

	@Override public boolean canConnect(Direction dir) { return dir.getAxis().isHorizontal(); }
	@Override public boolean canConnect(FluidType type, Direction dir) { return type == Fluids.AIR && dir.getAxis().isHorizontal(); }

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return 2_000; }

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {compair}; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {compair}; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 1, worldPosition.getZ() + 2);
	}
}
