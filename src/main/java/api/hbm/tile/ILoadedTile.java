package api.hbm.tile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** For anything that should be removed off networks when considered unloaded, only affects providers and receivers, not links. Must not necessarily be a tile. */
public interface ILoadedTile {

	public boolean isLoaded();

	/** Short-lived cache for tile lookups done by machine ports every few ticks */
	public static class TileAccessCache {

		private record Key(ResourceKey<Level> dim, long pos) { }

		public static Map<Key, TileAccessCache> cache = new HashMap<>();

		public static int NULL_CACHE = 20;
		public static int NONNULL_CACHE = 60;

		public BlockEntity tile;
		public long expiresOn;

		public TileAccessCache(BlockEntity tile, long expiresOn) {
			this.tile = tile;
			this.expiresOn = expiresOn;
		}

		public boolean hasExpired(long worldTime) {
			if(tile != null && tile.isRemoved()) return true;
			if(worldTime >= expiresOn) return true;
			if(tile instanceof ILoadedTile loaded && !loaded.isLoaded()) return true;
			return false;
		}

		public static BlockEntity getTileOrCache(Level world, BlockPos pos) {
			Key key = new Key(world.dimension(), pos.asLong());
			TileAccessCache entry = cache.get(key);
			long time = world.getGameTime();

			if(entry == null || entry.hasExpired(time)) {
				BlockEntity tile = world.isLoaded(pos) ? world.getBlockEntity(pos) : null;
				cache.put(key, new TileAccessCache(tile, time + (tile == null ? NULL_CACHE : NONNULL_CACHE)));
				return tile;
			}
			return entry.tile;
		}
	}
}
