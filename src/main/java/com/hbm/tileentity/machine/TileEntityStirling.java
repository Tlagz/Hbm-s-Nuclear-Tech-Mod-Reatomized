package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.machine.MachineStirling;
import com.hbm.entity.projectile.EntityCog;
import com.hbm.items.ModDataComponents;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.tile.IHeatSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Stirling engine: turns the heat of the heater below into power (half of it, all of it for the creative one).
 * Too much heat overspeeds it: after a warning the gear flies off with an explosion and has to be put back.
 */
public class TileEntityStirling extends TileEntityLoadedBase implements IEnergyProviderMK2 {

	public long powerBuffer;
	public int heat;
	private int warnCooldown = 0;
	private int overspeed = 0;
	public boolean hasCog = true;

	public float spin;
	public float lastSpin;

	/* CONFIGURABLE CONSTANTS */
	public static double diffusion = 0.1D;
	public static double efficiency = 0.5D;
	public static int maxHeatNormal = 300;
	public static int maxHeatSteel = 1500;
	public static int overspeedLimit = 300;

	public TileEntityStirling(BlockPos pos, BlockState state) {
		super(ModTileEntities.STIRLING.get(), pos, state);
	}

	/** 0 normal, 1 steel, 2 creative, also the gear's meta */
	public int getGeatMeta() {
		return getBlockState().getBlock() instanceof MachineStirling stirling ? stirling.type : 0;
	}

	public int maxHeat() {
		return getGeatMeta() == 0 ? maxHeatNormal : maxHeatSteel;
	}

	public boolean isCreative() {
		return getGeatMeta() == 2;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(hasCog) {
				this.powerBuffer = 0;
				tryPullHeat();

				this.powerBuffer = (long) (this.heat * (this.isCreative() ? 1 : efficiency));

				if(warnCooldown > 0)
					warnCooldown--;

				if(heat > maxHeat() && !isCreative()) {

					this.overspeed++;

					if(overspeed > 60 && warnCooldown == 0) {
						warnCooldown = 100;
						level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, ModSounds.get("block.warnOverspeed"), SoundSource.BLOCKS, 2.0F, 1.0F);
					}

					if(overspeed > overspeedLimit) {
						this.hasCog = false;
						level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, 5F, Level.ExplosionInteraction.NONE);

						int orientation = BlockDummyable.getMeta(getBlockState()) - BlockDummyable.offset;
						Direction dir = Direction.from3DDataValue(orientation);
						EntityCog cog = new EntityCog(level, worldPosition.getX() + 0.5 + dir.getStepX(), worldPosition.getY() + 1, worldPosition.getZ() + 0.5 + dir.getStepZ()).setOrientation(orientation).setMeta(this.getGeatMeta());
						// the original's getRotation(DOWN), counter-clockwise
						Direction rot = dir.getCounterClockWise();
						cog.setDeltaMovement(rot.getStepX(), 1 + (heat - maxHeat()) * 0.0001D, rot.getStepZ());
						level.addFreshEntity(cog);

						this.setChanged();
					}

				} else {
					this.overspeed = 0;
				}
			} else {
				this.overspeed = 0;
				this.warnCooldown = 0;
			}

			networkPackNT(150);

			if(hasCog) {
				for(DirPos pos : getConPos()) {
					this.tryProvide(level, pos);
				}
			} else {

				if(this.powerBuffer > 0)
					this.powerBuffer--;
			}

			this.heat = 0;
		} else {

			float momentum = powerBuffer * 50F / ((float) maxHeat());

			if(this.isCreative()) momentum = Math.min(momentum, 45F);

			this.lastSpin = this.spin;
			this.spin += momentum;

			if(this.spin >= 360F) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}
		}
	}

	protected DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.east(2), Direction.EAST),
				new DirPos(p.west(2), Direction.WEST),
				new DirPos(p.south(2), Direction.SOUTH),
				new DirPos(p.north(2), Direction.NORTH)
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(this.powerBuffer);
		buf.writeInt(this.heat);
		buf.writeBoolean(this.hasCog);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.powerBuffer = buf.readLong();
		this.heat = buf.readInt();
		this.hasCog = buf.readBoolean();
	}

	protected void tryPullHeat() {
		BlockEntity con = level.getBlockEntity(worldPosition.below());

		if(con instanceof IHeatSource source) {
			int heatSrc = (int) (source.getHeatStored() * diffusion);

			if(heatSrc > 0) {
				source.useUpHeat(heatSrc);
				this.heat += heatSrc;
				return;
			}
		}

		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.powerBuffer = nbt.getLong("powerBuffer");
		this.hasCog = !nbt.contains("hasCog") || nbt.getBoolean("hasCog");
		this.overspeed = nbt.getInt("overspeed");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("powerBuffer", powerBuffer);
		nbt.putBoolean("hasCog", hasCog);
		nbt.putInt("overspeed", overspeed);
	}

	/** A gearless engine drops as a gearless item (the original's item damage 1) */
	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		if(!hasCog) components.set(ModDataComponents.NO_COG.get(), true);
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		if(input.getOrDefault(ModDataComponents.NO_COG.get(), false)) this.hasCog = false;
	}

	@Override
	public void setPower(long power) {
		this.powerBuffer = power;
	}

	@Override
	public long getPower() {
		return powerBuffer;
	}

	@Override
	public long getMaxPower() {
		return powerBuffer;
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 2, worldPosition.getZ() + 2);
	}
}
