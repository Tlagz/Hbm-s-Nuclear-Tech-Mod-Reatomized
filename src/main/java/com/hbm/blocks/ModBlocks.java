package com.hbm.blocks;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.hbm.blocks.generic.BlockFallingNT;
import com.hbm.blocks.generic.BlockHazard;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.blocks.generic.BlockOutgas;
import com.hbm.blocks.network.BlockCable;
import com.hbm.creativetabs.NtmTab;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class ModBlocks {

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RefStrings.MODID);

	/** Full cube blocks with one texture, block -> texture path (e.g. "blocks/ore_uranium"), used by datagen */
	public static final Map<DeferredBlock<?>, String> CUBE_MODELS = new LinkedHashMap<>();
	/** Which tool mines the block, used by datagen for the mineable tags */
	public static final Map<DeferredBlock<?>, Tool> TOOLS = new LinkedHashMap<>();
	/** Blocks that can be used for beacon bases */
	public static final Set<DeferredBlock<?>> BEACON_BASES = new HashSet<>();

	/** Marks "setResistance was never called", the resistance then follows the hardness like in 1.7.10 */
	public static final float LEGACY_NONE = -1F;

	/// HAND-PORTED ///
	public static final DeferredBlock<BlockCable> red_cable = register("red_cable", BlockCable::new, props(Mat.IRON, 5.0F, 10.0F).noOcclusion(), NtmTab.MACHINE);

	/// GENERATED from the original's declarations by tools/gen_content.py, don't edit by hand ///
	// BEGIN GENERATED
	public static final DeferredBlock<Block> structure_anchor = generated("structure_anchor", Block::new, props(Mat.IRON, 2.5F, 10.0F), null, "blocks/structure_anchor");
	public static final DeferredBlock<BlockOutgas> ore_uranium = generated("ore_uranium", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_uranium");
	public static final DeferredBlock<BlockOutgas> ore_uranium_scorched = generated("ore_uranium_scorched", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_uranium_scorched");
	public static final DeferredBlock<Block> ore_thorium = generated("ore_thorium", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_thorium");
	public static final DeferredBlock<Block> ore_titanium = generated("ore_titanium", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_titanium");
	public static final DeferredBlock<BlockOre> ore_sulfur = generated("ore_sulfur", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_sulfur");
	public static final DeferredBlock<BlockOre> ore_niter = generated("ore_niter", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_niter");
	public static final DeferredBlock<Block> ore_copper = generated("ore_copper", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_copper");
	public static final DeferredBlock<Block> ore_tungsten = generated("ore_tungsten", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_tungsten");
	public static final DeferredBlock<Block> ore_aluminium = generated("ore_aluminium", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_aluminium");
	public static final DeferredBlock<BlockOre> ore_fluorite = generated("ore_fluorite", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_fluorite");
	public static final DeferredBlock<Block> ore_beryllium = generated("ore_beryllium", Block::new, props(Mat.ROCK, 5.0F, 15.0F), NtmTab.BLOCKS, "blocks/ore_beryllium");
	public static final DeferredBlock<Block> ore_lead = generated("ore_lead", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_lead");
	public static final DeferredBlock<BlockOre> ore_oil = generated("ore_oil", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_oil");
	public static final DeferredBlock<Block> ore_oil_empty = generated("ore_oil_empty", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_oil_empty");
	public static final DeferredBlock<BlockFallingNT> ore_oil_sand = generated("ore_oil_sand", BlockFallingNT::new, props(Mat.SAND, 0.5F, 1.0F).sound(SoundType.SAND), NtmTab.BLOCKS, "blocks/ore_oil_sand_alt");
	public static final DeferredBlock<BlockOre> ore_lignite = generated("ore_lignite", BlockOre::new, props(Mat.ROCK, 5.0F, 15.0F), NtmTab.BLOCKS, "blocks/ore_lignite");
	public static final DeferredBlock<BlockOutgas> ore_asbestos = generated("ore_asbestos", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 5.0F, 15.0F), NtmTab.BLOCKS, "blocks/ore_asbestos");
	public static final DeferredBlock<BlockOre> ore_schrabidium = generated("ore_schrabidium", p -> new BlockOre(p).setRad(0.1F), props(Mat.ROCK, 15.0F, 600.0F), NtmTab.BLOCKS, "blocks/ore_schrabidium");
	public static final DeferredBlock<Block> ore_australium = generated("ore_australium", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_australium");
	public static final DeferredBlock<BlockOre> ore_rare = generated("ore_rare", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_rare");
	public static final DeferredBlock<BlockOre> ore_cobalt = generated("ore_cobalt", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_cobalt");
	public static final DeferredBlock<BlockOre> ore_cinnebar = generated("ore_cinnebar", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_cinnebar");
	public static final DeferredBlock<BlockOre> ore_coltan = generated("ore_coltan", BlockOre::new, props(Mat.ROCK, 15.0F, 10.0F), NtmTab.BLOCKS, "blocks/ore_coltan");
	public static final DeferredBlock<Block> ore_bedrock_oil = generated("ore_bedrock_oil", Block::new, props(Mat.ROCK, 0.0F, 1_000_000).strength(-1.0F, 3600000.0F), NtmTab.BLOCKS, "blocks/ore_bedrock_oil");
	public static final DeferredBlock<BlockOutgas> ore_nether_uranium = generated("ore_nether_uranium", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, "blocks/ore_nether_uranium");
	public static final DeferredBlock<BlockOutgas> ore_nether_uranium_scorched = generated("ore_nether_uranium_scorched", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, "blocks/ore_nether_uranium_scorched");
	public static final DeferredBlock<Block> ore_nether_plutonium = generated("ore_nether_plutonium", Block::new, props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, "blocks/ore_nether_plutonium");
	public static final DeferredBlock<Block> ore_nether_tungsten = generated("ore_nether_tungsten", Block::new, props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, "blocks/ore_nether_tungsten");
	public static final DeferredBlock<BlockOre> ore_nether_sulfur = generated("ore_nether_sulfur", BlockOre::new, props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, "blocks/ore_nether_sulfur");
	public static final DeferredBlock<BlockOre> ore_nether_fire = generated("ore_nether_fire", BlockOre::new, props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, "blocks/ore_nether_fire");
	public static final DeferredBlock<BlockOre> ore_nether_cobalt = generated("ore_nether_cobalt", BlockOre::new, props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, "blocks/ore_nether_cobalt");
	public static final DeferredBlock<Block> ore_nether_schrabidium = generated("ore_nether_schrabidium", Block::new, props(Mat.ROCK, 15.0F, 600.0F), NtmTab.BLOCKS, "blocks/ore_nether_schrabidium");
	public static final DeferredBlock<BlockOre> ore_gneiss_iron = generated("ore_gneiss_iron", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/ore_gneiss_iron");
	public static final DeferredBlock<BlockOre> ore_gneiss_gold = generated("ore_gneiss_gold", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/ore_gneiss_gold");
	public static final DeferredBlock<BlockOutgas> ore_gneiss_uranium = generated("ore_gneiss_uranium", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/ore_gneiss_uranium");
	public static final DeferredBlock<BlockOutgas> ore_gneiss_uranium_scorched = generated("ore_gneiss_uranium_scorched", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/ore_gneiss_uranium_scorched");
	public static final DeferredBlock<BlockOre> ore_gneiss_copper = generated("ore_gneiss_copper", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/ore_gneiss_copper");
	public static final DeferredBlock<BlockOutgas> ore_gneiss_asbestos = generated("ore_gneiss_asbestos", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/ore_gneiss_asbestos");
	public static final DeferredBlock<BlockOre> ore_gneiss_lithium = generated("ore_gneiss_lithium", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/ore_gneiss_lithium");
	public static final DeferredBlock<BlockOre> ore_gneiss_schrabidium = generated("ore_gneiss_schrabidium", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/ore_gneiss_schrabidium");
	public static final DeferredBlock<BlockOre> ore_gneiss_rare = generated("ore_gneiss_rare", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/ore_gneiss_rare");
	public static final DeferredBlock<BlockOre> ore_gneiss_gas = generated("ore_gneiss_gas", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/ore_gneiss_gas");
	public static final DeferredBlock<Block> stone_gneiss = generated("stone_gneiss", Block::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/stone_gneiss_var");
	public static final DeferredBlock<Block> gneiss_brick = generated("gneiss_brick", Block::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/gneiss_brick");
	public static final DeferredBlock<Block> gneiss_tile = generated("gneiss_tile", Block::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, "blocks/gneiss_tile");
	public static final DeferredBlock<Block> basalt_smooth = generated("basalt_smooth", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/basalt_smooth");
	public static final DeferredBlock<Block> basalt_brick = generated("basalt_brick", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/basalt_brick");
	public static final DeferredBlock<Block> basalt_polished = generated("basalt_polished", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/basalt_polished");
	public static final DeferredBlock<Block> basalt_tiles = generated("basalt_tiles", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/basalt_tiles");
	public static final DeferredBlock<BlockHazard> block_uranium = generated("block_uranium", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_uranium", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_u233 = generated("block_u233", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_u233", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_u235 = generated("block_u235", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_u235", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_u238 = generated("block_u238", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_u238", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_uranium_fuel = generated("block_uranium_fuel", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_uranium_fuel", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_neptunium = generated("block_neptunium", BlockHazard::new, props(Mat.IRON, 5.0F, 60.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_neptunium", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_mox_fuel = generated("block_mox_fuel", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_mox_fuel", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_plutonium = generated("block_plutonium", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_plutonium", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_pu239 = generated("block_pu239", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_pu239", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_pu240 = generated("block_pu240", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_pu240", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_pu_mix = generated("block_pu_mix", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_pu_mix", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_plutonium_fuel = generated("block_plutonium_fuel", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_plutonium_fuel", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_thorium = generated("block_thorium", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_thorium", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_thorium_fuel = generated("block_thorium_fuel", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_thorium_fuel", Gen.BEACON);
	public static final DeferredBlock<Block> block_titanium = generated("block_titanium", Block::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_titanium", Gen.BEACON);
	public static final DeferredBlock<Block> block_sulfur = generated("block_sulfur", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/block_sulfur", Gen.BEACON);
	public static final DeferredBlock<Block> block_niter = generated("block_niter", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/block_niter", Gen.BEACON);
	public static final DeferredBlock<Block> block_copper = generated("block_copper", Block::new, props(Mat.IRON, 5.0F, 20.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_copper", Gen.BEACON);
	public static final DeferredBlock<Block> block_red_copper = generated("block_red_copper", Block::new, props(Mat.IRON, 5.0F, 25.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_red_copper", Gen.BEACON);
	public static final DeferredBlock<Block> block_tungsten = generated("block_tungsten", Block::new, props(Mat.IRON, 5.0F, 20.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_tungsten", Gen.BEACON);
	public static final DeferredBlock<Block> block_aluminium = generated("block_aluminium", Block::new, props(Mat.IRON, 5.0F, 20.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_aluminium", Gen.BEACON);
	public static final DeferredBlock<Block> block_fluorite = generated("block_fluorite", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/block_fluorite", Gen.BEACON);
	public static final DeferredBlock<Block> block_beryllium = generated("block_beryllium", Block::new, props(Mat.IRON, 5.0F, 20.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_beryllium", Gen.BEACON);
	public static final DeferredBlock<Block> block_cobalt = generated("block_cobalt", Block::new, props(Mat.IRON, 5.0F, 50.0F), NtmTab.BLOCKS, "blocks/block_cobalt", Gen.BEACON);
	public static final DeferredBlock<Block> block_steel = generated("block_steel", Block::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_steel", Gen.BEACON);
	public static final DeferredBlock<Block> block_tcalloy = generated("block_tcalloy", Block::new, props(Mat.IRON, 5.0F, 70.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_tcalloy", Gen.BEACON);
	public static final DeferredBlock<Block> block_cdalloy = generated("block_cdalloy", Block::new, props(Mat.IRON, 5.0F, 70.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_cdalloy", Gen.BEACON);
	public static final DeferredBlock<Block> block_lead = generated("block_lead", Block::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_lead", Gen.BEACON);
	public static final DeferredBlock<Block> block_bismuth = generated("block_bismuth", Block::new, props(Mat.IRON, 5.0F, 90.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_bismuth", Gen.BEACON);
	public static final DeferredBlock<Block> block_cadmium = generated("block_cadmium", Block::new, props(Mat.IRON, 5.0F, 90.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_cadmium", Gen.BEACON);
	public static final DeferredBlock<Block> block_coltan = generated("block_coltan", Block::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_coltan", Gen.BEACON);
	public static final DeferredBlock<Block> block_tantalium = generated("block_tantalium", Block::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_tantalium", Gen.BEACON);
	public static final DeferredBlock<Block> block_zirconium = generated("block_zirconium", Block::new, props(Mat.IRON, 5.0F, 30.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_zirconium", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_white_phosphorus = generated("block_white_phosphorus", BlockHazard::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/block_white_phosphorus", Gen.BEACON);
	public static final DeferredBlock<BlockFallingNT> block_scrap = generated("block_scrap", BlockFallingNT::new, props(Mat.SAND, 2.5F, 5.0F).sound(SoundType.GRAVEL), NtmTab.BLOCKS, "blocks/block_scrap");
	public static final DeferredBlock<BlockFallingNT> block_electrical_scrap = generated("block_electrical_scrap", BlockFallingNT::new, props(Mat.IRON, 2.5F, 5.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/electrical_scrap");
	public static final DeferredBlock<Block> block_foam = generated("block_foam", Block::new, props(Mat.SNOW, 0.5F, 0.0F).sound(SoundType.SNOW), NtmTab.BLOCKS, "blocks/foam");
	public static final DeferredBlock<Block> block_boron = generated("block_boron", Block::new, props(Mat.IRON, 5.0F, 10.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_boron", Gen.BEACON);
	public static final DeferredBlock<BlockOutgas> block_asbestos = generated("block_asbestos", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.CLOTH, 5.0F, 15.0F).sound(SoundType.WOOL), NtmTab.BLOCKS, "blocks/block_asbestos");
	public static final DeferredBlock<BlockHazard> block_trinitite = generated("block_trinitite", BlockHazard::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/block_trinitite", Gen.BEACON);
	public static final DeferredBlock<BlockOutgas> ancient_scrap = generated("ancient_scrap", p -> new BlockOutgas(p).setOutgas(true, 1, true, true), props(Mat.IRON, 100.0F, 6000.0F), NtmTab.BLOCKS, "blocks/ancient_scrap");
	public static final DeferredBlock<BlockHazard> block_corium = generated("block_corium", BlockHazard::new, props(Mat.IRON, 100.0F, 6000.0F), NtmTab.BLOCKS, "blocks/block_corium");
	public static final DeferredBlock<BlockOutgas> block_corium_cobble = generated("block_corium_cobble", p -> new BlockOutgas(p).setOutgas(true, 1, true, true), props(Mat.IRON, 100.0F, 6000.0F), NtmTab.BLOCKS, "blocks/block_corium_cobble");
	public static final DeferredBlock<BlockHazard> block_schraranium = generated("block_schraranium", BlockHazard::new, props(Mat.IRON, 5.0F, 250.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_schraranium", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_schrabidium = generated("block_schrabidium", BlockHazard::new, props(Mat.IRON, 5.0F, 600.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_schrabidium", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_schrabidate = generated("block_schrabidate", BlockHazard::new, props(Mat.IRON, 5.0F, 600.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_schrabidate", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_solinium = generated("block_solinium", BlockHazard::new, props(Mat.IRON, 5.0F, 600.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_solinium", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_schrabidium_fuel = generated("block_schrabidium_fuel", BlockHazard::new, props(Mat.IRON, 5.0F, 600.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_schrabidium_fuel", Gen.BEACON);
	public static final DeferredBlock<Block> block_euphemium = generated("block_euphemium", Block::new, props(Mat.IRON, 5.0F, 60000.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_euphemium", Gen.BEACON);
	public static final DeferredBlock<Block> block_dineutronium = generated("block_dineutronium", Block::new, props(Mat.IRON, 5.0F, 60000.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_dineutronium", Gen.BEACON);
	public static final DeferredBlock<Block> block_magnetized_tungsten = generated("block_magnetized_tungsten", Block::new, props(Mat.IRON, 5.0F, 75.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_magnetized_tungsten", Gen.BEACON);
	public static final DeferredBlock<Block> block_combine_steel = generated("block_combine_steel", Block::new, props(Mat.IRON, 5.0F, 600.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_combine_steel", Gen.BEACON);
	public static final DeferredBlock<Block> block_desh = generated("block_desh", Block::new, props(Mat.IRON, 5.0F, 300.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_desh", Gen.BEACON);
	public static final DeferredBlock<Block> block_dura_steel = generated("block_dura_steel", Block::new, props(Mat.IRON, 5.0F, 200.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_dura_steel", Gen.BEACON);
	public static final DeferredBlock<Block> block_starmetal = generated("block_starmetal", Block::new, props(Mat.IRON, 5.0F, 400.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_starmetal", Gen.BEACON);
	public static final DeferredBlock<Block> block_polymer = generated("block_polymer", Block::new, props(Mat.ROCK, 3.0F, 10.0F).sound(SoundType.STONE), NtmTab.BLOCKS, "blocks/block_polymer", Gen.BEACON);
	public static final DeferredBlock<Block> block_bakelite = generated("block_bakelite", Block::new, props(Mat.ROCK, 3.0F, 5.0F).sound(SoundType.STONE), NtmTab.BLOCKS, "blocks/block_bakelite", Gen.BEACON);
	public static final DeferredBlock<Block> block_rubber = generated("block_rubber", Block::new, props(Mat.ROCK, 3.0F, 15.0F).sound(SoundType.STONE), NtmTab.BLOCKS, "blocks/block_rubber", Gen.BEACON);
	public static final DeferredBlock<Block> block_australium = generated("block_australium", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/block_australium", Gen.BEACON);
	public static final DeferredBlock<Block> block_lanthanium = generated("block_lanthanium", Block::new, props(Mat.IRON, 5.0F, 10.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_lanthanium", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_ra226 = generated("block_ra226", BlockHazard::new, props(Mat.IRON, 5.0F, 10.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_ra226", Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_actinium = generated("block_actinium", BlockHazard::new, props(Mat.IRON, 5.0F, 10.0F).sound(SoundType.METAL), NtmTab.BLOCKS, "blocks/block_actinium", Gen.BEACON);
	public static final DeferredBlock<BlockOre> deco_titanium = generated("deco_titanium", p -> new BlockOre(p).noFortune(), props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/deco_titanium");
	public static final DeferredBlock<BlockOutgas> deco_asbestos = generated("deco_asbestos", p -> new BlockOutgas(p).setOutgas(true, 5, true).noFortune(), props(Mat.CLOTH, 5.0F, 10.0F), NtmTab.BLOCKS, "blocks/deco_asbestos");
	public static final DeferredBlock<Block> deco_rbmk = generated("deco_rbmk", Block::new, props(Mat.IRON, 5.0F, 100.0F), NtmTab.BLOCKS, "blocks/rbmk/rbmk_top");
	public static final DeferredBlock<Block> deco_rbmk_smooth = generated("deco_rbmk_smooth", Block::new, props(Mat.IRON, 5.0F, 100.0F), NtmTab.BLOCKS, "blocks/rbmk/rbmk_blank_top");
	public static final DeferredBlock<BlockFallingNT> gravel_obsidian = generated("gravel_obsidian", BlockFallingNT::new, props(Mat.IRON, 5.0F, 240.0F).sound(SoundType.GRAVEL), NtmTab.BLOCKS, "blocks/gravel_obsidian");
	public static final DeferredBlock<BlockFallingNT> gravel_diamond = generated("gravel_diamond", BlockFallingNT::new, props(Mat.SAND, 0.6F, LEGACY_NONE).sound(SoundType.GRAVEL), NtmTab.BLOCKS, "blocks/gravel_diamond");
	public static final DeferredBlock<Block> reinforced_brick = generated("reinforced_brick", Block::new, props(Mat.ROCK, 15.0F, 300.0F), NtmTab.BLOCKS, "blocks/reinforced_brick");
	public static final DeferredBlock<Block> reinforced_light = generated("reinforced_light", Block::new, props(Mat.ROCK, 15.0F, 80.0F).lightLevel(s -> (int) (1.0F * 15)), NtmTab.BLOCKS, "blocks/reinforced_light");
	public static final DeferredBlock<Block> reinforced_sand = generated("reinforced_sand", Block::new, props(Mat.ROCK, 15.0F, 40.0F), NtmTab.BLOCKS, "blocks/reinforced_sand");
	public static final DeferredBlock<Block> reinforced_stone = generated("reinforced_stone", Block::new, props(Mat.ROCK, 15.0F, 100.0F), NtmTab.BLOCKS, "blocks/reinforced_stone");
	public static final DeferredBlock<BlockFallingNT> concrete_super_broken = generated("concrete_super_broken", BlockFallingNT::new, props(Mat.ROCK, 10.0F, 20.0F), NtmTab.BLOCKS, "blocks/concrete_super_broken");
	public static final DeferredBlock<Block> brick_concrete_mossy = generated("brick_concrete_mossy", Block::new, props(Mat.ROCK, 15.0F, 160.0F), NtmTab.BLOCKS, "blocks/brick_concrete_mossy");
	public static final DeferredBlock<Block> brick_concrete_cracked = generated("brick_concrete_cracked", Block::new, props(Mat.ROCK, 15.0F, 60.0F), NtmTab.BLOCKS, "blocks/brick_concrete_cracked");
	public static final DeferredBlock<Block> brick_concrete_broken = generated("brick_concrete_broken", Block::new, props(Mat.ROCK, 15.0F, 45.0F), NtmTab.BLOCKS, "blocks/brick_concrete_broken");
	public static final DeferredBlock<Block> brick_obsidian = generated("brick_obsidian", Block::new, props(Mat.ROCK, 15.0F, 120.0F), NtmTab.BLOCKS, "blocks/brick_obsidian");
	public static final DeferredBlock<Block> brick_compound = generated("brick_compound", Block::new, props(Mat.ROCK, 15.0F, 400.0F), NtmTab.BLOCKS, "blocks/brick_compound");
	public static final DeferredBlock<Block> brick_light = generated("brick_light", Block::new, props(Mat.ROCK, 5.0F, 20.0F), NtmTab.BLOCKS, "blocks/brick_light");
	public static final DeferredBlock<BlockOutgas> brick_asbestos = generated("brick_asbestos", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 5.0F, 1000.0F), NtmTab.BLOCKS, "blocks/brick_asbestos");
	public static final DeferredBlock<Block> brick_fire = generated("brick_fire", Block::new, props(Mat.ROCK, 5.0F, 35.0F), NtmTab.BLOCKS, "blocks/brick_fire");
	public static final DeferredBlock<Block> cmb_brick = generated("cmb_brick", Block::new, props(Mat.ROCK, 25.0F, 5000.0F), NtmTab.BLOCKS, "blocks/cmb_brick");
	public static final DeferredBlock<Block> cmb_brick_reinforced = generated("cmb_brick_reinforced", Block::new, props(Mat.ROCK, 25.0F, 50000.0F), NtmTab.BLOCKS, "blocks/cmb_brick_reinforced");
	public static final DeferredBlock<BlockOutgas> tile_lab = generated("tile_lab", p -> new BlockOutgas(p).setOutgas(false, 5, true), props(Mat.ROCK, 1.0F, 20.0F).sound(SoundType.GLASS), NtmTab.BLOCKS, "blocks/tile_lab");
	public static final DeferredBlock<BlockOutgas> tile_lab_cracked = generated("tile_lab_cracked", p -> new BlockOutgas(p).setOutgas(false, 5, true), props(Mat.ROCK, 1.0F, 20.0F).sound(SoundType.GLASS), NtmTab.BLOCKS, "blocks/tile_lab_cracked");
	public static final DeferredBlock<BlockOutgas> tile_lab_broken = generated("tile_lab_broken", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 1.0F, 20.0F).sound(SoundType.GLASS), NtmTab.BLOCKS, "blocks/tile_lab_broken");
	public static final DeferredBlock<BlockOre> block_meteor = generated("block_meteor", p -> new BlockOre(p).noFortune(), props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, "blocks/meteor");
	public static final DeferredBlock<BlockOre> block_meteor_cobble = generated("block_meteor_cobble", p -> new BlockOre(p).noFortune(), props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, "blocks/meteor_cobble");
	public static final DeferredBlock<BlockOre> block_meteor_broken = generated("block_meteor_broken", p -> new BlockOre(p).noFortune(), props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, "blocks/meteor_crushed");
	public static final DeferredBlock<BlockOre> block_meteor_molten = generated("block_meteor_molten", p -> new BlockOre(p).noFortune(), props(Mat.ROCK, 15.0F, 360.0F).lightLevel(s -> (int) (0.75F * 15)), NtmTab.BLOCKS, "blocks/meteor_cobble_molten");
	public static final DeferredBlock<Block> meteor_polished = generated("meteor_polished", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, "blocks/meteor_polished");
	public static final DeferredBlock<Block> meteor_brick = generated("meteor_brick", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, "blocks/meteor_brick");
	public static final DeferredBlock<Block> meteor_brick_mossy = generated("meteor_brick_mossy", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, "blocks/meteor_brick_mossy");
	public static final DeferredBlock<Block> meteor_brick_cracked = generated("meteor_brick_cracked", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, "blocks/meteor_brick_cracked");
	public static final DeferredBlock<Block> meteor_brick_chiseled = generated("meteor_brick_chiseled", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, "blocks/meteor_brick_chiseled");
	public static final DeferredBlock<Block> brick_jungle = generated("brick_jungle", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, "blocks/brick_jungle");
	public static final DeferredBlock<Block> brick_jungle_cracked = generated("brick_jungle_cracked", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, "blocks/brick_jungle_cracked");
	public static final DeferredBlock<Block> brick_jungle_lava = generated("brick_jungle_lava", Block::new, props(Mat.ROCK, 15.0F, 360.0F).lightLevel(s -> (int) (5F/15F * 15)), NtmTab.BLOCKS, "blocks/brick_jungle_lava");
	public static final DeferredBlock<BlockOre> brick_jungle_ooze = generated("brick_jungle_ooze", BlockOre::new, props(Mat.ROCK, 15.0F, 360.0F).lightLevel(s -> (int) (5F/15F * 15)), NtmTab.BLOCKS, "blocks/brick_jungle_ooze");
	public static final DeferredBlock<BlockOre> brick_jungle_mystic = generated("brick_jungle_mystic", BlockOre::new, props(Mat.ROCK, 15.0F, 360.0F).lightLevel(s -> (int) (5F/15F * 15)), NtmTab.BLOCKS, "blocks/brick_jungle_mystic");
	public static final DeferredBlock<BlockFallingNT> moon_turf = generated("moon_turf", BlockFallingNT::new, props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.BLOCKS, "blocks/moon_turf");
	public static final DeferredBlock<BlockOre> waste_planks = generated("waste_planks", BlockOre::new, props(Mat.WOOD, 0.5F, 2.5F).sound(SoundType.WOOD), NtmTab.BLOCKS, "blocks/waste_planks");
	public static final DeferredBlock<BlockOre> frozen_dirt = generated("frozen_dirt", BlockOre::new, props(Mat.GROUND, 0.5F, 2.5F).sound(SoundType.GLASS), NtmTab.BLOCKS, "blocks/frozen_dirt");
	public static final DeferredBlock<BlockOre> frozen_planks = generated("frozen_planks", BlockOre::new, props(Mat.WOOD, 0.5F, 2.5F).sound(SoundType.GLASS), NtmTab.BLOCKS, "blocks/frozen_planks");
	public static final DeferredBlock<BlockFallingNT> dirt_dead = generated("dirt_dead", BlockFallingNT::new, props(Mat.GROUND, 0.5F, LEGACY_NONE).sound(SoundType.GRAVEL), NtmTab.BLOCKS, "blocks/dirt_dead");
	public static final DeferredBlock<BlockFallingNT> dirt_oily = generated("dirt_oily", BlockFallingNT::new, props(Mat.GROUND, 0.5F, LEGACY_NONE).sound(SoundType.GRAVEL), NtmTab.BLOCKS, "blocks/dirt_oily");
	public static final DeferredBlock<BlockFallingNT> sand_dirty = generated("sand_dirty", BlockFallingNT::new, props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.BLOCKS, "blocks/sand_dirty");
	public static final DeferredBlock<BlockFallingNT> sand_dirty_red = generated("sand_dirty_red", BlockFallingNT::new, props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.BLOCKS, "blocks/sand_dirty_red");
	public static final DeferredBlock<BlockFallingNT> stone_cracked = generated("stone_cracked", BlockFallingNT::new, props(Mat.ROCK, 5.0F, LEGACY_NONE).sound(SoundType.STONE), NtmTab.BLOCKS, "blocks/stone_cracked");
	public static final DeferredBlock<Block> tektite = generated("tektite", Block::new, props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.BLOCKS, "blocks/tektite");
	public static final DeferredBlock<Block> ore_tektite_osmiridium = generated("ore_tektite_osmiridium", Block::new, props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.BLOCKS, "blocks/ore_tektite_osmiridium");
	public static final DeferredBlock<Block> seal_frame = generated("seal_frame", Block::new, props(Mat.IRON, 10.0F, 100.0F), NtmTab.MACHINE, "blocks/seal_frame");
	public static final DeferredBlock<Block> struct_launcher = generated("struct_launcher", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MISSILE, "blocks/struct_launcher");
	public static final DeferredBlock<Block> struct_scaffold = generated("struct_scaffold", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MISSILE, "blocks/struct_scaffold");
	public static final DeferredBlock<Block> crystal_hardened = generated("crystal_hardened", Block::new, props(Mat.IRON, 15.0F, Float.POSITIVE_INFINITY), null, "blocks/crystal_hardened");
	public static final DeferredBlock<Block> pink_planks = generated("pink_planks", Block::new, props(Mat.WOOD, 0.0F, LEGACY_NONE).sound(SoundType.WOOD), null, "blocks/pink_planks");
	// END GENERATED

	/** Registers a block together with its BlockItem and adds it to the given creative tab (null for none). */
	public static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties props, NtmTab tab) {
		DeferredBlock<T> block = BLOCKS.registerBlock(name, factory, props);
		ModItems.ITEMS.registerSimpleBlockItem(block);
		if(tab != null) tab.add(block);
		return block;
	}

	public enum Gen { BEACON }

	/** Registration for generated blocks, which are all full cubes with one texture */
	private static <T extends Block> DeferredBlock<T> generated(String name, Function<BlockBehaviour.Properties, T> factory, PropsWithTool props, NtmTab tab, String texture, Gen... flags) {
		DeferredBlock<T> block = register(name, factory, props.props, tab);
		CUBE_MODELS.put(block, texture);
		if(props.tool != null) TOOLS.put(block, props.tool);
		for(Gen flag : flags) if(flag == Gen.BEACON) BEACON_BASES.add(block);
		return block;
	}

	public enum Tool { PICKAXE, AXE, SHOVEL }

	/** The 1.7.10 materials that are used, with their modern map color, tool and whether that tool is needed for drops */
	public enum Mat {
		ROCK(MapColor.STONE, Tool.PICKAXE, true),
		IRON(MapColor.METAL, Tool.PICKAXE, true),
		GROUND(MapColor.DIRT, Tool.SHOVEL, false),
		SAND(MapColor.SAND, Tool.SHOVEL, false),
		WOOD(MapColor.WOOD, Tool.AXE, false),
		CLOTH(MapColor.WOOL, null, false),
		SNOW(MapColor.SNOW, Tool.SHOVEL, false);

		public final MapColor color;
		public final Tool tool;
		public final boolean needsTool;

		Mat(MapColor color, Tool tool, boolean needsTool) {
			this.color = color;
			this.tool = tool;
			this.needsTool = needsTool;
		}
	}

	/** Block properties that also remember the harvest tool (for the generated tags) */
	public static class PropsWithTool {
		public final BlockBehaviour.Properties props;
		public final Tool tool;

		PropsWithTool(BlockBehaviour.Properties props, Tool tool) {
			this.props = props;
			this.tool = tool;
		}

		public PropsWithTool sound(SoundType sound) { props.sound(sound); return this; }
		public PropsWithTool lightLevel(java.util.function.ToIntFunction<net.minecraft.world.level.block.state.BlockState> light) { props.lightLevel(light); return this; }
		public PropsWithTool strength(float hardness, float resistance) { props.strength(hardness, resistance); return this; }
		public PropsWithTool noOcclusion() { props.noOcclusion(); return this; }
	}

	/**
	 * 1.7.10's setResistance(r) resulted in an explosion resistance of r * 3 / 5,
	 * modern versions use the value directly. Declarations keep the original values.
	 */
	public static float legacyResistance(float resistance) {
		return resistance * 0.6F;
	}

	/**
	 * Properties from the original's material, hardness and resistance.
	 * The default sound is stone, like every 1.7.10 block that didn't set one.
	 */
	public static PropsWithTool props(Mat mat, float hardness, float resistance) {
		BlockBehaviour.Properties props = BlockBehaviour.Properties.of().mapColor(mat.color).sound(SoundType.STONE);
		props.strength(hardness, resistance == LEGACY_NONE ? hardness : legacyResistance(resistance));
		if(mat.needsTool) props.requiresCorrectToolForDrops();
		return new PropsWithTool(props, mat.tool);
	}

	private static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> factory, PropsWithTool props, NtmTab tab) {
		DeferredBlock<T> block = register(name, factory, props.props, tab);
		if(props.tool != null) TOOLS.put(block, props.tool);
		return block;
	}
}
