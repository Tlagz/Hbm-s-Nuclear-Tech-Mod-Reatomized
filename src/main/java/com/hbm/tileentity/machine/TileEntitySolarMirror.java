package com.hbm.tileentity.machine;

import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Heliostat mirror: while it sees the sky in daylight, adds its sunlight (sky light above 11) as heat to the solar
 * boiler it's aimed at (the target is the boiler core's block above, set with the mirror tool).
 */
public class TileEntitySolarMirror extends TileEntityLoadedBase {

	public int tX;
	public int tY;
	public int tZ;
	public boolean isOn;

	public TileEntitySolarMirror(BlockPos pos, BlockState state) {
		super(ModTileEntities.SOLAR_MIRROR.get(), pos, state);
	}

	@Override
	public void updateEntity() {

		BlockPos boilerPos = new BlockPos(tX, tY - 1, tZ);

		if(!level.isClientSide) {

			if(level.getGameTime() % 20 == 0)
				this.networkPackNT(200);

			if(tY < worldPosition.getY()) {
				isOn = false;
				return;
			}

			int sun = getSun();

			if(sun <= 0 || !level.canSeeSky(worldPosition.above())) {
				isOn = false;
				return;
			}

			isOn = true;

			if(level.getBlockEntity(boilerPos) instanceof TileEntitySolarBoiler boiler) {
				boiler.heat += sun;
			}
		} else {

			if(isOn && level.getBlockEntity(boilerPos) instanceof TileEntitySolarBoiler boiler) {
				boiler.primary.add(worldPosition);
			}
		}
	}

	/** The original's saved sky light minus the sky darkening, minus 11 */
	public int getSun() {
		return level.getBrightness(LightLayer.SKY, worldPosition) - level.getSkyDarken() - 11;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.tX);
		buf.writeInt(this.tY);
		buf.writeInt(this.tZ);
		buf.writeBoolean(this.isOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.tX = buf.readInt();
		this.tY = buf.readInt();
		this.tZ = buf.readInt();
		this.isOn = buf.readBoolean();
	}

	public void setTarget(int x, int y, int z) {
		tX = x;
		tY = y;
		tZ = z;
		this.setChanged();
		this.networkPackNT(200);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		tX = nbt.getInt("targetX");
		tY = nbt.getInt("targetY");
		tZ = nbt.getInt("targetZ");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("targetX", tX);
		nbt.putInt("targetY", tY);
		nbt.putInt("targetZ", tZ);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition).inflate(1);
	}
}
