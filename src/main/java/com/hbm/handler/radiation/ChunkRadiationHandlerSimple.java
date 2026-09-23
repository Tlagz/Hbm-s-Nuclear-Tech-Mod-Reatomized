package com.hbm.handler.radiation;

import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * Most basic implementation of a chunk radiation system: Each chunk has a radiation value which spreads out to its neighbors.
 * Values are saved with the chunk under the original's NBT key.
 *
 * TODO radiation fog particles (needs the particle/effect packet system), world destruction (needs waste_earth/waste_leaves)
 * @author hbm
 */
public class ChunkRadiationHandlerSimple extends ChunkRadiationHandler {

	private final Map<LevelAccessor, SimpleRadiationPerWorld> perWorld = new HashMap<>();
	private static final float maxRad = 100_000F;

	@Override
	public float getRadiation(Level world, BlockPos pos) {
		SimpleRadiationPerWorld radWorld = perWorld.get(world);

		if(radWorld != null) {
			long coords = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
			return Mth.clamp(radWorld.radiation.get(coords), 0, maxRad);
		}

		return 0;
	}

	@Override
	public void setRadiation(Level world, BlockPos pos, float rad) {
		SimpleRadiationPerWorld radWorld = perWorld.get(world);

		if(radWorld != null) {

			if(world.isLoaded(pos)) {
				radWorld.radiation.put(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), Mth.clamp(rad, 0, maxRad));
				world.getChunkAt(pos).setUnsaved(true);
			}
		}
	}

	@Override
	public void incrementRad(Level world, BlockPos pos, float rad) {
		setRadiation(world, pos, getRadiation(world, pos) + rad);
	}

	@Override
	public void decrementRad(Level world, BlockPos pos, float rad) {
		setRadiation(world, pos, Math.max(getRadiation(world, pos) - rad, 0));
	}

	@Override
	public void updateSystem() {

		for(Map.Entry<LevelAccessor, SimpleRadiationPerWorld> entry : perWorld.entrySet()) {

			SimpleRadiationPerWorld radWorld = entry.getValue();
			Long2FloatMap radiation = radWorld.radiation;
			Long2FloatMap buff = new Long2FloatOpenHashMap(radiation);
			radiation.clear();

			for(Long2FloatMap.Entry chunk : buff.long2FloatEntrySet()) {

				if(chunk.getFloatValue() == 0)
					continue;

				int cx = ChunkPos.getX(chunk.getLongKey());
				int cz = ChunkPos.getZ(chunk.getLongKey());

				for(int i = -1; i <= 1; i++) {
					for(int j = -1; j <= 1; j++) {

						int type = Math.abs(i) + Math.abs(j);
						float percent = type == 0 ? 0.6F : type == 1 ? 0.075F : 0.025F;
						long newCoord = ChunkPos.asLong(cx + i, cz + j);

						if(buff.containsKey(newCoord)) {
							float rad = radiation.get(newCoord);
							float newRad = rad + chunk.getFloatValue() * percent;
							// the original clamps with the arguments in the wrong order, which boils down to this (no upper limit here)
							newRad = Math.max(newRad * 0.99F - 0.05F, 0F);
							radiation.put(newCoord, newRad);
						} else {
							radiation.put(newCoord, chunk.getFloatValue() * percent);
						}
					}
				}
			}

			// spreading changes the saved value, make sure loaded chunks get saved again
			if(entry.getKey() instanceof ServerLevel level) {
				for(Long2FloatMap.Entry chunk : radiation.long2FloatEntrySet()) {
					if(buff.get(chunk.getLongKey()) == chunk.getFloatValue()) continue;
					ChunkAccess loaded = level.getChunkSource().getChunkNow(ChunkPos.getX(chunk.getLongKey()), ChunkPos.getZ(chunk.getLongKey()));
					if(loaded != null) loaded.setUnsaved(true);
				}
			}
		}
	}

	@Override
	public void clearSystem(Level world) {
		SimpleRadiationPerWorld radWorld = perWorld.get(world);

		if(radWorld != null) {
			radWorld.radiation.clear();
		}
	}

	@Override
	public void receiveWorldLoad(LevelEvent.Load event) {
		if(!event.getLevel().isClientSide())
			perWorld.put(event.getLevel(), new SimpleRadiationPerWorld());
	}

	@Override
	public void receiveWorldUnload(LevelEvent.Unload event) {
		if(!event.getLevel().isClientSide())
			perWorld.remove(event.getLevel());
	}

	private static final String NBT_KEY_CHUNK_RADIATION = "hfr_simple_radiation";

	@Override
	public void receiveChunkLoad(ChunkDataEvent.Load event) {
		SimpleRadiationPerWorld radWorld = perWorld.get(event.getLevel());

		if(radWorld != null) {
			long pos = event.getChunk().getPos().toLong();
			radWorld.pendingUnload.remove(pos);
			radWorld.radiation.put(pos, event.getData().getFloat(NBT_KEY_CHUNK_RADIATION));
		}
	}

	@Override
	public void receiveChunkSave(ChunkDataEvent.Save event) {
		SimpleRadiationPerWorld radWorld = perWorld.get(event.getLevel());

		if(radWorld != null) {
			long pos = event.getChunk().getPos().toLong();
			event.getData().putFloat(NBT_KEY_CHUNK_RADIATION, radWorld.radiation.get(pos));
			// unloading chunks are saved right after the unload event, drop them now that the value is on disk
			if(radWorld.pendingUnload.remove(pos)) radWorld.radiation.remove(pos);
		}
	}

	/**
	 * The original tried to remove the chunk object instead of its coordinates, so this never did anything.
	 * NeoForge fires the unload event before the chunk is saved, so the value is only dropped after that save.
	 */
	@Override
	public void receiveChunkUnload(ChunkEvent.Unload event) {
		SimpleRadiationPerWorld radWorld = perWorld.get(event.getLevel());
		ChunkAccess chunk = event.getChunk();

		if(radWorld != null) {
			radWorld.pendingUnload.add(chunk.getPos().toLong());
		}
	}

	public static class SimpleRadiationPerWorld {
		/** Chunk pos (as long) to radiation, missing entries read as 0 */
		public Long2FloatMap radiation = new Long2FloatOpenHashMap();
		public LongSet pendingUnload = new LongOpenHashSet();
	}
}
