package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.entity.projectile.EntitySawblade;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;

import api.hbm.tile.IHeatSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Stirling sawmill: runs on heat from below (100-300 TU), saws logs into 1.5x the planks, planks into sticks, sticks
 * and saplings into sawdust/sticks, sometimes with sawdust on the side. Saws up anything standing next to the blade.
 * Above 300 TU the blade flies off after a while. Slots: 0 input, 1 output, 2 sawdust; no GUI, it's used by hand.
 */
public class TileEntitySawmill extends TileEntityMachineBase {

	public int heat;
	public static final double diffusion = 0.1D;
	private int warnCooldown = 0;
	private int overspeed = 0;
	public boolean hasBlade = true;

	public int progress = 0;
	public static final int processingTime = 600;

	public float spin;
	public float lastSpin;

	private static final TagKey<Item> WOODEN_RODS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse("c:rods/wooden"));

	public TileEntitySawmill(BlockPos pos, BlockState state) {
		super(ModTileEntities.SAWMILL.get(), pos, state, 3);
	}

	@Override
	public String getName() {
		return "container.sawmill";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(hasBlade) {
				tryPullHeat();

				if(warnCooldown > 0) warnCooldown--;

				if(heat >= 100) {

					ItemStack result = this.getOutput(slots.get(0));

					if(!result.isEmpty()) {
						progress += heat / 10;

						if(progress >= processingTime) {
							progress = 0;
							slots.set(0, ItemStack.EMPTY);
							slots.set(1, result);

							if(!result.is(ModItems.powder_sawdust.get())) {
								float chance = result.is(Items.STICK) ? 0.1F : 0.5F;
								if(level.random.nextFloat() < chance) {
									slots.set(2, new ItemStack(ModItems.powder_sawdust.get()));
								}
							}

							this.setChanged();
						}

					} else {
						this.progress = 0;
					}

					sawEntities();

				} else {
					this.progress = 0;
				}

				if(heat > 300) {

					this.overspeed++;

					if(overspeed > 60 && warnCooldown == 0) {
						warnCooldown = 100;
						level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, ModSounds.get("block.warnOverspeed"), SoundSource.BLOCKS, 2.0F, 1.0F);
					}

					if(overspeed > 300) {
						this.hasBlade = false;
						level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, 5F, Level.ExplosionInteraction.NONE);

						int orientation = BlockDummyable.getMeta(getBlockState()) - BlockDummyable.offset;
						Direction dir = Direction.from3DDataValue(orientation);
						EntitySawblade blade = new EntitySawblade(level, worldPosition.getX() + 0.5 + dir.getStepX(), worldPosition.getY() + 1, worldPosition.getZ() + 0.5 + dir.getStepZ());
						blade.setOrientation(orientation);
						Direction rot = dir.getCounterClockWise(); // ForgeDirection.getRotation(DOWN)
						blade.setDeltaMovement(rot.getStepX(), 1 + (heat - 100) * 0.0001D, rot.getStepZ());
						level.addFreshEntity(blade);

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

			this.heat = 0;

		} else {

			float momentum = heat * 25F / 300F;

			this.lastSpin = this.spin;
			this.spin += momentum;

			if(this.spin >= 360F) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}
		}
	}

	/** Everything in the blade's path gets sawed */
	private void sawEntities() {
		Direction rot = BlockDummyable.getRotation(getBlockState()).getClockWise(); // getRotation(UP)
		AABB aabb = BlockDummyable.rotateBox(new AABB(-1D, 0.375D, -1D, -0.875, 2.375D, 1D), rot).move(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5);

		for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
			if(e.isAlive() && e.hurt(ModDamageSource.source(level, ModDamageSource.TURBOFAN), 100)) {
				level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 2.0F, 0.95F + level.random.nextFloat() * 0.2F);
				int count = Math.min((int) Math.ceil(e.getMaxHealth() / 4), 250);
				if(level instanceof ServerLevel server) {
					server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()), e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ(), count * 4, 0, 0, 0, 0.1D);
				}
			}
		}
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
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(heat);
		buf.writeInt(progress);
		buf.writeBoolean(hasBlade);
		for(ItemStack slot : slots) BufferUtil.writeItemStack(buf, slot, level.registryAccess());
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.heat = buf.readInt();
		this.progress = buf.readInt();
		this.hasBlade = buf.readBoolean();
		for(int i = 0; i < slots.size(); i++) slots.set(i, BufferUtil.readItemStack(buf, level.registryAccess()));
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.hasBlade = !nbt.contains("hasBlade") || nbt.getBoolean("hasBlade");
		this.progress = nbt.getInt("progress");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("hasBlade", hasBlade);
		nbt.putInt("progress", progress);
	}

	/** A sawmill without its blade keeps that on the item (the NO_COG flag, same as the Stirling engine's gear) */
	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		if(!hasBlade) components.set(ModDataComponents.NO_COG.get(), true);
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		if(input.getOrDefault(ModDataComponents.NO_COG.get(), false)) this.hasBlade = false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return i == 0 && slots.get(0).isEmpty() && slots.get(1).isEmpty() && slots.get(2).isEmpty() && stack.getCount() == 1 && !getOutput(stack).isEmpty();
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i > 0;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] {0, 1, 2};
	}

	public ItemStack getOutput(ItemStack input) {

		if(input.isEmpty()) return ItemStack.EMPTY;

		if(input.is(WOODEN_RODS) || input.is(Items.STICK)) {
			return new ItemStack(ModItems.powder_sawdust.get());
		}

		if(input.is(ItemTags.LOGS) && level != null) {
			CraftingInput craft = CraftingInput.of(1, 1, List.of(input.copyWithCount(1)));
			ItemStack out = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craft, level).map(r -> r.value().assemble(craft, level.registryAccess())).orElse(ItemStack.EMPTY);
			if(!out.isEmpty()) {
				out = out.copy();
				out.setCount(out.getCount() * 6 / 4); //4 planks become 6
				return out;
			}
		}

		if(input.is(ItemTags.PLANKS)) {
			return new ItemStack(Items.STICK, 6);
		}

		if(input.is(ItemTags.SAPLINGS)) {
			return new ItemStack(Items.STICK, 1);
		}

		return ItemStack.EMPTY;
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 2, worldPosition.getZ() + 2);
	}
}
