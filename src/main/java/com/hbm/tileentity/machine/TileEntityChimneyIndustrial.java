package com.hbm.tileentity.machine;

import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Industrial smokestack, releases 10% of the pollution and also catches soot */
public class TileEntityChimneyIndustrial extends TileEntityChimneyBase {

	public TileEntityChimneyIndustrial(BlockPos pos, BlockState state) {
		super(ModTileEntities.CHIMNEY_INDUSTRIAL.get(), pos, state);
	}

	@Override
	public void spawnParticles() {

		if(level.getGameTime() % 2 == 0) {
			CompoundTag fx = new CompoundTag();
			fx.putString("type", "tower");
			fx.putFloat("lift", 10F);
			fx.putFloat("base", 0.75F);
			fx.putFloat("max", 3F);
			fx.putInt("life", 250 + level.random.nextInt(50));
			fx.putInt("color", 0x404040);
			fx.putDouble("posX", worldPosition.getX() + 0.5);
			fx.putDouble("posY", worldPosition.getY() + 22);
			fx.putDouble("posZ", worldPosition.getZ() + 0.5);
			com.hbm.particle.ParticleEffectsNT.effectNT(fx);
		}
	}

	@Override
	public double getPollutionMod() {
		return 0.1D;
	}

	@Override
	public boolean cpaturesSoot() {
		return true;
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 23, worldPosition.getZ() + 2);
	}
}
