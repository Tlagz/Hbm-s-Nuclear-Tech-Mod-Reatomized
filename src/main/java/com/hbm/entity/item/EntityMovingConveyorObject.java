package com.hbm.entity.item;

import java.util.List;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Something riding on conveyor belts: the server moves it along the belt it's on, the client interpolates the
 * position updates. Entering an IEnterableBlock (sideways, or falling into one) hands it over.
 */
public abstract class EntityMovingConveyorObject extends Entity {

	/** The original's ServerConfig.CONVEYOR_CRAM_MAX and CONVEYOR_CRAM_EXPLODE defaults */
	public static int cramMax = 25;
	public static boolean cramExplode = true;

	protected int turnProgress;
	protected double syncPosX;
	protected double syncPosY;
	protected double syncPosZ;
	protected Vec3 motion = Vec3.ZERO;

	public EntityMovingConveyorObject(EntityType<?> type, Level world) {
		super(type, world);
		this.noPhysics = true;
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean isAttackable() {
		return true;
	}

	/** Players punching it take it off the belt */
	@Override
	public boolean skipAttackInteraction(Entity attacker) {
		if(attacker instanceof Player) {
			this.discard();
		}
		return true;
	}

	@Override
	public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
		this.syncPosX = x;
		this.syncPosY = y;
		this.syncPosZ = z;
		this.turnProgress = steps + 2; //use 4-ply for extra smoothness
	}

	@Override
	public void tick() {

		if(level().isClientSide) {
			if(this.turnProgress > 0) {
				double interpX = this.getX() + (this.syncPosX - this.getX()) / (double) this.turnProgress;
				double interpY = this.getY() + (this.syncPosY - this.getY()) / (double) this.turnProgress;
				double interpZ = this.getZ() + (this.syncPosZ - this.getZ()) / (double) this.turnProgress;
				--this.turnProgress;
				this.setPos(interpX, interpY, interpZ);
			}
			return;
		}

		tickCount++;

		if(this.tickCount <= 5) {
			return;
		}

		// cram check every 20s
		if((tickCount + this.getId()) % 400 == 0) {
			List<EntityMovingConveyorObject> objs = level().getEntitiesOfClass(EntityMovingConveyorObject.class, this.getBoundingBox().inflate(0.125));
			if(objs.size() >= cramMax) {
				for(EntityMovingConveyorObject obj : objs) obj.discard();
				level().explode(this, getX(), getY() + 0.125, getZ(), 1, Level.ExplosionInteraction.NONE);
				BlockPos here = this.blockPosition();

				if(level().getBlockState(here).getBlock() instanceof IConveyorBelt && this.tickCount > 400 && cramExplode)
					level().destroyBlock(here, false);
			}
		}

		BlockPos blockPos = BlockPos.containing(getX(), getY(), getZ());
		Block b = level().getBlockState(blockPos).getBlock();
		boolean isOnConveyor = b instanceof IConveyorBelt belt && belt.canItemStay(level(), blockPos, position());

		if(!isOnConveyor) {

			if(onLeaveConveyor()) {
				return;
			}
		} else {

			Vec3 target = ((IConveyorBelt) b).getTravelLocation(level(), blockPos, position(), getMoveSpeed());
			this.motion = target.subtract(position());
		}

		BlockPos lastPos = BlockPos.containing(getX(), getY(), getZ());
		this.setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
		BlockPos newPos = BlockPos.containing(getX(), getY(), getZ());

		if(!lastPos.equals(newPos)) {

			BlockState newState = level().getBlockState(newPos);

			if(newState.getBlock() instanceof IEnterableBlock enterable) {

				Direction dir = null;

				if(lastPos.getX() > newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() == newPos.getZ()) dir = Direction.EAST;
				else if(lastPos.getX() < newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() == newPos.getZ()) dir = Direction.WEST;
				else if(lastPos.getX() == newPos.getX() && lastPos.getY() > newPos.getY() && lastPos.getZ() == newPos.getZ()) dir = Direction.UP;
				else if(lastPos.getX() == newPos.getX() && lastPos.getY() < newPos.getY() && lastPos.getZ() == newPos.getZ()) dir = Direction.DOWN;
				else if(lastPos.getX() == newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() > newPos.getZ()) dir = Direction.SOUTH;
				else if(lastPos.getX() == newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() < newPos.getZ()) dir = Direction.NORTH;

				enterBlock(enterable, newPos, dir);

			} else {

				if(newState.isAir() || !newState.isSolid()) {

					BlockState below = level().getBlockState(newPos.below());

					if(below.getBlock() instanceof IEnterableBlock enterable) {
						enterBlockFalling(enterable, newPos);
					}
				}
			}
		}
	}

	/** dir is the side of the block it enters from, null for a diagonal move (the original's UNKNOWN) */
	public abstract void enterBlock(IEnterableBlock enterable, BlockPos pos, Direction dir);

	public void enterBlockFalling(IEnterableBlock enterable, BlockPos pos) {
		this.enterBlock(enterable, pos.below(), Direction.UP);
	}

	/** @return true if the update loop should end */
	public abstract boolean onLeaveConveyor();

	public double getMoveSpeed() {
		return 0.0625D;
	}

	public Vec3 getMotion() {
		return motion;
	}
}
