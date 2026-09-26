package com.hbm.tileentity.machine.storage;

import java.math.BigInteger;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.container.ContainerBatteryREDD;
import com.hbm.items.ModDataComponents;
import com.hbm.lib.Library;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * REDD (the FEnSU's successor): stores an unlimited amount of energy as a BigInteger, the wheel spins faster the
 * more is stored. Slot 0 charges from an item, slot 1 charges an item. The charge stays on the dropped item.
 *
 * TODO muffler, RoR/OC
 */
public class TileEntityBatteryREDD extends TileEntityBatteryBase implements MenuProvider {

	public float prevRotation = 0F;
	public float rotation = 0F;

	public BigInteger[] log = new BigInteger[20];
	public BigInteger delta = BigInteger.valueOf(0);

	public BigInteger power = BigInteger.valueOf(0);

	private AudioWrapper audio;

	public TileEntityBatteryREDD(BlockPos pos, BlockState state) {
		super(ModTileEntities.BATTERY_REDD.get(), pos, state, 2);
	}

	@Override public String getName() { return "container.batteryREDD"; }

	@Override
	public void updateEntity() {

		BigInteger prevPower = this.power;

		super.updateEntity();

		if(isServer()) {

			long toAdd = Library.chargeTEFromItems(slots, 0, 0, this.getMaxPower());
			if(toAdd > 0) this.power = this.power.add(BigInteger.valueOf(toAdd));

			long toRemove = this.getPower() - Library.chargeItemsFromTE(slots, 1, this.getPower(), this.getMaxPower());
			if(toRemove > 0) this.power = this.power.subtract(BigInteger.valueOf(toRemove));

			BigInteger avg = this.power.add(prevPower).divide(BigInteger.valueOf(2));
			this.delta = avg.subtract(this.log[0] == null ? BigInteger.ZERO : this.log[0]);

			for(int i = 1; i < this.log.length; i++) {
				this.log[i - 1] = this.log[i];
			}

			this.log[19] = avg;

		} else {

			this.prevRotation = this.rotation;
			this.rotation += this.getSpeed();

			if(rotation >= 360) {
				rotation -= 360;
				prevRotation -= 360;
			}

			float pitch = 0.5F + this.getSpeed() / 15F * 1.5F;

			if(this.prevRotation != this.rotation && com.hbm.main.ClientHooks.distanceToPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 5.5, worldPosition.getZ() + 0.5) < 30) {
				if(this.audio == null || !this.audio.isPlaying()) {
					this.audio = AudioWrapper.getLoopedSound("hbm:block.fensuHum", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), this.getVolume(1.5F), 25F, pitch, 5);
					this.audio.startSound();
				}

				this.audio.updateVolume(this.getVolume(1.5F));
				this.audio.updatePitch(pitch);
				this.audio.keepAlive();

			} else {
				if(this.audio != null) {
					this.audio.stopSound();
					this.audio = null;
				}
			}
		}
	}

	public float getSpeed() {
		return (float) Math.min(Math.pow(Math.log(this.power.doubleValue() * 0.05 + 1) * 0.05F, 5), 15F);
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

	private static void writeBig(ByteBuf buf, BigInteger value) {
		byte[] array = value.toByteArray();
		buf.writeInt(array.length);
		buf.writeBytes(array);
	}

	private static BigInteger readBig(ByteBuf buf) {
		byte[] array = new byte[buf.readInt()];
		buf.readBytes(array);
		return new BigInteger(array);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		writeBig(buf, this.power);
		writeBig(buf, this.delta);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = readBig(buf);
		this.delta = readBig(buf);
	}

	private static BigInteger bigFrom(CompoundTag nbt, String key) {
		byte[] bytes = nbt.getByteArray(key);
		return bytes.length == 0 ? BigInteger.ZERO : new BigInteger(bytes);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = bigFrom(nbt, "power");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putByteArray("power", this.power.toByteArray());
	}

	/** The charge stays on the dropped item (the original's IPersistentNBT) */
	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		if(this.power.signum() == 0) return;
		CompoundTag data = new CompoundTag();
		data.putByteArray("power", this.power.toByteArray());
		components.set(ModDataComponents.PERSISTENT.get(), CustomData.of(data));
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		CustomData data = input.get(ModDataComponents.PERSISTENT.get());
		if(data != null) this.power = bigFrom(data.copyTag(), "power");
	}

	@Override
	public BlockPos[] getPortPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;
		return new BlockPos[] {
				p.relative(dir, 2).relative(rot, 2),
				p.relative(dir, 2).relative(rot, -2),
				p.relative(dir, -2).relative(rot, 2),
				p.relative(dir, -2).relative(rot, -2),
				p.relative(rot, 4),
				p.relative(rot, -4),
		};
	}

	@Override
	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.relative(dir, 3).relative(rot, 2), dir),
				new DirPos(p.relative(dir, 3).relative(rot, -2), dir),
				new DirPos(p.relative(dir, -3).relative(rot, 2), dir.getOpposite()),
				new DirPos(p.relative(dir, -3).relative(rot, -2), dir.getOpposite()),
				new DirPos(p.relative(rot, 5), rot),
				new DirPos(p.relative(rot, -5), rot.getOpposite()),
		};
	}

	@Override
	public void usePower(long power) {
		this.power = this.power.subtract(BigInteger.valueOf(power));
	}

	@Override
	public long transferPower(long power) {
		this.power = this.power.add(BigInteger.valueOf(power));
		return 0L;
	}

	@Override public long getPower() { return this.power.min(BigInteger.valueOf(getMaxPower() / 2)).longValue(); } // for provision
	@Override public void setPower(long power) { } // not needed since we use transferPower and usePower directly
	@Override public long getMaxPower() { return Long.MAX_VALUE / 100L; } // for connection speed

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 4, worldPosition.getY(), worldPosition.getZ() - 4, worldPosition.getX() + 5, worldPosition.getY() + 10, worldPosition.getZ() + 5);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerBatteryREDD(id, inv, this);
	}
}
