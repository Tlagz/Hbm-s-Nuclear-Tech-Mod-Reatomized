package com.hbm.main;

import java.io.File;

import org.slf4j.Logger;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.CommonConfig;
import com.hbm.creativetabs.ModCreativeTabs;
import com.hbm.hazard.HazardRegistry;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.ModMenus;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.potion.HbmPotion;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.world.gen.ModWorldGen;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;

@Mod(RefStrings.MODID)
public class MainRegistry {

	public static final Logger logger = LogUtils.getLogger();
	/** config/hbmConfig, where the original keeps its JSON configs (custom fluids, fluid traits, recipes...) */
	public static final File configHbmDir = FMLPaths.CONFIGDIR.get().resolve("hbmConfig").toFile();

	public MainRegistry(IEventBus modEventBus, ModContainer modContainer) {
		CommonConfig.register(modContainer, modEventBus);
		configHbmDir.mkdirs();

		ModBlocks.BLOCKS.register(modEventBus);
		ModItems.ITEMS.register(modEventBus);
		ModCreativeTabs.TABS.register(modEventBus);
		ModTileEntities.TILES.register(modEventBus);
		ModSounds.SOUNDS.register(modEventBus);
		ModAttachments.ATTACHMENTS.register(modEventBus);
		HbmPotion.EFFECTS.register(modEventBus);
		ModWorldGen.PLACEMENT_MODIFIERS.register(modEventBus);
		ModWorldGen.FEATURES.register(modEventBus);
		ModMenus.MENUS.register(modEventBus);
		ModDataComponents.COMPONENTS.register(modEventBus);

		modEventBus.addListener(this::commonSetup);

		logger.info("Loading " + RefStrings.NAME);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			// needs registered status effects (toxin traits), so not in the constructor like the original's preInit
			Fluids.init();
			OreDictManager.registerOres();
			HazardRegistry.registerItems();
			Fluids.reloadFluids();
			// after the fluids (custom ones included) and all items exist
			com.hbm.inventory.FluidContainerRegistry.register();
			com.hbm.inventory.recipes.RefineryRecipes.registerDefaults();
			com.hbm.inventory.recipes.PressRecipes.registerDefaults();
			com.hbm.inventory.recipes.anvil.AnvilRecipes.register();
			com.hbm.inventory.recipes.loader.GenericRecipes.clearPools();
			com.hbm.inventory.recipes.AssemblyMachineRecipes.INSTANCE.initialize();
			com.hbm.inventory.recipes.ChemicalPlantRecipes.INSTANCE.initialize();
			com.hbm.inventory.recipes.ArcWelderRecipes.registerDefaults();
			logger.info("Generic recipes: " + com.hbm.inventory.recipes.AssemblyMachineRecipes.INSTANCE.recipeOrderedList.size() + " assembly machine, "
					+ com.hbm.inventory.recipes.ChemicalPlantRecipes.INSTANCE.recipeOrderedList.size() + " chemical plant");
		});
	}
}
