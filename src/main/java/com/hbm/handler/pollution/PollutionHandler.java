package com.hbm.handler.pollution;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.hbm.config.MobConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.lib.RefStrings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * World pollution in 64x64 block areas. Stored as SavedData per dimension (data/hbmpollution.dat, same file
 * and format as the original, which saved it manually).
 *
 * TODO glyphid exceptions and rampant mode (glyphids not ported yet)
 */
@EventBusSubscriber(modid = RefStrings.MODID)
public class PollutionHandler {

	public static final String fileName = "hbmpollution";

	/** Baserate of soot generation for a furnace-equivalent machine per second */
	public static final float SOOT_PER_SECOND = 1F / 25F;
	/** Baserate of heavy metal generation, balanced around the soot values of combustion engines */
	public static final float HEAVY_METAL_PER_SECOND = 1F / 50F;
	/** Baserate for poison when spilled */
	public static final float POISON_PER_SECOND = 1F / 50F;

	///////////////////////
	/// UTILITY METHODS ///
	///////////////////////
	private static PollutionPerWorld getWorld(Level world) {
		if(!(world instanceof ServerLevel server)) return null;
		return server.getDataStorage().computeIfAbsent(PollutionPerWorld.FACTORY, fileName);
	}

	private static long key(BlockPos pos) {
		return ChunkPos.asLong(pos.getX() >> 6, pos.getZ() >> 6);
	}

	public static void incrementPollution(Level world, BlockPos pos, PollutionType type, float amount) {

		if(!RadiationConfig.enablePollution) return;

		PollutionPerWorld ppw = getWorld(world);
		if(ppw == null) return;
		PollutionData data = ppw.pollution.computeIfAbsent(key(pos), k -> new PollutionData());
		data.pollution[type.ordinal()] = Mth.clamp((float) (data.pollution[type.ordinal()] + amount * MobConfig.pollutionMult), 0F, 10_000F);
		ppw.setDirty();
	}

	public static void decrementPollution(Level world, BlockPos pos, PollutionType type, float amount) {
		incrementPollution(world, pos, type, -amount);
	}

	public static void setPollution(Level world, BlockPos pos, PollutionType type, float amount) {

		if(!RadiationConfig.enablePollution) return;

		PollutionPerWorld ppw = getWorld(world);
		if(ppw == null) return;
		PollutionData data = ppw.pollution.computeIfAbsent(key(pos), k -> new PollutionData());
		data.pollution[type.ordinal()] = amount;
		ppw.setDirty();
	}

	public static float getPollution(Level world, BlockPos pos, PollutionType type) {

		if(!RadiationConfig.enablePollution) return 0;

		PollutionPerWorld ppw = getWorld(world);
		if(ppw == null) return 0F;
		PollutionData data = ppw.pollution.get(key(pos));
		if(data == null) return 0F;
		return data.pollution[type.ordinal()];
	}

	public static PollutionData getPollutionData(Level world, BlockPos pos) {

		if(!RadiationConfig.enablePollution) return null;

		PollutionPerWorld ppw = getWorld(world);
		if(ppw == null) return null;
		return ppw.pollution.get(key(pos));
	}

	//////////////////////////
	/// SYSTEM UPDATE LOOP ///
	//////////////////////////
	private static int eggTimer = 0;

	@SubscribeEvent
	public static void updateSystem(ServerTickEvent.Post event) {

		if(!RadiationConfig.enablePollution) return;

		for(ServerLevel level : event.getServer().getAllLevels()) handleWorldDestruction(level);

		eggTimer++;
		if(eggTimer < 60) return;
		eggTimer = 0;

		for(ServerLevel level : event.getServer().getAllLevels()) {
			PollutionPerWorld ppw = getWorld(level);
			Map<Long, PollutionData> newPollution = new HashMap<>();

			for(Entry<Long, PollutionData> chunk : ppw.pollution.entrySet()) {
				int x = ChunkPos.getX(chunk.getKey());
				int z = ChunkPos.getZ(chunk.getKey());
				PollutionData data = chunk.getValue();

				float[] pollutionForNeightbors = new float[PollutionType.values().length];
				int S = PollutionType.SOOT.ordinal();
				int H = PollutionType.HEAVYMETAL.ordinal();
				int P = PollutionType.POISON.ordinal();

				/* CALCULATION */
				if(data.pollution[S] > 10) {
					pollutionForNeightbors[S] = data.pollution[S] * 0.05F;
					data.pollution[S] *= 0.8F;
				}

				data.pollution[S] *= 0.99F;
				data.pollution[H] *= 0.9995F;

				if(data.pollution[P] > 10) {
					pollutionForNeightbors[P] = data.pollution[P] * 0.025F;
					data.pollution[P] *= 0.9F;
				} else {
					data.pollution[P] *= 0.995F;
				}

				/* SPREADING */
				//apply new data to self
				PollutionData newData = newPollution.getOrDefault(chunk.getKey(), new PollutionData());

				boolean shouldPut = false;
				for(int i = 0; i < newData.pollution.length; i++) {
					newData.pollution[i] += data.pollution[i];
					if(newData.pollution[i] > 0) shouldPut = true;
				}
				if(shouldPut) newPollution.put(chunk.getKey(), newData);

				//apply neighbor data to neighboring chunks
				int[][] offsets = new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
				for(int[] offset : offsets) {
					long offPos = ChunkPos.asLong(x + offset[0], z + offset[1]);
					PollutionData offsetData = newPollution.getOrDefault(offPos, new PollutionData());

					shouldPut = false;
					for(int i = 0; i < offsetData.pollution.length; i++) {
						offsetData.pollution[i] += pollutionForNeightbors[i];
						if(offsetData.pollution[i] > 0) shouldPut = true;
					}
					if(shouldPut) newPollution.put(offPos, offsetData);
				}
			}

			if(!ppw.pollution.isEmpty() || !newPollution.isEmpty()) ppw.setDirty();
			ppw.pollution.clear();
			ppw.pollution.putAll(newPollution);
		}
	}

	protected static final float DESTRUCTION_THRESHOLD = 15F;
	protected static final int DESTRUCTION_COUNT = 5;

	protected static void handleWorldDestruction(ServerLevel world) {

		PollutionPerWorld ppw = getWorld(world);

		for(Entry<Long, PollutionData> pollution : ppw.pollution.entrySet()) {

			float poison = pollution.getValue().pollution[PollutionType.POISON.ordinal()];
			if(poison < DESTRUCTION_THRESHOLD) continue;

			int cx = ChunkPos.getX(pollution.getKey());
			int cz = ChunkPos.getZ(pollution.getKey());

			for(int i = 0; i < DESTRUCTION_COUNT; i++) {
				int x = (cx << 6) + world.random.nextInt(64);
				int z = (cz << 6) + world.random.nextInt(64);

				if(world.getChunkSource().hasChunk(x >> 4, z >> 4)) {
					int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1 - world.random.nextInt(3) + 1;
					BlockPos pos = new BlockPos(x, y, z);
					BlockState state = world.getBlockState(pos);

					if(state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
						world.setBlock(pos, Blocks.COARSE_DIRT.defaultBlockState(), 3);
					} else if(state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS)) {
						world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
					}
				}
			}
		}
	}

	//////////////////////
	/// DATA STRUCTURE ///
	//////////////////////
	public static class PollutionPerWorld extends SavedData {

		public static final SavedData.Factory<PollutionPerWorld> FACTORY = new SavedData.Factory<>(PollutionPerWorld::new, PollutionPerWorld::new, null);

		/** Keys are 64x64 area coordinates packed like ChunkPos longs */
		public Map<Long, PollutionData> pollution = new HashMap<>();

		public PollutionPerWorld() { }

		public PollutionPerWorld(CompoundTag data, HolderLookup.Provider registries) {

			ListTag list = data.getList("entries", Tag.TAG_COMPOUND);

			for(int i = 0; i < list.size(); i++) {
				CompoundTag nbt = list.getCompound(i);
				int chunkX = nbt.getInt("chunkX");
				int chunkZ = nbt.getInt("chunkZ");
				pollution.put(ChunkPos.asLong(chunkX, chunkZ), PollutionData.fromNBT(nbt));
			}
		}

		@Override
		public CompoundTag save(CompoundTag data, HolderLookup.Provider registries) {

			ListTag list = new ListTag();

			for(Entry<Long, PollutionData> entry : pollution.entrySet()) {
				CompoundTag nbt = new CompoundTag();
				nbt.putInt("chunkX", ChunkPos.getX(entry.getKey()));
				nbt.putInt("chunkZ", ChunkPos.getZ(entry.getKey()));
				entry.getValue().toNBT(nbt);
				list.add(nbt);
			}

			data.put("entries", list);

			return data;
		}
	}

	public static class PollutionData {
		public float[] pollution = new float[PollutionType.values().length];

		public static PollutionData fromNBT(CompoundTag nbt) {
			PollutionData data = new PollutionData();

			for(int i = 0; i < PollutionType.values().length; i++) {
				data.pollution[i] = nbt.getFloat(PollutionType.values()[i].name().toLowerCase(Locale.US));
			}

			return data;
		}

		public void toNBT(CompoundTag nbt) {
			for(int i = 0; i < PollutionType.values().length; i++) {
				nbt.putFloat(PollutionType.values()[i].name().toLowerCase(Locale.US), pollution[i]);
			}
		}
	}

	public static enum PollutionType {
		SOOT, POISON, HEAVYMETAL, FALLOUT;
	}

	///////////////////
	/// MOB EFFECTS ///
	///////////////////

	public static final net.minecraft.resources.ResourceLocation maxHealth = RefStrings.loc("soot_anger_health");
	public static final net.minecraft.resources.ResourceLocation attackDamage = RefStrings.loc("soot_anger_damage");

	@SubscribeEvent
	public static void decorateMob(FinalizeSpawnEvent event) {

		if(!RadiationConfig.enablePollution) return;

		LivingEntity living = event.getEntity();
		Level world = living.level();
		if(world.isClientSide) return;

		PollutionData data = getPollutionData(world, BlockPos.containing(event.getX(), event.getY(), event.getZ()));
		if(data == null) return;

		if(living instanceof Enemy) {

			if(data.pollution[PollutionType.SOOT.ordinal()] > RadiationConfig.buffMobThreshold) {
				AttributeInstance health = living.getAttribute(Attributes.MAX_HEALTH);
				AttributeInstance damage = living.getAttribute(Attributes.ATTACK_DAMAGE);
				// operation 1 in 1.7.10 = multiply base
				if(health != null && !health.hasModifier(maxHealth)) health.addPermanentModifier(new AttributeModifier(maxHealth, 1D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
				if(damage != null && !damage.hasModifier(attackDamage)) damage.addPermanentModifier(new AttributeModifier(attackDamage, 1.5D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
				living.heal(living.getMaxHealth());
			}
		}
	}
}
