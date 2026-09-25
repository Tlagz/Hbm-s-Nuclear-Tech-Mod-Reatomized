package com.hbm.datagen;

import java.util.concurrent.CompletableFuture;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ItemEnums.EnumBriquetteType;
import com.hbm.items.ItemEnums.EnumCokeType;
import com.hbm.items.ModItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * Data maps. Furnace fuels are the original's FuelHandler (an IFuelHandler, so they also worked in vanilla furnaces).
 *
 * TODO book_guide once ported
 */
public class ModDataMapProvider extends DataMapProvider {

	public ModDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		super(output, lookup);
	}

	@Override
	protected void gather(HolderLookup.Provider provider) {
		var fuels = builder(NeoForgeDataMaps.FURNACE_FUELS);
		int single = 200;

		fuel(fuels, ModItems.solid_fuel.get(), single * 16);
		fuel(fuels, ModItems.solid_fuel_presto.get(), single * 40);
		fuel(fuels, ModItems.solid_fuel_presto_triplet.get(), single * 200);
		fuel(fuels, ModItems.solid_fuel_bf.get(), single * 160);
		fuel(fuels, ModItems.solid_fuel_presto_bf.get(), single * 400);
		fuel(fuels, ModItems.solid_fuel_presto_triplet_bf.get(), single * 2000);
		fuel(fuels, ModItems.rocket_fuel.get(), single * 32);

		fuel(fuels, ModItems.biomass.get(), single * 2);
		fuel(fuels, ModItems.biomass_compressed.get(), single * 4);
		fuel(fuels, ModItems.powder_coal.get(), single * 8);
		fuel(fuels, ModItems.scrap.get(), single / 4);
		fuel(fuels, ModItems.dust.get(), single / 8);
		fuel(fuels, ModBlocks.block_scrap.get(), single * 2);
		fuel(fuels, ModItems.powder_fire.get(), 6400);
		fuel(fuels, ModItems.lignite.get(), 1200);
		fuel(fuels, ModItems.powder_lignite.get(), 1200);
		for(EnumCokeType coke : EnumCokeType.values()) fuel(fuels, ModItems.coke.get(coke).get(), single * 16);
		for(EnumCokeType coke : EnumCokeType.values()) fuel(fuels, ModBlocks.block_coke.get(coke).get(), single * 160);
		fuel(fuels, ModItems.coal_infernal.get(), 4800);
		fuel(fuels, ModItems.coal_eternal.get(), single * 16);
		fuel(fuels, ModItems.crystal_coal.get(), 6400);
		fuel(fuels, ModItems.powder_sawdust.get(), single / 2);

		fuel(fuels, ModItems.briquette.get(EnumBriquetteType.COAL).get(), single * 10);
		fuel(fuels, ModItems.briquette.get(EnumBriquetteType.LIGNITE).get(), single * 8);
		fuel(fuels, ModItems.briquette.get(EnumBriquetteType.WOOD).get(), single * 2);

		fuel(fuels, ModItems.powder_ash.get(EnumAshType.WOOD).get(), single / 2);
		fuel(fuels, ModItems.powder_ash.get(EnumAshType.COAL).get(), single);
		fuel(fuels, ModItems.powder_ash.get(EnumAshType.MISC).get(), single / 2);
		fuel(fuels, ModItems.powder_ash.get(EnumAshType.FLY).get(), single);
		fuel(fuels, ModItems.powder_ash.get(EnumAshType.SOOT).get(), single / 2);
	}

	private static void fuel(Builder<FurnaceFuel, net.minecraft.world.item.Item> builder, ItemLike item, int time) {
		builder.add(item.asItem().builtInRegistryHolder(), new FurnaceFuel(time), false);
	}
}
