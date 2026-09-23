package com.hbm.main;

import java.util.ArrayList;
import java.util.List;

import com.hbm.commands.CommandRadiation;
import com.hbm.config.ServerConfig;
import com.hbm.handler.EntityEffectHandler;
import com.hbm.hazard.HazardSystem;
import com.hbm.lib.RefStrings;
import com.hbm.uninos.UniNodespace;

import api.hbm.tile.ILoadedTile.TileAccessCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Game bus events, counterpart of the original's ModEventHandler */
@EventBusSubscriber(modid = RefStrings.MODID)
public class ModEventHandler {

	@SubscribeEvent
	public static void onServerTickPre(ServerTickEvent.Pre event) {
		// Networks! All of them!
		UniNodespace.updateNodespace(event.getServer());
	}

	/** Was LivingUpdateEvent, which fired at the start of the living entity's update */
	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		if(!(event.getEntity() instanceof LivingEntity living)) return;

		EntityEffectHandler.onUpdate(living);

		if(!living.level().isClientSide && !(living instanceof Player)) {
			HazardSystem.updateLivingInventory(living);
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Pre event) {
		Player player = event.getEntity();

		if(!player.level().isClientSide) {
			HazardSystem.updatePlayerInventory(player);
		}
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if(!(event.getLevel() instanceof ServerLevel level)) return;

		int tickrate = Math.max(1, ServerConfig.ITEM_HAZARD_DROP_TICKRATE);

		if(level.getGameTime() % tickrate == 0) {
			List<ItemEntity> items = new ArrayList<>();
			for(Entity e : level.getAllEntities()) if(e instanceof ItemEntity item) items.add(item);
			items.forEach(HazardSystem::updateDroppedItem);
		}
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

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		CommandRadiation.register(event.getDispatcher());
	}
}
