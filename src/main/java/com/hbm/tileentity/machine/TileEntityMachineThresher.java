package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.blocks.machine.MachineThresher;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Thresher: a fuel-burning arm that periodically folds out in front of the machine and sweeps a seven block wide row,
 * harvesting and re-planting mature crops, trimming cane and cacti and dropping everything behind the machine.
 * Monsters caught by the wheel drop small nitra piles.
 *
 * TODO NTM tall plants (hemp etc.) once BlockTallPlant exists, IFluidCopiable
 */
public class TileEntityMachineThresher extends TileEntityLoadedBase implements IFluidStandardReceiverMK2 {

	public FluidTank tank;

	public boolean isOn;
	public boolean isSuspended;
	public int delay;

	private int turnProgress;
	public float syncAngle;
	public float angle;
	public float prevAngle;

	// 0: waiting, 1: extending, 2: retracting
	private int state = 0;

	public float spin;
	public float lastSpin;
	private AudioWrapper audio;

	public TileEntityMachineThresher(BlockPos pos, BlockState state) {
		super(ModTileEntities.THRESHER.get(), pos, state);
		this.tank = new FluidTank(Fluids.WOODOIL, 100);
	}

	/** The block faces where the arm reaches out to; the original's dir is the opposite of that */
	private Direction getFacing() {
		return getBlockState().getValue(MachineThresher.FACING);
	}

	@Override
	public void updateEntity() {

		int xCoord = worldPosition.getX();
		int yCoord = worldPosition.getY();
		int zCoord = worldPosition.getZ();
		Direction dir = getFacing().getOpposite();
		Direction rot = dir.getClockWise();

		if(!level.isClientSide) {

			if(!isSuspended && level.getGameTime() % 20 == 0) {
				if(tank.getFill() > 0) {
					tank.setFill(tank.getFill() - 1);
					this.isOn = true;
				} else {
					this.isOn = false;
				}

				trySubscribe(tank.getTankType(), level, worldPosition.relative(rot), rot);
				trySubscribe(tank.getTankType(), level, worldPosition.relative(rot.getOpposite()), rot.getOpposite());
				trySubscribe(tank.getTankType(), level, worldPosition.below(), Direction.DOWN);
			}

			if(isOn && !isSuspended) {

				if(this.state == 0) {
					this.delay--;
					if(delay <= 0) this.state = 1;
				}

				if(this.state == 1) {
					this.angle += 82.5F / 60F;

					if(this.angle >= 82.5F) {
						this.angle = 82.5F;
						this.state = 2;
					}
				} else if(this.state == 2) {
					this.angle -= 82.5F / 60F;

					if(this.angle <= 0F) {
						this.angle = 0F;
						this.state = 0;
						this.delay = 200 + level.random.nextInt(100);
					}
				}

				if(this.angle != 0) {
					// the two arm segments fold like the original's rotated vectors, only their horizontal length matters
					double reach = 8 * Math.cos(Math.toRadians(82.5 - angle)) + 2;
					double endX = xCoord + 0.5 - dir.getStepX() * (1 + reach);
					double endZ = zCoord + 0.5 - dir.getStepZ() * (1 + reach);

					for(int i = -3; i <= 3; i++) {
						BlockPos hit = new BlockPos(Mth.floor(endX + rot.getStepX() * i), yCoord, Mth.floor(endZ + rot.getStepZ() * i));
						BlockState b = level.getBlockState(hit);

						if(b.isRedstoneConductor(level, hit) && !canCut(b)) {
							this.state = 2;
							break;
						}

						if(b.getBlock() instanceof DoublePlantBlock) {
							if(b.is(Blocks.SUNFLOWER) && level.random.nextInt(250) == 0) {
								level.levelEvent(2001, hit, Block.getId(b));
								this.dropItem(new ItemStack(Blocks.SUNFLOWER));
							}
							if(b.is(Blocks.TALL_GRASS) && level.random.nextInt(100) == 0) {
								level.levelEvent(2001, hit, Block.getId(b));
								this.dropItem(new ItemStack(Items.WHEAT_SEEDS));
							}
							continue;
						}

						if(b.is(Blocks.SUGAR_CANE) || b.is(Blocks.CACTUS)) {
							this.cutCane(b.getBlock(), hit);
							continue;
						}
						// BonemealableBlock also covers anything that accepts bone
						// meal, so we have to handle actual crops last
						if(canCut(b) && !shouldIgnore(hit, b)) this.cutCrop(b, hit);
					}

					double ex = Math.abs(dir.getStepX() * 0.5) + Math.abs(rot.getStepX() * 4.5);
					double ez = Math.abs(dir.getStepZ() * 0.5) + Math.abs(rot.getStepZ() * 4.5);
					List<LivingEntity> affected = level.getEntitiesOfClass(LivingEntity.class, new AABB(endX - ex, yCoord, endZ - ez, endX + ex, yCoord + 1, endZ + ez));

					for(LivingEntity e : affected) {
						if(e.isAlive() && e.hurt(ModDamageSource.source(level, ModDamageSource.TURBOFAN), 100)) {
							if(e instanceof Enemy && !e.isAlive()) this.dropItem(new ItemStack(ModItems.nitra_small.get()));
							level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 2.0F, 0.95F + level.random.nextFloat() * 0.2F);
							int count = Math.min((int) Math.ceil(e.getMaxHealth() / 4), 250);
							if(level instanceof ServerLevel server) {
								server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()), e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ(), count * 4, e.getBbWidth() * 0.5, e.getBbHeight() * 0.5, e.getBbWidth() * 0.5, 0.1D);
							}
						}
					}
				}
			}

			networkPackNT(100);

		} else {

			this.lastSpin = this.spin;

			if(isOn && !isSuspended) {
				if(this.angle > 0) this.spin += 15F;

				level.addParticle(ParticleTypes.SMOKE, xCoord + 0.5 + dir.getStepX() * 0.8125 + rot.getStepX() * 0.375, yCoord + 1.5625, zCoord + 0.5 + dir.getStepZ() * 0.8125 + rot.getStepZ() * 0.375, 0, 0, 0);

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.keepAlive();
				audio.updateVolume(this.getVolume(1F));

			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}

			if(this.spin >= 360F) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}

			this.prevAngle = this.angle;

			if(this.turnProgress > 0) {
				double d0 = Mth.wrapDegrees(this.syncAngle - (double) this.angle);
				this.angle = (float) ((double) this.angle + d0 / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.angle = this.syncAngle;
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.engine", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1.0F, 10F, 1.0F + level.random.nextFloat() * 0.1F, 10);
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

	public static boolean canCut(BlockState b) {
		if(b.getBlock() instanceof BonemealableBlock) return true;
		if(b.is(Blocks.NETHER_WART)) return true;
		if(b.is(Blocks.MELON) || b.is(Blocks.PUMPKIN)) return true;
		return false;
	}

	/** Stems stay, and anything that can still grow isn't ripe yet */
	public boolean shouldIgnore(BlockPos pos, BlockState b) {

		if(b.getBlock() instanceof StemBlock || b.getBlock() instanceof AttachedStemBlock) return true;
		if(b.is(Blocks.NETHER_WART)) return b.getValue(NetherWartBlock.AGE) < 3;

		if(b.getBlock() instanceof BonemealableBlock growable) {
			return growable.isValidBonemealTarget(level, pos, b);
		}

		return false;
	}

	/** Removes the top two blocks from a three block crop like cacti and sugar cane */
	protected void cutCane(Block target, BlockPos pos) {

		// people may be inclined to incorrectly place this thing one block above
		// the intended operating level, so we compensate for that
		int offset = level.getBlockState(pos.below()).is(target) ? -1 : 0;

		// top to bottom, only the cane itself (the original cleared whatever was there)
		for(int i = 2 + offset; i > 0 + offset; i--) {
			BlockPos p = pos.above(i);
			BlockState b = level.getBlockState(p);
			if(!b.is(target)) continue;
			level.levelEvent(2001, p, Block.getId(b));
			for(ItemStack drop : Block.getDrops(b, (ServerLevel) level, p, null)) dropItem(drop);
			level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
		}
	}

	/** Harvests and re-plants crops like wheat */
	protected void cutCrop(BlockState b, BlockPos pos) {

		level.levelEvent(2001, pos, Block.getId(b));

		BlockState replacement = Blocks.AIR.defaultBlockState();
		boolean replanted = false;

		for(ItemStack drop : Block.getDrops(b, (ServerLevel) level, pos, level.getBlockEntity(pos))) {
			if(!replanted && drop.getItem() instanceof BlockItem seed && seed.getBlock() instanceof BushBlock) {
				BlockState plant = seed.getBlock().defaultBlockState();

				if(plant.canSurvive(level, pos)) {
					replacement = plant;
					replanted = true;
					drop.shrink(1);
				}
			}

			if(!drop.isEmpty()) dropItem(drop);
		}

		// Apparently, until 1.14 full-grown wheat could sometimes drop no seeds at all
		// This is a quick and dirty workaround for that.
		if(b.is(Blocks.WHEAT) && !replanted) {
			replacement = Blocks.WHEAT.defaultBlockState();
		}

		level.setBlock(pos, replacement, 3);
	}

	/** Items come out of the back of the machine */
	protected void dropItem(ItemStack drop) {

		Direction dir = getFacing();
		double spawnX = worldPosition.getX() + 0.5 - dir.getStepX() * 0.75;
		double spawnZ = worldPosition.getZ() + 0.5 - dir.getStepZ() * 0.75;

		ItemEntity entityItem = new ItemEntity(level, spawnX, worldPosition.getY(), spawnZ, drop);
		entityItem.setPickUpDelay(10);
		entityItem.setDeltaMovement(dir.getStepX() * -0.2 + 0.2, entityItem.getDeltaMovement().y, dir.getStepZ() * -0.2);
		level.addFreshEntity(entityItem);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.isOn);
		buf.writeBoolean(this.isSuspended);
		buf.writeFloat(this.angle);
		this.tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.isOn = buf.readBoolean();
		this.isSuspended = buf.readBoolean();
		this.syncAngle = buf.readFloat();
		this.turnProgress = 3; //use 3-ply for extra smoothness
		this.tank.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.isOn = nbt.getBoolean("isOn");
		this.isSuspended = nbt.getBoolean("isSuspended");
		this.angle = nbt.getFloat("angle");
		this.state = nbt.getInt("state");
		this.tank.readFromNBT(nbt, "t");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("isOn", this.isOn);
		nbt.putBoolean("isSuspended", this.isSuspended);
		nbt.putFloat("angle", this.angle);
		nbt.putInt("state", this.state);
		tank.writeToNBT(nbt, "t");
	}

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tank}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tank}; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 10, worldPosition.getY(), worldPosition.getZ() - 10, worldPosition.getX() + 11, worldPosition.getY() + 7, worldPosition.getZ() + 11);
	}
}
