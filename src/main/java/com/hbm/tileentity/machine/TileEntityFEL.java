package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerFEL;
import com.hbm.items.machine.ItemFELCrystal;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.lib.Library;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Free electron laser: shoots a beam of the crystal's wavelength (needs 1250 * 3^wavelength HE per tick) up to 24
 * blocks, powering SILEX chambers in its path (aligned with it, 5 blocks away at least) and burning whatever it hits.
 * Slots: 0 battery, 1 laser crystal.
 *
 * TODO digamma fire and ash for the DRX laser (the blocks aren't ported yet, it sets normal fire)
 */
public class TileEntityFEL extends TileEntityMachineBase implements IEnergyReceiverMK2, IControlReceiver, MenuProvider {

	public long power;
	public static final long maxPower = 20000000;
	public static final int powerReq = 1250;
	public EnumWavelengths mode = EnumWavelengths.NULL;
	public boolean isOn;
	public boolean missingValidSilex = true;
	public int distance;

	private int audioDuration = 0;
	private AudioWrapper audio;

	public TileEntityFEL(BlockPos pos, BlockState state) {
		super(ModTileEntities.FEL.get(), pos, state, 2);
	}

	@Override
	public String getName() {
		return "container.machineFEL";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			Direction dir = BlockDummyable.getRotation(getBlockState());
			this.trySubscribe(level, worldPosition.relative(dir, -5).above(), dir.getOpposite());
			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			if(this.isOn && slots.get(1).getItem() instanceof ItemFELCrystal crystal) {
				this.mode = crystal.wavelength;
			} else {
				this.mode = EnumWavelengths.NULL;
			}

			int range = 24;
			boolean silexSpacing = false;
			int req = (int) (powerReq * ((mode.ordinal() == 0) ? 0 : Math.pow(3, mode.ordinal())));

			if(this.isOn && this.mode != EnumWavelengths.NULL && power < req) {
				this.power = 0;
			}

			if(this.isOn && power >= req && this.mode != EnumWavelengths.NULL) {

				hurtEntities(dir);

				power -= req;

				for(int i = 3; i < range; i++) {

					BlockPos pos = worldPosition.relative(dir, i).above();
					BlockState state = level.getBlockState(pos);

					// the chamber is checked first, multiblocks don't occlude (they're drawn by their renderers)
					if(state.is(ModBlocks.machine_silex.get())) {

						// the beam hits the chamber's upper part, the core is right behind it at the bottom
						if(level.getBlockEntity(worldPosition.relative(dir, i + 1)) instanceof TileEntitySILEX silex) {
							Direction silexDir = BlockDummyable.getRotation(silex.getBlockState());

							if((silexDir == dir || silexDir == dir.getOpposite()) && i >= 5 && !silexSpacing) {
								if(silex.mode != this.mode) {
									silex.mode = this.mode;
									this.missingValidSilex = false;
									silexSpacing = true;
									continue;
								}
							} else {
								// a wrongly placed chamber gets knocked off
								level.destroyBlock(silex.getBlockPos(), true);
							}
						}

					} else if(isTransparent(state, pos)) {
						// the beam goes through anything see-through
						this.distance = range;
						silexSpacing = false;

					} else {

						this.distance = i;

						float hardness = state.getBlock().getExplosionResistance();

						if(hardness < 75 && level.random.nextInt(5) == 0) {
							level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
							level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
						}
						break;
					}
				}
			}

			this.networkPackNT(250);

		} else {

			if(power > powerReq * Math.pow(2, mode.ordinal()) && isOn && mode != EnumWavelengths.NULL && distance - 3 > 0) {
				audioDuration += 2;
			} else {
				audioDuration -= 3;
			}

			audioDuration = Mth.clamp(audioDuration, 0, 60);

			if(audioDuration > 10) {
				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}
				audio.keepAlive();
				audio.updateVolume(getVolume(2F));
				audio.updatePitch((audioDuration - 10) / 100F + 0.5F);
			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	/**
	 * The original checked the block's material for opacity; here see-through means air, plants, glass and the like.
	 * Multiblocks and other shaped blocks with a solid material (iron, stone, wood) still block the beam.
	 */
	private boolean isTransparent(BlockState state, BlockPos pos) {
		if(state.is(Blocks.TNT)) return false;
		if(state.isAir() || !state.getFluidState().isEmpty()) return true;
		if(state.canOcclude()) return false;
		return state.getBlock().getExplosionResistance() < 1F || state.propagatesSkylightDown(level, pos);
	}

	/** Everything in the beam gets burned, blinded, irradiated or digamma'd depending on the wavelength */
	private void hurtEntities(Direction dir) {
		int d = this.distance - 1;
		BlockPos end = worldPosition.relative(dir, d).above();
		AABB beam = new AABB(Math.min(worldPosition.getX(), end.getX()) + 0.2, worldPosition.getY() + 0.2, Math.min(worldPosition.getZ(), end.getZ()) + 0.2,
				Math.max(worldPosition.getX(), end.getX()) + 0.8, worldPosition.getY() + 1.8, Math.max(worldPosition.getZ(), end.getZ()) + 0.8);

		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, beam);

		for(LivingEntity entity : list) {
			switch(this.mode) {
			case VISIBLE: entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60 * 60 * 65536, 0));
			case IR:
			case UV: entity.igniteForSeconds(10); break;
			case GAMMA: ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, 25); break;
			case DRX: ContaminationUtil.applyDigammaData(entity, 0.1F); break;
			default: break;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeByte(mode.ordinal());
		buf.writeBoolean(isOn);
		buf.writeBoolean(missingValidSilex);
		buf.writeInt(distance);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		mode = EnumWavelengths.values()[buf.readByte()];
		isOn = buf.readBoolean();
		missingValidSilex = buf.readBoolean();
		distance = buf.readInt();
	}

	/** The on/off switch */
	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("toggle")) this.isOn = !this.isOn;
		this.setChanged();
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		power = nbt.getLong("power");
		isOn = nbt.getBoolean("isOn");
		missingValidSilex = nbt.getBoolean("valid");
		distance = nbt.getInt("distance");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putBoolean("isOn", isOn);
		nbt.putBoolean("valid", missingValidSilex);
		nbt.putInt("distance", distance);
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.fel", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 2.0F, 10F, 2.0F, 20);
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

	/** The beam goes 24 blocks in front */
	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition).inflate(26);
	}

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override
	public boolean isItemValidForSlot(int i, net.minecraft.world.item.ItemStack stack) {
		return i == 1 ? stack.getItem() instanceof ItemFELCrystal : i == 0;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerFEL(id, inv, this);
	}
}
