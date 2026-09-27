package com.hbm.entity.item;

import com.hbm.entity.ModEntities;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** A stack riding on a conveyor. Right-click picks it up, hitting it knocks it off the belt. */
public class EntityMovingItem extends EntityMovingConveyorObject implements IConveyorItem {

	private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(EntityMovingItem.class, EntityDataSerializers.ITEM_STACK);

	public EntityMovingItem(EntityType<? extends EntityMovingItem> type, Level world) {
		super(type, world);
	}

	public EntityMovingItem(Level world) {
		this(ModEntities.MOVING_ITEM.get(), world);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ITEM, ItemStack.EMPTY);
	}

	public void setItemStack(ItemStack stack) {
		this.entityData.set(ITEM, stack);
	}

	@Override
	public ItemStack getItemStack() {
		ItemStack stack = this.entityData.get(ITEM);
		return stack.isEmpty() ? new ItemStack(Blocks.STONE) : stack;
	}

	/** Adds the item to the player's inventory */
	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {

		if(!level().isClientSide && this.isAlive() && player.getInventory().add(this.getItemStack().copy())) {
			player.containerMenu.broadcastChanges();
			this.discard();
		}

		return InteractionResult.sidedSuccess(level().isClientSide);
	}

	/** Knocks the item off the belt */
	@Override
	public boolean skipAttackInteraction(Entity attacker) {

		if(!level().isClientSide && this.isAlive()) {
			this.discard();
			level().addFreshEntity(new ItemEntity(level(), getX(), getY(), getZ(), this.getItemStack()));
		}
		return true;
	}

	/** Ensures the item is knocked off the belt due to non-player attacks (explosions, etc) */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		this.skipAttackInteraction(source.getEntity());
		return true;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {

		ItemStack stack = ItemStack.parseOptional(registryAccess(), nbt.getCompound("Item"));
		this.setItemStack(stack);

		if(stack.isEmpty())
			this.discard();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		ItemStack stack = this.entityData.get(ITEM);
		if(!stack.isEmpty())
			nbt.put("Item", stack.save(registryAccess()));
	}

	@Override
	public void enterBlock(IEnterableBlock enterable, BlockPos pos, Direction dir) {
		if(!this.isAlive()) return;

		if(enterable.canItemEnter(level(), pos, dir, this)) {
			enterable.onItemEnter(level(), pos, dir, this);
			this.discard();
		}
	}

	@Override
	public boolean onLeaveConveyor() {

		if(!this.isAlive()) return true;

		this.discard();
		ItemEntity item = new ItemEntity(level(), getX() + motion.x * 2, getY() + motion.y * 2, getZ() + motion.z * 2, this.getItemStack());
		item.lifespan = 60 * 20;
		item.setDeltaMovement(motion.x * 2, 0.1, motion.z * 2);
		level().addFreshEntity(item);

		return true;
	}
}
