package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerMachineSiren;
import com.hbm.items.machine.ItemCassette;
import com.hbm.items.machine.ItemCassette.SoundType;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Siren: plays the cassette's track while powered by redstone. Looping tracks run as long as the signal is on,
 * one-shot tracks play once per rising edge. The sound fades linearly over the track's volume in blocks.
 * The original sent a TESirenPacket every tick, here the state goes through the tile's buf packet.
 */
public class TileEntityMachineSiren extends TileEntityMachineBase implements MenuProvider {

	public boolean lock = false;

	/** Synced: the track ordinal (-1 for none), whether a loop is running, and a counter bumped for every one-shot */
	public int trackId = -1;
	public boolean active;
	public int triggers;

	private int lastTriggers;
	private AudioWrapper audio;
	private int audioTrack = -1;

	public TileEntityMachineSiren(BlockPos pos, BlockState state) {
		super(ModTileEntities.SIREN.get(), pos, state, 1);
	}

	@Override
	public String getName() {
		return "container.siren";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 0 };
	}

	public TrackType getCurrentType() {
		return ItemCassette.getType(slots.get(0));
	}

	@Override
	public void updateEntity() {

		if(!level.isClientSide) {
			TrackType type = getCurrentType();
			this.trackId = type == null ? -1 : type.ordinal();

			if(type == null) {
				this.active = false;
			} else {
				boolean powered = level.hasNeighborSignal(worldPosition);

				if(type.getType() == SoundType.LOOP) {
					this.active = powered;
				} else {
					this.active = false;
					if(!lock && powered) {
						lock = true;
						triggers++;
					}
					if(lock && !powered) {
						lock = false;
					}
				}
			}

			this.networkPackNT(1500);

		} else {
			TrackType type = trackId < 0 ? null : TrackType.values()[trackId];

			// looping tracks
			boolean shouldLoop = type != null && type.getType() == SoundType.LOOP && active;

			if(shouldLoop && (audio == null || audioTrack != trackId || !audio.isPlaying())) {
				stopAudio();
				audio = AudioWrapper.getLoopedSound(type.getSoundLocation(), worldPosition.getX() + 0.5F, worldPosition.getY() + 0.5F, worldPosition.getZ() + 0.5F, 2F, type.getVolume(), 1F);
				audioTrack = trackId;
				audio.startSound();
			} else if(!shouldLoop && audio != null && audioTrack >= 0 && TrackType.values()[audioTrack].getType() == SoundType.LOOP) {
				stopAudio();
			}

			// one-shots
			if(triggers != lastTriggers) {
				lastTriggers = triggers;
				if(type != null && type.getType() != SoundType.LOOP) {
					stopAudio();
					audio = AudioWrapper.getLoopedSound(type.getSoundLocation(), worldPosition.getX() + 0.5F, worldPosition.getY() + 0.5F, worldPosition.getZ() + 0.5F, 2F, type.getVolume(), 1F);
					audio.setDoesRepeat(false);
					audioTrack = trackId;
					audio.startSound();
				}
			}
		}
	}

	private void stopAudio() {
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
		audioTrack = -1;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(trackId);
		buf.writeBoolean(active);
		buf.writeInt(triggers);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		boolean first = this.trackId == -1 && this.triggers == 0 && this.lastTriggers == 0;
		this.trackId = buf.readInt();
		this.active = buf.readBoolean();
		this.triggers = buf.readInt();
		// don't replay an old one-shot when the chunk gets loaded
		if(first) this.lastTriggers = this.triggers;
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		stopAudio();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		stopAudio();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineSiren(id, inv, this);
	}
}
