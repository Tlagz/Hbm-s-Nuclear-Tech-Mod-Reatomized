package com.hbm.entity.projectile;

import com.hbm.entity.ModEntities;
import com.hbm.items.ModItems;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The gear an overspeeding Stirling engine throws out: it crushes whatever it hits, bounces off blocks with an
 * explosion while fast (breaking weak ones) and gets stuck once it's slow. Using it picks the gear back up.
 * Orientation 0-5 is the facing of the engine it came from, +6 while it's stuck.
 *
 * TODO the original's giblets effect when it kills something
 */
public class EntityCog extends ThrowableProjectile {

	private static final EntityDataAccessor<Integer> ORIENTATION = SynchedEntityData.defineId(EntityCog.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> META = SynchedEntityData.defineId(EntityCog.class, EntityDataSerializers.INT);

	private boolean inGround = false;

	public EntityCog(EntityType<? extends EntityCog> type, Level world) {
		super(type, world);
	}

	public EntityCog(Level world, double x, double y, double z) {
		super(ModEntities.COG.get(), x, y, z, world);
	}

	/** For the sawmill's sawblade, which flies and lands the same way */
	protected EntityCog(EntityType<? extends EntityCog> type, Level world, double x, double y, double z) {
		super(type, x, y, z, world);
	}

	/** What the player gets back when picking it up */
	protected ItemStack getDropItem() {
		return new ItemStack(getMeta() == 1 ? ModItems.gear_large_steel.get() : ModItems.gear_large.get());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ORIENTATION, 0);
		builder.define(META, 0);
	}

	public EntityCog setOrientation(int rot) {
		this.entityData.set(ORIENTATION, rot);
		return this;
	}

	/** 0 iron gear, 1 steel gear, 2 creative */
	public EntityCog setMeta(int meta) {
		this.entityData.set(META, meta);
		return this;
	}

	public int getOrientation() {
		return this.entityData.get(ORIENTATION);
	}

	public int getMeta() {
		return this.entityData.get(META);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if(!level().isClientSide) {
			if(player.getInventory().add(getDropItem())) this.discard();
		}
		return InteractionResult.sidedSuccess(level().isClientSide);
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		Entity e = result.getEntity();
		if(e.isAlive()) e.hurt(level().damageSources().fallingStalactite(this), 1000);
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		if(this.tickCount <= 1) return;

		int orientation = getOrientation();

		if(orientation < 6) {
			Vec3 motion = this.getDeltaMovement();
			if(motion.length() < 0.75) {
				orientation += 6;
				setOrientation(orientation);
			} else {
				Direction side = result.getDirection();
				this.setDeltaMovement(motion.x * (1 - Math.abs(side.getStepX()) * 2), motion.y * (1 - Math.abs(side.getStepY()) * 2), motion.z * (1 - Math.abs(side.getStepZ()) * 2));
				level().explode(this, getX(), getY(), getZ(), 3F, Level.ExplosionInteraction.NONE);
				if(level().getBlockState(result.getBlockPos()).getBlock().getExplosionResistance() < 50) {
					level().destroyBlock(result.getBlockPos(), false);
				}
			}
		}

		if(orientation >= 6) {
			this.setDeltaMovement(Vec3.ZERO);
			this.inGround = true;
		}
	}

	@Override
	public void tick() {
		if(!level().isClientSide) {
			int orientation = getOrientation();
			if(orientation >= 6 && !this.inGround) {
				setOrientation(orientation - 6);
			}
		}
		if(this.inGround) this.setDeltaMovement(Vec3.ZERO);
		super.tick();
	}

	@Override
	protected double getDefaultGravity() {
		return inGround ? 0 : 0.03D;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		return true;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putInt("rot", this.getOrientation());
		nbt.putInt("meta", this.getMeta());
		nbt.putBoolean("inGround", this.inGround);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		this.setOrientation(nbt.getInt("rot"));
		this.setMeta(nbt.getInt("meta"));
		this.inGround = nbt.getBoolean("inGround");
	}
}
