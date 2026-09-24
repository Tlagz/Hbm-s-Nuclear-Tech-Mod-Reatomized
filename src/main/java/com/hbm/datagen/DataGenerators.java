package com.hbm.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.hbm.lib.RefStrings;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = RefStrings.MODID)
public class DataGenerators {

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		ExistingFileHelper efh = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

		generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, efh));
		generator.addProvider(event.includeClient(), new ModItemModelProvider(output, efh));
		// has to come after the model providers, it collects the textures they use
		generator.addProvider(event.includeClient(), new ModSpriteSourceProvider(output, lookup, efh));

		generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
				List.of(new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK)), lookup));
		generator.addProvider(event.includeServer(), new ModWorldGenProvider(output, lookup));
		generator.addProvider(event.includeServer(), new ModDataMapProvider(output, lookup));
		ModBlockTagProvider blockTags = generator.addProvider(event.includeServer(), new ModBlockTagProvider(output, lookup, efh));
		generator.addProvider(event.includeServer(), new ModItemTagProvider(output, lookup, blockTags.contentsGetter(), efh));
	}
}
