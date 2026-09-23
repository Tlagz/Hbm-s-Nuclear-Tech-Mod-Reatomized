package com.hbm.handler.radiation;

import com.hbm.config.RadiationConfig;
import com.hbm.lib.RefStrings;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** TODO PRISM (3D resistance-aware radiation, optional in the original) */
@EventBusSubscriber(modid = RefStrings.MODID)
public class ChunkRadiationManager {

	public static ChunkRadiationHandler proxy = new ChunkRadiationHandlerSimple();

	@SubscribeEvent
	public static void onWorldLoad(LevelEvent.Load event) {
		if(RadiationConfig.enableChunkRads) proxy.receiveWorldLoad(event);
	}

	@SubscribeEvent
	public static void onWorldUnload(LevelEvent.Unload event) {
		if(RadiationConfig.enableChunkRads) proxy.receiveWorldUnload(event);
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkDataEvent.Load event) {
		if(RadiationConfig.enableChunkRads) proxy.receiveChunkLoad(event);
	}

	@SubscribeEvent
	public static void onChunkSave(ChunkDataEvent.Save event) {
		if(RadiationConfig.enableChunkRads) proxy.receiveChunkSave(event);
	}

	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event) {
		if(RadiationConfig.enableChunkRads && !event.getLevel().isClientSide()) proxy.receiveChunkUnload(event);
	}

	private static int eggTimer = 0;

	@SubscribeEvent
	public static void updateSystem(ServerTickEvent.Post event) {

		if(RadiationConfig.enableChunkRads) {

			eggTimer++;

			if(eggTimer >= 20) {
				proxy.updateSystem();
				eggTimer = 0;
			}

			if(RadiationConfig.worldRadEffects) {
				proxy.handleWorldDestruction();
			}

			proxy.receiveWorldTick();
		}
	}
}
