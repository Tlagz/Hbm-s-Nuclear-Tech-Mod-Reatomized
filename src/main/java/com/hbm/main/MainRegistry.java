package com.hbm.main;

import org.slf4j.Logger;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.CommonConfig;
import com.hbm.creativetabs.ModCreativeTabs;
import com.hbm.hazard.HazardRegistry;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.ModTileEntities;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(RefStrings.MODID)
public class MainRegistry {

	public static final Logger logger = LogUtils.getLogger();

	public MainRegistry(IEventBus modEventBus, ModContainer modContainer) {
		CommonConfig.register(modContainer, modEventBus);

		ModBlocks.BLOCKS.register(modEventBus);
		ModItems.ITEMS.register(modEventBus);
		ModCreativeTabs.TABS.register(modEventBus);
		ModTileEntities.TILES.register(modEventBus);
		ModSounds.SOUNDS.register(modEventBus);
		ModAttachments.ATTACHMENTS.register(modEventBus);

		modEventBus.addListener(this::commonSetup);

		logger.info("Loading " + RefStrings.NAME);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(HazardRegistry::registerItems);
	}
}
