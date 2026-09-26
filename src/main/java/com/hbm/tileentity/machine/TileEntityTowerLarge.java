package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Large cooling tower, a 9x9 condenser 13 blocks tall with three ports on each side, the steam rises from inside.
 * TODO config (IConfigurableMachine), ClientConfig.COOLING_TOWER_PARTICLES
 */
public class TileEntityTowerLarge extends TileEntityCondenser {

	//Configurable values
	public static int inputTankSizeTL = 10_000;
	public static int outputTankSizeTL = 10_000;

	public TileEntityTowerLarge(BlockPos pos, BlockState state) {
		super(ModTileEntities.TOWER_LARGE.get(), pos, state);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.SPENTSTEAM, inputTankSizeTL);
		tanks[1] = new FluidTank(Fluids.WATER, outputTankSizeTL);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(!isServer()) {

			if(this.waterTimer > 0 && this.level.getGameTime() % 4 == 0) {
				CompoundTag data = new CompoundTag();
				data.putString("type", "tower");
				data.putFloat("lift", 0.5F);
				data.putFloat("base", 1F);
				data.putFloat("max", 10F);
				data.putInt("life", 750 + level.random.nextInt(250));

				data.putDouble("posX", worldPosition.getX() + 0.5 + level.random.nextDouble() * 3 - 1.5);
				data.putDouble("posZ", worldPosition.getZ() + 0.5 + level.random.nextDouble() * 3 - 1.5);
				data.putDouble("posY", worldPosition.getY() + 1);

				com.hbm.particle.ParticleEffectsNT.effectNT(data);
			}
		}
	}

	/** Ports 5 blocks out on each side, in the middle and 3 blocks to either side of it */
	@Override
	protected void subscribeToAllAround() {
		for(Direction dir : Direction.Plane.HORIZONTAL) {
			Direction rot = dir.getClockWise();
			BlockPos side = worldPosition.relative(dir, 5);
			this.trySubscribe(this.tanks[0].getTankType(), level, side, dir);
			this.trySubscribe(this.tanks[0].getTankType(), level, side.relative(rot, 3), dir);
			this.trySubscribe(this.tanks[0].getTankType(), level, side.relative(rot, -3), dir);
		}
	}

	@Override
	protected void sendFluidToAll() {
		for(Direction dir : Direction.Plane.HORIZONTAL) {
			Direction rot = dir.getClockWise();
			BlockPos side = worldPosition.relative(dir, 5);
			this.tryProvide(this.tanks[1], level, side, dir);
			this.tryProvide(this.tanks[1], level, side.relative(rot, 3), dir);
			this.tryProvide(this.tanks[1], level, side.relative(rot, -3), dir);
		}
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 4, worldPosition.getY(), worldPosition.getZ() - 4, worldPosition.getX() + 5, worldPosition.getY() + 13, worldPosition.getZ() + 5);
	}
}
