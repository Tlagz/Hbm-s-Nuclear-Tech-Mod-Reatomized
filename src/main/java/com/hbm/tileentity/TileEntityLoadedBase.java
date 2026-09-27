package com.hbm.tileentity;

import com.hbm.util.DirPos;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.sound.AudioWrapper;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import api.hbm.tile.ILoadedTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Base class of (almost) all NTM tile entities.
 *
 * TODO tilting (machine gravity)
 */
public abstract class TileEntityLoadedBase extends BlockEntity implements ILoadedTile, IBufPacketReceiver {

	public boolean isLoaded = true;
	public boolean muffled = false;
	public boolean tilted = false;

	public int tickOffset = -1;
	public int[] energyRecDelay;
	public int[] energyProDelay;
	public int[] fluidRecDelay;
	public int[] fluidProDelay;

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
		if(this.fluidRecDelay == null || this.fluidRecDelay.length != pos.length) this.fluidRecDelay = new int[pos.length];
		if(this.fluidProDelay == null || this.fluidProDelay.length != pos.length) this.fluidProDelay = new int[pos.length];

		IEnergyReceiverMK2 energyRec = this instanceof IEnergyReceiverMK2 rec ? rec : null;
		IEnergyProviderMK2 energyProv = this instanceof IEnergyProviderMK2 prov ? prov : null;
		IFluidStandardReceiverMK2 fluidRec = this instanceof IFluidStandardReceiverMK2 rec ? rec : null;
		IFluidStandardSenderMK2 fluidPro = this instanceof IFluidStandardSenderMK2 pro ? pro : null;

		for(int i = 0; i < pos.length; i++) {
			DirPos port = pos[i];

			if(energyRec != null) { if(energyRecDelay[i] > 0) energyRecDelay[i]--; else energyRecDelay[i] = energyRec.trySubscribe(level, port).delay; }
			if(energyProv != null) { if(energyProDelay[i] > 0) energyProDelay[i]--; else energyProDelay[i] = energyProv.tryProvide(level, port).delay; }

			if(fluidRec != null) if(fluidRecDelay[i] > 0) { fluidRecDelay[i]--; } else {
				for(FluidTank tank : fluidRec.getReceivingTanks()) {
					int newDelay = tank.getTankType() == Fluids.NONE ? 20 : fluidRec.trySubscribe(tank.getTankType(), level, port).delay;
					if(fluidRecDelay[i] <= 0 || newDelay < fluidRecDelay[i]) fluidRecDelay[i] = newDelay;
				}
			}

			if(fluidPro != null) if(fluidProDelay[i] > 0) { fluidProDelay[i]--; } else {
				for(FluidTank tank : fluidPro.getSendingTanks()) {
					int newDelay = tank.getFill() <= 0 ? 20 : fluidPro.tryProvide(tank, level, port).delay;
					if(fluidProDelay[i] <= 0 || newDelay < fluidProDelay[i]) fluidProDelay[i] = newDelay;
				}
			}
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

	/** The machine's looping sound (client side), null if it has none */
	public AudioWrapper createAudioLoop() { return null; }

	public AudioWrapper rebootAudio(AudioWrapper wrapper) {
		wrapper.stopSound();
		AudioWrapper audio = createAudioLoop();
		audio.startSound();
		return audio;
	}

	private byte[] lastPackedBuf;

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(muffled);
		buf.writeBoolean(tilted);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.muffled = buf.readBoolean();
		this.tilted = buf.readBoolean();
	}

	/** Sends a sync packet that uses ByteBuf for efficient information-cramming, to all players within range */
	public void networkPackNT(int range) {
		if(!(level instanceof ServerLevel server)) return;

		BufPacket packet = BufPacket.of(worldPosition, this);

		// Don't send unnecessary packets, except for maybe one every second or so,
		// clients that reload the chunk need the state again
		if(java.util.Arrays.equals(packet.data(), lastPackedBuf) && level.getGameTime() % 20 != 0) return;
		this.lastPackedBuf = packet.data();

		com.hbm.packet.PacketDispatcher.sendToPlayersNear(server, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, range, packet);
	}
}
