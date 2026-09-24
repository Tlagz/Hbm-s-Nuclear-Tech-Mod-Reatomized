package com.hbm.datagen;

import java.util.HashSet;
import java.util.Set;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockNTMGlass;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ItemEnums.EnumTarType;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.core.Holder;

/**
 * Block drops. Everything drops itself, except the special drops from the original's BlockOre.getItemDropped /
 * quantityDropped. The original's fortune formula (quantityDroppedWithBonus) is the same as vanilla's ore drops.
 */
public class ModBlockLootProvider extends BlockLootSubProvider {

	private final Set<Block> handled = new HashSet<>();

	public ModBlockLootProvider(HolderLookup.Provider registries) {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
	}

	@Override
	protected void generate() {

		oreDrop(ModBlocks.ore_fluorite.get(), ModItems.fluorite.get(), 2, 4, true);
		oreDrop(ModBlocks.ore_niter.get(), ModItems.niter.get(), 2, 4, true);
		oreDrop(ModBlocks.ore_sulfur.get(), ModItems.sulfur.get(), 2, 4, true);
		oreDrop(ModBlocks.ore_nether_sulfur.get(), ModItems.sulfur.get(), 2, 4, true);
		oreDrop(ModBlocks.ore_asbestos.get(), ModItems.ingot_asbestos.get(), 1, 1, true);
		oreDrop(ModBlocks.ore_gneiss_asbestos.get(), ModItems.ingot_asbestos.get(), 1, 1, true);
		oreDrop(ModBlocks.ore_lignite.get(), ModItems.lignite.get(), 1, 1, true);
		oreDrop(ModBlocks.ore_cinnebar.get(), ModItems.cinnebar.get(), 1, 1, true);
		oreDrop(ModBlocks.ore_coltan.get(), ModItems.fragment_coltan.get(), 1, 1, true);
		oreDrop(ModBlocks.ore_cobalt.get(), ModItems.fragment_cobalt.get(), 4, 9, true);
		oreDrop(ModBlocks.ore_nether_cobalt.get(), ModItems.fragment_cobalt.get(), 5, 12, true);
		oreDrop(ModBlocks.waste_planks.get(), Items.CHARCOAL, 1, 1, true);
		oreDrop(ModBlocks.frozen_dirt.get(), Items.SNOWBALL, 1, 1, true);
		oreDrop(ModBlocks.frozen_planks.get(), Items.SNOWBALL, 1, 1, true);
		oreDrop(ModBlocks.block_meteor_cobble.get(), ModItems.fragment_meteorite.get(), 1, 1, false);
		oreDrop(ModBlocks.block_meteor_broken.get(), ModItems.fragment_meteorite.get(), 1, 3, false);
		oreDrop(ModBlocks.ore_rare.get(), ModItems.chunk_ore.get(EnumChunkType.RARE).get(), 1, 1, true);
		oreDrop(ModBlocks.ore_gneiss_rare.get(), ModItems.chunk_ore.get(EnumChunkType.RARE).get(), 1, 1, true);

		// oil ore can't be silk touched and drops crude tar
		add(ModBlocks.ore_oil.get(), createSingleItemTable(ModItems.oil_tar.get(EnumTarType.CRUDE).get()));
		handled.add(ModBlocks.ore_oil.get());

		// 1 in 10 white phosphorus, otherwise hellfire powder
		Block netherFire = ModBlocks.ore_nether_fire.get();
		add(netherFire, createSilkTouchDispatchTable(netherFire, AlternativesEntry.alternatives(
				LootItem.lootTableItem(ModItems.ingot_phosphorus.get()).when(LootItemRandomChanceCondition.randomChance(0.1F)).apply(ApplyExplosionDecay.explosionDecay()),
				LootItem.lootTableItem(ModItems.powder_fire.get()).apply(ApplyBonusCount.addOreBonusCount(fortune())).apply(ApplyExplosionDecay.explosionDecay()))));
		handled.add(netherFire);

		// clusters drop crystals (BlockCluster in the original)
		oreDrop(ModBlocks.cluster_iron.get(), ModItems.crystal_iron.get(), 1, 1, false);
		oreDrop(ModBlocks.cluster_titanium.get(), ModItems.crystal_titanium.get(), 1, 1, false);
		oreDrop(ModBlocks.cluster_aluminium.get(), ModItems.crystal_aluminium.get(), 1, 1, false);
		oreDrop(ModBlocks.cluster_copper.get(), ModItems.crystal_copper.get(), 1, 1, false);

		// glass only drops with silk touch, unless the original said otherwise
		for(var holder : ModBlocks.BLOCKS.getEntries()) {
			if(holder.get() instanceof BlockNTMGlass glass && !glass.doesDrop) {
				add(glass, createSilkTouchOnlyTable(glass));
				handled.add(glass);
			}
		}

		// pipes keep their style (the original dropped the metadata)
		add(ModBlocks.fluid_duct_neo.get(), LootTable.lootTable().withPool(applyExplosionCondition(ModBlocks.fluid_duct_neo.get(), LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(LootItem.lootTableItem(ModBlocks.fluid_duct_neo.get()).apply(net.minecraft.world.level.storage.loot.functions.CopyBlockState.copyState(ModBlocks.fluid_duct_neo.get()).copy(com.hbm.blocks.network.FluidDuctStandard.STYLE))))));
		handled.add(ModBlocks.fluid_duct_neo.get());

		// barrels keep their fluid (IPersistentNBT)
		for(var barrel : ModBlocks.BARRELS) {
			if(barrel == ModBlocks.barrel_corroded) continue;
			add(barrel.get(), LootTable.lootTable().withPool(applyExplosionCondition(barrel.get(), LootPool.lootPool().setRolls(ConstantValue.exactly(1))
					.add(LootItem.lootTableItem(barrel.get()).apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(ModDataComponents.PERSISTENT.get()))))));
			handled.add(barrel.get());
		}

		// capacitors keep their charge
		for(var capacitor : java.util.List.of(ModBlocks.capacitor_copper, ModBlocks.capacitor_gold, ModBlocks.capacitor_niobium, ModBlocks.capacitor_tantalium, ModBlocks.capacitor_schrabidate)) {
			add(capacitor.get(), LootTable.lootTable().withPool(applyExplosionCondition(capacitor.get(), LootPool.lootPool().setRolls(ConstantValue.exactly(1))
					.add(LootItem.lootTableItem(capacitor.get()).apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(ModDataComponents.CHARGE.get()))))));
			handled.add(capacitor.get());
		}

		// molten meteor blocks turn into lava when broken and don't drop anything
		add(ModBlocks.block_meteor_molten.get(), LootTable.lootTable());
		handled.add(ModBlocks.block_meteor_molten.get());

		for(Block block : getKnownBlocks()) {
			if(!handled.contains(block)) dropSelf(block);
		}
	}

	private Holder<Enchantment> fortune() {
		return this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
	}

	/** Drops min..max of the item, silk touch drops the block itself */
	private void oreDrop(Block block, Item drop, int min, int max, boolean fortune) {
		LootItem.Builder<?> entry = LootItem.lootTableItem(drop);
		if(min != 1 || max != 1) entry.apply(SetItemCountFunction.setCount(min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max)));
		if(fortune) entry.apply(ApplyBonusCount.addOreBonusCount(fortune()));
		add(block, createSilkTouchDispatchTable(block, applyExplosionDecay(block, entry)));
		handled.add(block);
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return ModBlocks.BLOCKS.getEntries().stream().<Block>map(e -> e.value())::iterator;
	}
}
