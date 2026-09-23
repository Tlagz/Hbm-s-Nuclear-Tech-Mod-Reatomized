package com.hbm.tileentity;

import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.tile.ILoadedTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class of (almost) all NTM tile entities.
 *
 * TODO fluid ports in autoPort (fluid system), tilting (machine gravity), audio, buf packets
 */
public abstract class TileEntityLoadedBase extends BlockEntity implements ILoadedTile {

	public boolean isLoaded = true;
	public boolean muffled = false;
	public boolean tilted = false;

	public int tickOffset = -1;
	public int[] energyRecDelay;
	public int[] energyProDelay;

	public TileEntityLoadedBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/** Equivalent of 1.7.10's updateEntity, called every tick on both sides by the ticker from {@link #ticker()} */
	public void updateEntity() { }

	/** Ticker for blocks that create this kind of tile, calls {@link #updateEntity()} */
	public static <T extends BlockEntity> BlockEntityTicker<T> ticker() {
		return (level, pos, state, tile) -> ((TileEntityLoadedBase) tile).updateEntity();
	}

	/** Automatic handling of ports, including dynamic pauses for ports not currently in use. Mainly a shitty bandaid fix. */
	public void autoPort(DirPos[] pos) {

		if(this.tickOffset == -1) {
			this.tickOffset = Math.abs(getIdentity(worldPosition) % 100);
		}

		if(this.energyRecDelay == null || this.energyRecDelay.length != pos.length) this.energyRecDelay = new int[pos.length];
		if(this.energyProDelay == null || this.energyProDelay.length != pos.length) this.energyProDelay = new int[pos.length];

		IEnergyReceiverMK2 energyRec = this instanceof IEnergyReceiverMK2 rec ? rec : null;
		IEnergyProviderMK2 energyProv = this instanceof IEnergyProviderMK2 prov ? prov : null;

		for(int i = 0; i < pos.length; i++) {
			if(energyRec != null) { if(energyRecDelay[i] > 0) energyRecDelay[i]--; else energyRecDelay[i] = energyRec.trySubscribe(level, pos[i]).delay; }
			if(energyProv != null) { if(energyProDelay[i] > 0) energyProDelay[i]--; else energyProDelay[i] = energyProv.tryProvide(level, pos[i]).delay; }
		}
	}

	/** The six neighbors, as ports pointing away from this tile */
	public DirPos[] allAround() {
		return DirPos.allAround(worldPosition);
	}

	/** Same hash as the original's BlockPos.getIdentity, used to spread out periodic work between tiles */
	public static int getIdentity(BlockPos pos) {
		return (pos.getY() + pos.getZ() * 27644437) * 27644437 + pos.getX();
	}

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		this.isLoaded = false;
	}

	/** The "chunks is modified, pls don't forget to save me" effect of markDirty, minus the block updates */
	public void markChanged() {
		if(level != null) level.blockEntityChanged(worldPosition);
	}

	public boolean isServer() {
		return level != null && !level.isClientSide;
	}

	public Level getWorld() {
		return level;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.muffled = nbt.getBoolean("muffled");
		this.tilted = nbt.getBoolean("tilted");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("muffled", muffled);
		nbt.putBoolean("tilted", tilted);
	}

	public float getVolume(float baseVolume) {
		return muffled ? baseVolume * 0.1F : baseVolume;
	}
}
