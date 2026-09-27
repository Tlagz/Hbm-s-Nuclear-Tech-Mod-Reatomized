package com.hbm.tileentity.machine;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Automatic buzz saw: a fuel-burning arm sweeping around, cutting plants and felling whole trees (re-planting their
 * saplings). Anything alive caught by the blade gets shredded.
 *
 * The original's materials map to tags: wood -> logs, leaves -> leaves, plants -> bushes (flowers, grass, crops).
 * Saplings don't count as plants here, otherwise the saw would cut down the ones it just planted.
 * TODO shouldIgnore for immature willows once the tall plants exist, IFluidCopiable
 */
public class TileEntityMachineAutosaw extends TileEntityLoadedBase implements IFluidStandardReceiverMK2 {

	private static final int MIN_DIST = 2;
	private static final int MAX_DIST = 9;

	private static final int FELL_HORIZONTAL_RANGE = 10;
	private static final int FELL_BFS_RADIUS = MAX_DIST + FELL_HORIZONTAL_RANGE;
	private static final int FELL_VERTICAL_RANGE = 32;
	private static final int FELL_MAX_BASE_DEPTH = FELL_VERTICAL_RANGE / 2;

	// 18-connectivity: 6 face-adjacent + 12 edge-adjacent (exactly one coord diff is 0)
	private static final int[][] EIGHTEEN_DIRS = {
		{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
		{1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
		{1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
		{0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1}
	};

	public static boolean isAcceptedFuel(FluidType type) {
		return type == Fluids.WOODOIL || type == Fluids.ETHANOL || type == Fluids.FISHOIL || type == Fluids.HEAVYOIL || type == Fluids.COALCREOSOTE;
	}

	public FluidTank tank;

	public boolean isOn;
	public boolean isSuspended;
	private int forceSkip;
	public float syncYaw;
	public float rotationYaw;
	public float prevRotationYaw;
	public float syncPitch;
	public float rotationPitch;
	public float prevRotationPitch;

	// 0: searching, 1: extending, 2: retracting
	private int state = 0;

	private int turnProgress;

	public float spin;
	public float lastSpin;
	private AudioWrapper audio;

	public TileEntityMachineAutosaw(BlockPos pos, BlockState state) {
		super(ModTileEntities.AUTOSAW.get(), pos, state);
		this.tank = new FluidTank(Fluids.WOODOIL, 100);
	}

	public static boolean isWood(BlockState state) { return state.is(BlockTags.LOGS); }
	public static boolean isLeaves(BlockState state) { return state.is(BlockTags.LEAVES); }
	public static boolean isPlant(BlockState state) { return state.getBlock() instanceof BushBlock && !(state.getBlock() instanceof SaplingBlock); }

	@Override
	public void updateEntity() {

		int xCoord = worldPosition.getX();
		int yCoord = worldPosition.getY();
		int zCoord = worldPosition.getZ();

		if(!level.isClientSide) {

			if(!isSuspended && level.getGameTime() % 20 == 0) {
				if(tank.getFill() > 0) {
					tank.setFill(tank.getFill() - 1);
					this.isOn = true;
				} else {
					this.isOn = false;
				}

				for(Direction dir : Direction.values()) {
					if(dir != Direction.UP) trySubscribe(tank.getTankType(), level, worldPosition.relative(dir), dir);
				}
			}

			if(isOn && !isSuspended) {
				Vec3 tip = getBladePos();
				double cX = tip.x;
				double cY = tip.y;
				double cZ = tip.z;

				List<LivingEntity> affected = level.getEntitiesOfClass(LivingEntity.class, new AABB(cX - 1, cY - 0.25, cZ - 1, cX + 1, cY + 0.25, cZ + 1));

				for(LivingEntity e : affected) {
					if(e.isAlive() && e.hurt(ModDamageSource.source(level, ModDamageSource.TURBOFAN), 100)) {
						level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 2.0F, 0.95F + level.random.nextFloat() * 0.2F);
						int count = Math.min((int) Math.ceil(e.getMaxHealth() / 4), 250);
						if(level instanceof ServerLevel server) {
							server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()), e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ(), count * 4, e.getBbWidth() * 0.5, e.getBbHeight() * 0.5, e.getBbWidth() * 0.5, 0.1D);
						}
					}
				}

				if(state == 0) {

					this.rotationYaw += 1;

					if(this.rotationYaw >= 360) {
						this.rotationYaw -= 360;
					}

					if(forceSkip > 0) {
						forceSkip--;
					} else {
						final double CUT_ANGLE = Math.toRadians(5);
						double rotationYawRads = Math.toRadians((rotationYaw + 270) % 360);

						outer:
						for(int dx = -MAX_DIST; dx <= MAX_DIST; dx++) {
							for(int dz = -MAX_DIST; dz <= MAX_DIST; dz++) {
								int sqrDst = dx * dx + dz * dz;

								if(sqrDst <= MIN_DIST * MIN_DIST || sqrDst > MAX_DIST * MAX_DIST)
									continue;

								double angle = Math.atan2(dz, dx);
								double relAngle = Math.abs(angle - rotationYawRads);
								relAngle = Math.abs((relAngle + Math.PI) % (2 * Math.PI) - Math.PI);

								if(relAngle > CUT_ANGLE) continue;

								BlockState b = level.getBlockState(new BlockPos(xCoord + dx, yCoord + 1, zCoord + dz));
								if(!(isWood(b) || isLeaves(b) || isPlant(b))) continue;

								state = 1;
								break outer;
							}
						}
					}
				}

				int hitY = Mth.floor(cY);
				int hitX0 = Mth.floor(cX - 0.5);
				int hitZ0 = Mth.floor(cZ - 0.5);
				int hitX1 = Mth.floor(cX + 0.5);
				int hitZ1 = Mth.floor(cZ + 0.5);

				this.tryInteract(new BlockPos(hitX0, hitY, hitZ0));
				this.tryInteract(new BlockPos(hitX1, hitY, hitZ0));
				this.tryInteract(new BlockPos(hitX0, hitY, hitZ1));
				this.tryInteract(new BlockPos(hitX1, hitY, hitZ1));

				if(state == 1) {
					this.rotationPitch += 2;

					if(this.rotationPitch > 80) {
						this.rotationPitch = 80;
						state = 2;
					}
				}

				if(state == 2) {
					this.rotationPitch -= 2;

					if(this.rotationPitch <= 0) {
						this.rotationPitch = 0;
						state = 0;
					}
				}
			}

			networkPackNT(100);
		} else {

			this.lastSpin = this.spin;

			if(isOn && !isSuspended) {
				this.spin += 15F;

				Vec3 vec = new Vec3(0.625, 0, 1.625).yRot(-(float) Math.toRadians(rotationYaw));
				level.addParticle(ParticleTypes.SMOKE, xCoord + 0.5 + vec.x, yCoord + 2.0625, zCoord + 0.5 + vec.z, 0, 0, 0);

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

			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;

			if(this.turnProgress > 0) {
				double d0 = Mth.wrapDegrees(this.syncYaw - (double) this.rotationYaw);
				double d1 = Mth.wrapDegrees(this.syncPitch - (double) this.rotationPitch);
				this.rotationYaw = (float) ((double) this.rotationYaw + d0 / (double) this.turnProgress);
				this.rotationPitch = (float) ((double) this.rotationPitch + d1 / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.rotationYaw = this.syncYaw;
				this.rotationPitch = this.syncPitch;
			}
		}
	}

	/** Where the sawblade is, from the arm's yaw and pitch; only the horizontal position moves */
	public Vec3 getBladePos() {
		Vec3 pivot = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 1.75, worldPosition.getZ() + 0.5);
		Vec3 upperArm = new Vec3(0, 0, -4).xRot((float) Math.toRadians(80 - rotationPitch)).yRot(-(float) Math.toRadians(rotationYaw));
		Vec3 lowerArm = new Vec3(0, 0, -4).xRot((float) -Math.toRadians(80 - rotationPitch)).yRot(-(float) Math.toRadians(rotationYaw));
		Vec3 armTip = new Vec3(0, 0, -2).yRot(-(float) Math.toRadians(rotationYaw));
		return new Vec3(pivot.x + upperArm.x + lowerArm.x + armTip.x, pivot.y, pivot.z + upperArm.z + lowerArm.z + armTip.z);
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

	protected void tryInteract(BlockPos pos) {

		BlockState b = level.getBlockState(pos);

		if(isLeaves(b) || isPlant(b)) {
			level.destroyBlock(pos, true);
		} else if(isWood(b)) {
			fellTree(pos);
			if(state == 1) {
				state = 2;
			}
		}

		// Return when hitting a wall
		if(state == 1 && level.getBlockState(pos).isRedstoneConductor(level, pos)) {
			state = 2;
			forceSkip = 5;
		}
	}

	protected void fellTree(BlockPos hit) {

		int xCoord = worldPosition.getX();
		int zCoord = worldPosition.getZ();
		int sawY = hit.getY();
		BlockPos hitCol = new BlockPos(hit.getX(), -1, hit.getZ());

		// Step A: Scan working area for trunks (column -> trunk base pos)
		HashMap<BlockPos, BlockPos> trunks = new HashMap<>();

		for(int dx = -MAX_DIST; dx <= MAX_DIST; dx++) {
			for(int dz = -MAX_DIST; dz <= MAX_DIST; dz++) {
				if(dx * dx + dz * dz > MAX_DIST * MAX_DIST) {
					continue;
				}

				int colX = xCoord + dx;
				int colZ = zCoord + dz;

				if(!isWood(level.getBlockState(new BlockPos(colX, sawY, colZ)))) {
					continue;
				}

				int baseY = sawY;
				while(sawY - baseY < FELL_MAX_BASE_DEPTH && isWood(level.getBlockState(new BlockPos(colX, baseY - 1, colZ)))) {
					baseY--;
				}

				BlockPos base = new BlockPos(colX, baseY, colZ);
				if(!canSupportSapling(base, level.getBlockState(base))) {
					continue;
				}

				trunks.put(new BlockPos(colX, -1, colZ), base);
			}
		}

		// Always include the hit position's trunk
		if(!trunks.containsKey(hitCol)) {
			int baseY = hit.getY();
			while(sawY - baseY < FELL_MAX_BASE_DEPTH && isWood(level.getBlockState(new BlockPos(hit.getX(), baseY - 1, hit.getZ())))) {
				baseY--;
			}
			trunks.put(hitCol, new BlockPos(hit.getX(), baseY, hit.getZ()));
		}

		// Step B: 0-1 BFS from all trunks
		// Vertical neighbors (same column) have distance 0, horizontal neighbors have distance 1
		// blockOwner: block pos -> column of owning trunk
		HashMap<BlockPos, BlockPos> blockOwner = new HashMap<>();
		ArrayDeque<BlockPos[]> deque = new ArrayDeque<>();
		int hitColCount = 1;

		int minY = Math.max(level.getMinBuildHeight(), sawY - FELL_MAX_BASE_DEPTH);
		int maxY = Math.min(level.getMaxBuildHeight() - 1, sawY + FELL_VERTICAL_RANGE);

		for(Map.Entry<BlockPos, BlockPos> trunk : trunks.entrySet()) {
			deque.addFirst(new BlockPos[] {trunk.getValue(), trunk.getKey()});
		}

		while(!deque.isEmpty()) {
			BlockPos[] pair = deque.pollFirst();
			BlockPos current = pair[0];
			BlockPos currentCol = pair[1];

			if(blockOwner.containsKey(current)) {
				if(currentCol.equals(hitCol)) {
					hitColCount--;
					if(hitColCount == 0) {
						break;
					}
				}
				continue;
			}
			blockOwner.put(current, currentCol);

			for(int[] dir : EIGHTEEN_DIRS) {
				int neighborX = current.getX() + dir[0];
				int neighborY = current.getY() + dir[1];
				int neighborZ = current.getZ() + dir[2];

				// Bounds check: radius FELL_BFS_RADIUS horizontal, minY to maxY vertical
				int neighborDx = neighborX - xCoord;
				int neighborDz = neighborZ - zCoord;
				if(neighborDx * neighborDx + neighborDz * neighborDz > FELL_BFS_RADIUS * FELL_BFS_RADIUS) {
					continue;
				}
				if(neighborY < minY || neighborY > maxY) {
					continue;
				}

				BlockPos neighborPos = new BlockPos(neighborX, neighborY, neighborZ);
				if(blockOwner.containsKey(neighborPos)) {
					continue;
				}

				BlockState b = level.getBlockState(neighborPos);
				if(!isWood(b) && !isLeaves(b)) {
					continue;
				}

				boolean hasHorizontal = dir[0] != 0 || dir[2] != 0;
				BlockPos[] entry = new BlockPos[] {neighborPos, currentCol};
				if(!hasHorizontal) {
					deque.addFirst(entry);
				} else {
					deque.addLast(entry);
				}
				if(currentCol.equals(hitCol)) {
					hitColCount++;
				}
			}

			if(currentCol.equals(hitCol)) {
				hitColCount--;
				if(hitColCount == 0) {
					break; // Early exit: all hit-tree blocks processed
				}
			}
		}

		// Step C: Cut blocks assigned to the hit trunk
		for(Map.Entry<BlockPos, BlockPos> entry : blockOwner.entrySet()) {
			if(!entry.getValue().equals(hitCol)) {
				continue;
			}

			BlockPos pos = entry.getKey();
			BlockState b = level.getBlockState(pos);

			// Replant sapling at positions within working area
			if(isWood(b) && isWithinWorkingArea(pos.getX(), pos.getZ()) && canSupportSapling(pos, b)) {
				BlockState sapling = getSapling(b);
				level.destroyBlock(pos, true);
				level.setBlock(pos, sapling, 3);
			} else {
				level.destroyBlock(pos, true);
			}
		}
	}

	private boolean isWithinWorkingArea(int x, int z) {
		int dx = x - worldPosition.getX();
		int dz = z - worldPosition.getZ();
		int distSq = dx * dx + dz * dz;
		return distSq > MIN_DIST * MIN_DIST && distSq <= MAX_DIST * MAX_DIST;
	}

	/** Whether the log's sapling could grow where the log is, i.e. the block below is dirt or similar */
	private boolean canSupportSapling(BlockPos pos, BlockState log) {
		return getSapling(log).canSurvive(level, pos);
	}

	/** The log's own sapling by name (birch_log -> birch_sapling), oak for everything else */
	public static BlockState getSapling(BlockState log) {
		ResourceLocation id = BuiltInRegistries.BLOCK.getKey(log.getBlock());
		String path = id.getPath().replace("stripped_", "");
		if(path.endsWith("_log") || path.endsWith("_wood")) {
			path = path.substring(0, path.lastIndexOf('_')) + "_sapling";
			Block sapling = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), path));
			if(sapling instanceof SaplingBlock) return sapling.defaultBlockState();
		}
		return Blocks.OAK_SAPLING.defaultBlockState();
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.isOn);
		buf.writeBoolean(this.isSuspended);
		buf.writeFloat(this.rotationYaw);
		buf.writeFloat(this.rotationPitch);
		this.tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.isOn = buf.readBoolean();
		this.isSuspended = buf.readBoolean();
		this.syncYaw = buf.readFloat();
		this.syncPitch = buf.readFloat();
		this.turnProgress = 3; //use 3-ply for extra smoothness
		this.tank.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.isOn = nbt.getBoolean("isOn");
		this.isSuspended = nbt.getBoolean("isSuspended");
		this.forceSkip = nbt.getInt("skip");
		this.rotationYaw = nbt.getFloat("yaw");
		this.rotationPitch = nbt.getFloat("pitch");
		this.state = nbt.getInt("state");
		this.tank.readFromNBT(nbt, "t");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("isOn", this.isOn);
		nbt.putBoolean("isSuspended", this.isSuspended);
		nbt.putInt("skip", this.forceSkip);
		nbt.putFloat("yaw", this.rotationYaw);
		nbt.putFloat("pitch", this.rotationPitch);
		nbt.putInt("state", this.state);
		tank.writeToNBT(nbt, "t");
	}

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tank}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tank}; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 12, worldPosition.getY(), worldPosition.getZ() - 12, worldPosition.getX() + 13, worldPosition.getY() + 10, worldPosition.getZ() + 13);
	}
}
