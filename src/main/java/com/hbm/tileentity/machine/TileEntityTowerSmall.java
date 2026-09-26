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
 * Auxiliary cooling tower, a 5x5 condenser 19 blocks tall with ports on its four sides, puffs steam from the top.
 * TODO config (IConfigurableMachine), ClientConfig.COOLING_TOWER_PARTICLES
 */
public class TileEntityTowerSmall extends TileEntityCondenser {

	//Configurable values
	public static int inputTankSizeTS = 1_000;
	public static int outputTankSizeTS = 1_000;

	public TileEntityTowerSmall(BlockPos pos, BlockState state) {
		super(ModTileEntities.TOWER_SMALL.get(), pos, state);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.SPENTSTEAM, inputTankSizeTS);
		tanks[1] = new FluidTank(Fluids.WATER, outputTankSizeTS);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(!isServer()) {

			if(this.waterTimer > 0 && this.level.getGameTime() % 2 == 0) {
				CompoundTag data = new CompoundTag();
				data.putString("type", "tower");
				data.putFloat("lift", 1F);
				data.putFloat("base", 0.5F);
				data.putFloat("max", 4F);
				data.putInt("life", 250 + level.random.nextInt(250));

				data.putDouble("posX", worldPosition.getX() + 0.5);
				data.putDouble("posZ", worldPosition.getZ() + 0.5);
				data.putDouble("posY", worldPosition.getY() + 18);

				com.hbm.particle.ParticleEffectsNT.effectNT(data);
			}
		}
	}

	@Override
	protected void subscribeToAllAround() {
		for(Direction dir : Direction.Plane.HORIZONTAL) this.trySubscribe(this.tanks[0].getTankType(), level, worldPosition.relative(dir, 3), dir);
	}

	@Override
	protected void sendFluidToAll() {
		for(Direction dir : Direction.Plane.HORIZONTAL) this.tryProvide(this.tanks[1], level, worldPosition.relative(dir, 3), dir);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 20, worldPosition.getZ() + 3);
	}
}
