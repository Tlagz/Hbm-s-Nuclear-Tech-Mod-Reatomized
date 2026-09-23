package com.hbm.blocks;

import java.util.function.Function;

import com.hbm.creativetabs.NtmTab;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RefStrings.MODID);

	public static final DeferredBlock<Block> ore_uranium = register("ore_uranium", Block::new, stone(5.0F, 10.0F), NtmTab.BLOCKS);
	public static final DeferredBlock<Block> ore_titanium = register("ore_titanium", Block::new, stone(5.0F, 10.0F), NtmTab.BLOCKS);

	public static final DeferredBlock<Block> block_uranium = register("block_uranium", Block::new, metal(5.0F, 50.0F), NtmTab.BLOCKS);
	public static final DeferredBlock<Block> block_titanium = register("block_titanium", Block::new, metal(5.0F, 50.0F), NtmTab.BLOCKS);
	public static final DeferredBlock<Block> block_steel = register("block_steel", Block::new, metal(5.0F, 50.0F), NtmTab.BLOCKS);

	/** Registers a block together with its BlockItem and adds it to the given creative tab (null for none). */
	public static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties props, NtmTab tab) {
		DeferredBlock<T> block = BLOCKS.registerBlock(name, factory, props);
		ModItems.ITEMS.registerSimpleBlockItem(block);
		if(tab != null) tab.add(block);
		return block;
	}

	/**
	 * 1.7.10's setResistance(r) resulted in an explosion resistance of r * 3 / 5,
	 * modern versions use the value directly. Declarations keep the original values.
	 */
	public static float legacyResistance(float resistance) {
		return resistance * 0.6F;
	}

	public static BlockBehaviour.Properties stone(float hardness, float resistance) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops()
				.strength(hardness, legacyResistance(resistance)).sound(SoundType.STONE);
	}

	public static BlockBehaviour.Properties metal(float hardness, float resistance) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
				.strength(hardness, legacyResistance(resistance)).sound(SoundType.METAL);
	}
}
