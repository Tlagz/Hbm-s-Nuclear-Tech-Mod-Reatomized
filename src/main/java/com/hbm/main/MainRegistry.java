package com.hbm.main;

import org.slf4j.Logger;

import com.hbm.blocks.ModBlocks;
import com.hbm.creativetabs.ModCreativeTabs;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(RefStrings.MODID)
public class MainRegistry {

	public static final Logger logger = LogUtils.getLogger();

	public MainRegistry(IEventBus modEventBus, ModContainer modContainer) {
		ModBlocks.BLOCKS.register(modEventBus);
		ModItems.ITEMS.register(modEventBus);
		ModCreativeTabs.TABS.register(modEventBus);

		logger.info("Loading " + RefStrings.NAME);
	}
}
