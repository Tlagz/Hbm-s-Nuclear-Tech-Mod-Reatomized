package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.main.ModSounds;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.DirPos;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.tile.IHeatSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Industrial boiler: the big boiler, four times the tanks and heat buffer and it doesn't explode.
 * TODO config (IConfigurableMachine), copy tool, RoR values, heating from the post-impact fires (TomSaveData)
 */
public class TileEntityHeatBoilerIndustrial extends TileEntityLoadedBase implements IFluidStandardTransceiverMK2 {

	public int heat;
	public FluidTank[] tanks;
	public boolean isOn;

	private AudioWrapper audio;
	private int audioTime;

	/* CONFIGURABLE */
	public static int maxHeat = 12_800_000;
	public static double diffusion = 0.1D;

	public TileEntityHeatBoilerIndustrial(BlockPos pos, BlockState state) {
		super(ModTileEntities.BOILER_INDUSTRIAL.get(), pos, state);
		this.tanks = new FluidTank[2];

		this.tanks[0] = new FluidTank(Fluids.WATER, 64_000);
		this.tanks[1] = new FluidTank(Fluids.STEAM, 64_000 * 100);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.setupTanks();
			this.updateConnections();
			this.tryPullHeat();

			this.isOn = false;
			this.tryConvert();

			if(this.tanks[1].getFill() > 0) {
				this.sendFluid();
			}

			networkPackNT(25);
		} else {

			if(this.isOn) audioTime = 20;

			if(audioTime > 0) {

				audioTime--;

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.updateVolume(getVolume(1F));
				audio.keepAlive();

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.boiler", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 0.125F, 10F, 1.0F, 20);
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
		buf.writeInt(this.heat);
		this.tanks[0].serialize(buf);
		this.tanks[1].serialize(buf);
		buf.writeBoolean(this.isOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.heat = buf.readInt();
		this.tanks[0].deserialize(buf);
		this.tanks[1].deserialize(buf);
		this.isOn = buf.readBoolean();
	}

	protected void tryPullHeat() {

		if(level.getBlockEntity(worldPosition.below()) instanceof IHeatSource source) {
			int diff = source.getHeatStored() - this.heat;

			if(diff == 0) {
				return;
			}

			if(diff > 0) {
				diff = (int) Math.ceil(diff * diffusion);
				diff = Math.min(diff, maxHeat - this.heat);
				source.useUpHeat(diff);
				this.heat += diff;
				if(this.heat > maxHeat)
					this.heat = maxHeat;
				return;
			}
		}

		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}

	protected void setupTanks() {

		if(tanks[0].getTankType().hasTrait(FT_Heatable.class)) {
			FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
			if(trait.getEfficiency(HeatingType.BOILER) > 0) {
				HeatingStep entry = trait.getFirstStep();
				tanks[1].setTankType(entry.typeProduced);
				tanks[1].changeTankSize(tanks[0].getMaxFill() * entry.amountProduced / entry.amountReq);
				return;
			}
		}

		tanks[0].setTankType(Fluids.NONE);
		tanks[1].setTankType(Fluids.NONE);
	}

	protected void tryConvert() {

		if(tanks[0].getTankType().hasTrait(FT_Heatable.class)) {
			FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
			if(trait.getEfficiency(HeatingType.BOILER) > 0) {

				HeatingStep entry = trait.getFirstStep();
				int heatReq = (int) Math.max(entry.heatReq / trait.getEfficiency(HeatingType.BOILER), 1);
				int inputOps = this.tanks[0].getFill() / entry.amountReq;
				int outputOps = (this.tanks[1].getMaxFill() - this.tanks[1].getFill()) / entry.amountProduced;
				int heatOps = this.heat / heatReq;

				int ops = Math.min(inputOps, Math.min(outputOps, heatOps));

				this.tanks[0].setFill(this.tanks[0].getFill() - entry.amountReq * ops);
				this.tanks[1].setFill(this.tanks[1].getFill() + entry.amountProduced * ops);
				this.heat -= heatReq * ops;

				if(ops > 0 && level.random.nextInt(400) == 0) {
					level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 2, worldPosition.getZ() + 0.5, ModSounds.get("block.boilerGroan"), SoundSource.BLOCKS, 0.5F, 1.0F);
				}

				if(ops > 0) {
					this.isOn = true;
				}
			}
		}
	}

	private void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(tanks[0].getTankType(), level, pos, pos.getDir());
		}
	}

	private void sendFluid() {
		for(DirPos pos : getConPos()) {
			this.tryProvide(tanks[1], level, pos, pos.getDir());
		}
	}

	/** Water in and steam out on all four sides and on top */
	private DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(worldPosition.east(2), Direction.EAST),
				new DirPos(worldPosition.west(2), Direction.WEST),
				new DirPos(worldPosition.south(2), Direction.SOUTH),
				new DirPos(worldPosition.north(2), Direction.NORTH),
				new DirPos(worldPosition.above(5), Direction.UP),
		};
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "steam");
		heat = nbt.getInt("heat");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "steam");
		nbt.putInt("heat", heat);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[1]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0]};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 5, worldPosition.getZ() + 2);
	}
}
