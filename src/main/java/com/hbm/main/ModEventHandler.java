package com.hbm.main;

import com.hbm.lib.RefStrings;
import com.hbm.uninos.UniNodespace;

import api.hbm.tile.ILoadedTile.TileAccessCache;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Game bus events, counterpart of the original's ModEventHandler */
@EventBusSubscriber(modid = RefStrings.MODID)
public class ModEventHandler {

	@SubscribeEvent
	public static void onServerTickPre(ServerTickEvent.Pre event) {
		// Networks! All of them!
		UniNodespace.updateNodespace(event.getServer());
	}

	@SubscribeEvent
	public static void onLevelUnload(LevelEvent.Unload event) {
		if(event.getLevel() instanceof Level level && !level.isClientSide) {
			UniNodespace.unloadWorld(level);
		}
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		UniNodespace.clear();
		TileAccessCache.cache.clear();
	}
}
