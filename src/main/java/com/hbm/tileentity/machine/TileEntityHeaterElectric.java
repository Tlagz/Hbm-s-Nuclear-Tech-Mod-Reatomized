package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.tile.IHeatSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Electric heater: turns power into heat for whatever sits on it, in 10 settings changed with the screwdriver
 * (100 TU/t per setting, the power use grows faster). Heat of a heater below is passed on at 85%.
 */
public class TileEntityHeaterElectric extends TileEntityLoadedBase implements IHeatSource, IEnergyReceiverMK2 {

	public long power;
	public int heatEnergy;
	public boolean isOn;
	protected int setting = 0;

	private AudioWrapper audio;

	public TileEntityHeaterElectric(BlockPos pos, BlockState state) {
		super(ModTileEntities.HEATER_ELECTRIC.get(), pos, state);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(level.getGameTime() % 20 == 0) { //doesn't have to happen constantly
				Direction dir = Direction.from3DDataValue(BlockDummyable.getMeta(getBlockState()) - BlockDummyable.offset);
				this.trySubscribe(level, worldPosition.relative(dir, 3), dir);
			}

			this.heatEnergy *= 0.999;

			this.tryPullHeat();

			this.isOn = false;

			if(setting > 0 && this.power >= this.getConsumption()) {
				this.power -= this.getConsumption();
				this.heatEnergy += getHeatGen();
				this.isOn = true;
			}

			networkPackNT(25);
		} else {

			if(isOn) {
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
		return AudioWrapper.getLoopedSound("hbm:block.electricHum", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 0.25F, 7.5F, 1.0F, 20);
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
		buf.writeBoolean(this.muffled);
		buf.writeByte(this.setting);
		buf.writeInt(this.heatEnergy);
		buf.writeBoolean(this.isOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.muffled = buf.readBoolean();
		this.setting = buf.readByte();
		this.heatEnergy = buf.readInt();
		this.isOn = buf.readBoolean();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
		this.setting = nbt.getInt("setting");
		this.heatEnergy = nbt.getInt("heatEnergy");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putInt("setting", setting);
		nbt.putInt("heatEnergy", heatEnergy);
	}

	protected void tryPullHeat() {
		BlockEntity con = level.getBlockEntity(worldPosition.below());

		if(con instanceof IHeatSource source) {
			this.heatEnergy += source.getHeatStored() * 0.85;
			source.useUpHeat(source.getHeatStored());
		}
	}

	public void toggleSetting() {
		setting++;

		if(setting > 10)
			setting = 0;
	}

	public int getSetting() {
		return setting;
	}

	@Override
	public long getPower() {
		return power;
	}

	public long getConsumption() {
		return (long) (Math.pow(setting, 1.4D) * 200D);
	}

	@Override
	public long getMaxPower() {
		return getConsumption() * 20;
	}

	public int getHeatGen() {
		return this.setting * 100;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public int getHeatStored() {
		return heatEnergy;
	}

	@Override
	public void useUpHeat(int heat) {
		this.heatEnergy = Math.max(0, this.heatEnergy - heat);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 1, worldPosition.getZ() + 3);
	}
}
