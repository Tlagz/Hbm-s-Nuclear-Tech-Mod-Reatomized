package com.hbm.blocks;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.hbm.blocks.generic.BlockFallingNT;
import com.hbm.blocks.generic.BlockHazard;
import com.hbm.blocks.generic.BlockNTMGlass;
import com.hbm.blocks.generic.BlockNoSpawn;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.blocks.generic.BlockOutgas;
import com.hbm.blocks.machine.BlockFluidBarrel;
import com.hbm.blocks.machine.MachineCapacitor;
import com.hbm.blocks.machine.HeaterFirebox;
import com.hbm.blocks.machine.MachineDiesel;
import com.hbm.blocks.machine.NTMAnvil;
import com.hbm.blocks.machine.MachinePress;
import com.hbm.blocks.machine.MachineRefinery;
import com.hbm.blocks.machine.MachineHeatBoiler;
import com.hbm.blocks.machine.MachineOilWell;
import com.hbm.blocks.network.FluidDuctStandard;
import com.hbm.blocks.machine.MachineWoodBurner;
import com.hbm.blocks.machine.MachineCapacitorBus;
import com.hbm.blocks.machine.MachineElectricFurnace;
import com.hbm.blocks.network.BlockCable;
import com.hbm.creativetabs.NtmTab;
import com.hbm.blocks.BlockEnums.*;
import com.hbm.blocks.generic.BlockConcreteColoredExt.EnumConcreteType;
import com.hbm.blocks.generic.BlockNTMSand.EnumSandType;
import com.hbm.items.ItemEnums.EnumCokeType;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class ModBlocks {

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RefStrings.MODID);

	/** How datagen builds the model of generated blocks */
	public static final Map<DeferredBlock<?>, BlockModel> MODELS = new LinkedHashMap<>();
	/** All multiblocks, they share the proxy tile entity type */
	public static final java.util.List<DeferredBlock<? extends BlockDummyable>> DUMMYABLES = new java.util.ArrayList<>();
	/** Blocks rendered by a tile entity renderer: block -> particle texture, item uses the NTM item renderer */
	public static final Map<DeferredBlock<?>, String> TILE_RENDERED = new LinkedHashMap<>();
	/** NTM anvils: block -> side and top texture, used by datagen */
	public static final Map<DeferredBlock<NTMAnvil>, String[]> ANVILS = new LinkedHashMap<>();
	/** Which tool mines the block, used by datagen for the mineable tags */
	public static final Map<DeferredBlock<?>, Tool> TOOLS = new LinkedHashMap<>();
	/** Blocks that can be used for beacon bases */
	public static final Set<DeferredBlock<?>> BEACON_BASES = new java.util.LinkedHashSet<>();

	/** Marks "setResistance was never called", the resistance then follows the hardness like in 1.7.10 */
	public static final float LEGACY_NONE = -1F;

	/// HAND-PORTED ///
	public static final DeferredBlock<BlockCable> red_cable = register("red_cable", BlockCable::new, props(Mat.IRON, 5.0F, 10.0F).noOcclusion(), NtmTab.MACHINE);
	public static final DeferredBlock<FluidDuctStandard> fluid_duct_neo = registerWithItem("fluid_duct_neo", FluidDuctStandard::new, props(Mat.IRON, 5.0F, 10.0F).noOcclusion().sound(ModSoundTypes.PIPE), NtmTab.MACHINE,
			block -> new com.hbm.items.block.ItemBlockStyled(block, FluidDuctStandard.STYLE, new net.minecraft.world.item.Item.Properties()));
	public static final DeferredBlock<BlockFluidBarrel> barrel_plastic = register("barrel_plastic", p -> new BlockFluidBarrel(p, 12000), props(Mat.IRON, 2.0F, 5.0F).sound(SoundType.STONE), NtmTab.MACHINE);
	public static final DeferredBlock<BlockFluidBarrel> barrel_corroded = register("barrel_corroded", p -> new BlockFluidBarrel(p, 6000), props(Mat.IRON, 2.0F, 5.0F).sound(SoundType.METAL), null);
	public static final DeferredBlock<BlockFluidBarrel> barrel_steel = register("barrel_steel", p -> new BlockFluidBarrel(p, 16000), props(Mat.IRON, 2.0F, 5.0F).sound(SoundType.METAL), NtmTab.MACHINE);
	public static final DeferredBlock<BlockFluidBarrel> barrel_tcalloy = register("barrel_tcalloy", p -> new BlockFluidBarrel(p, 24000), props(Mat.IRON, 2.0F, 5.0F).sound(SoundType.METAL), NtmTab.MACHINE);
	public static final DeferredBlock<BlockFluidBarrel> barrel_antimatter = register("barrel_antimatter", p -> new BlockFluidBarrel(p, 16000), props(Mat.IRON, 2.0F, 5.0F).sound(SoundType.METAL), NtmTab.MACHINE);
	/** All fluid barrels, the texture is textures/blocks/[name].png */
	public static final java.util.List<DeferredBlock<BlockFluidBarrel>> BARRELS = java.util.List.of(barrel_plastic, barrel_corroded, barrel_steel, barrel_tcalloy, barrel_antimatter);
	public static final DeferredBlock<MachineCapacitorBus> capacitor_bus = register("capacitor_bus", MachineCapacitorBus::new, props(Mat.IRON, 5.0F, 10.0F), null);
	public static final DeferredBlock<MachineCapacitor> capacitor_copper = register("capacitor_copper", p -> new MachineCapacitor(p, 1_000_000L, "copper"), props(Mat.IRON, 5.0F, 10.0F).noOcclusion(), NtmTab.MACHINE);
	public static final DeferredBlock<MachineCapacitor> capacitor_gold = register("capacitor_gold", p -> new MachineCapacitor(p, 5_000_000L, "gold"), props(Mat.IRON, 5.0F, 10.0F).noOcclusion(), null);
	public static final DeferredBlock<MachineCapacitor> capacitor_niobium = register("capacitor_niobium", p -> new MachineCapacitor(p, 25_000_000L, "niobium"), props(Mat.IRON, 5.0F, 10.0F).noOcclusion(), null);
	public static final DeferredBlock<MachineCapacitor> capacitor_tantalium = register("capacitor_tantalium", p -> new MachineCapacitor(p, 150_000_000L, "tantalium"), props(Mat.IRON, 5.0F, 10.0F).noOcclusion(), null);
	public static final DeferredBlock<MachineCapacitor> capacitor_schrabidate = register("capacitor_schrabidate", p -> new MachineCapacitor(p, 50_000_000_000L, "schrabidate"), props(Mat.IRON, 5.0F, 10.0F).noOcclusion(), null);
	public static final DeferredBlock<MachineWoodBurner> machine_wood_burner = dummyable("machine_wood_burner", MachineWoodBurner::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<MachineDiesel> machine_diesel = tileRendered("machine_diesel", MachineDiesel::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<MachineOilWell> machine_well = dummyable("machine_well", MachineOilWell::new, props(Mat.IRON, 5.0F, 20.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachinePumpjack> machine_pumpjack = dummyable("machine_pumpjack", com.hbm.blocks.machine.MachinePumpjack::new, props(Mat.IRON, 5.0F, 20.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineFrackingTower> machine_fracking_tower = dummyable("machine_fracking_tower", com.hbm.blocks.machine.MachineFrackingTower::new, props(Mat.IRON, 5.0F, 20.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineGasFlare> machine_flare = dummyable("machine_flare", com.hbm.blocks.machine.MachineGasFlare::new, props(Mat.IRON, 5.0F, 100.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineOilProcessor.VacuumDistill> machine_vacuum_distill = dummyable("machine_vacuum_distill", com.hbm.blocks.machine.MachineOilProcessor.VacuumDistill::new, props(Mat.IRON, 5.0F, 20.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineOilProcessor.CatalyticReformer> machine_catalytic_reformer = dummyable("machine_catalytic_reformer", com.hbm.blocks.machine.MachineOilProcessor.CatalyticReformer::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineOilProcessor.Hydrotreater> machine_hydrotreater = dummyable("machine_hydrotreater", com.hbm.blocks.machine.MachineOilProcessor.Hydrotreater::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineLiquefactor> machine_liquefactor = dummyable("machine_liquefactor", com.hbm.blocks.machine.MachineLiquefactor::liquefactor, props(Mat.IRON, 10.0F, 20.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineLiquefactor> machine_solidifier = dummyable("machine_solidifier", com.hbm.blocks.machine.MachineLiquefactor::solidifier, props(Mat.IRON, 10.0F, 20.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineCoker> machine_coker = dummyable("machine_coker", com.hbm.blocks.machine.MachineCoker::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachinePyroOven> machine_pyrooven = dummyable("machine_pyrooven", com.hbm.blocks.machine.MachinePyroOven::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineElectrolyser> machine_electrolyser = dummyable("machine_electrolyser", com.hbm.blocks.machine.MachineElectrolyser::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineGasCent> machine_gascent = dummyable("machine_gascent", com.hbm.blocks.machine.MachineGasCent::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineTurbineGas> machine_turbinegas = dummyable("machine_turbinegas", com.hbm.blocks.machine.MachineTurbineGas::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.network.MachineBatterySocket> machine_battery_socket = dummyable("machine_battery_socket", com.hbm.blocks.network.MachineBatterySocket::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.network.MachineBatteryREDD> machine_battery_redd = dummyable("machine_battery_redd", com.hbm.blocks.network.MachineBatteryREDD::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineSolderingStation> machine_soldering_station = dummyable("machine_soldering_station", com.hbm.blocks.machine.MachineSolderingStation::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineCompressor.Tower> machine_compressor = dummyable("machine_compressor", com.hbm.blocks.machine.MachineCompressor.Tower::new, props(Mat.IRON, 10.0F, 20.0F), NtmTab.MACHINE, "blocks/block_steel_machine");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineCompressor.Compact> machine_compressor_compact = dummyable("machine_compressor_compact", com.hbm.blocks.machine.MachineCompressor.Compact::new, props(Mat.IRON, 10.0F, 20.0F), NtmTab.MACHINE, "blocks/block_steel_machine");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineMixer> machine_mixer = dummyable("machine_mixer", com.hbm.blocks.machine.MachineMixer::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.FurnaceIron> furnace_iron = dummyable("furnace_iron", com.hbm.blocks.machine.FurnaceIron::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_aluminium");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineBrickFurnace> machine_furnace_brick_off = register("machine_furnace_brick_off", com.hbm.blocks.machine.MachineBrickFurnace::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE);
	public static final DeferredBlock<com.hbm.blocks.machine.FurnaceCombination> furnace_combination = dummyable("furnace_combination", com.hbm.blocks.machine.FurnaceCombination::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/brick_light_alt");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineRotaryFurnace> machine_rotary_furnace = dummyable("machine_rotary_furnace", com.hbm.blocks.machine.MachineRotaryFurnace::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineStrandCaster> machine_strand_caster = dummyable("machine_strand_caster", com.hbm.blocks.machine.MachineStrandCaster::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockStorageCrate> crate_iron = generated("crate_iron", p -> new com.hbm.blocks.generic.BlockStorageCrate(p, com.hbm.blocks.generic.BlockStorageCrate.CrateType.IRON), props(Mat.IRON, 5.0F, 10.0F).sound(net.minecraft.world.level.block.SoundType.METAL), NtmTab.MACHINE, BlockModel.column("blocks/crate_iron_side", "blocks/crate_iron_top"));
	public static final DeferredBlock<com.hbm.blocks.generic.BlockStorageCrate> crate_steel = generated("crate_steel", p -> new com.hbm.blocks.generic.BlockStorageCrate(p, com.hbm.blocks.generic.BlockStorageCrate.CrateType.STEEL), props(Mat.IRON, 5.0F, 10.0F).sound(net.minecraft.world.level.block.SoundType.METAL), NtmTab.MACHINE, BlockModel.column("blocks/crate_steel_side", "blocks/crate_steel_top"));
	public static final DeferredBlock<com.hbm.blocks.generic.BlockStorageCrate> crate_desh = generated("crate_desh", p -> new com.hbm.blocks.generic.BlockStorageCrate(p, com.hbm.blocks.generic.BlockStorageCrate.CrateType.DESH), props(Mat.IRON, 5.0F, 10.0F).sound(net.minecraft.world.level.block.SoundType.METAL), NtmTab.MACHINE, BlockModel.column("blocks/crate_desh_side", "blocks/crate_desh_top"));
	public static final DeferredBlock<com.hbm.blocks.generic.BlockStorageCrate> crate_tungsten = generated("crate_tungsten", p -> new com.hbm.blocks.generic.BlockStorageCrate(p, com.hbm.blocks.generic.BlockStorageCrate.CrateType.TUNGSTEN), props(Mat.IRON, 7.5F, 300.0F).sound(net.minecraft.world.level.block.SoundType.METAL), NtmTab.MACHINE, BlockModel.column("blocks/crate_tungsten_side", "blocks/crate_tungsten_top"));
	public static final DeferredBlock<com.hbm.blocks.generic.BlockStorageCrate> safe = register("safe", p -> new com.hbm.blocks.generic.BlockStorageCrate(p, com.hbm.blocks.generic.BlockStorageCrate.CrateType.SAFE), props(Mat.IRON, 7.5F, 10000.0F).sound(net.minecraft.world.level.block.SoundType.METAL), NtmTab.MACHINE);
	public static final java.util.List<DeferredBlock<com.hbm.blocks.generic.BlockStorageCrate>> CRATES = java.util.List.of(crate_iron, crate_steel, crate_desh, crate_tungsten, safe);
	/** BlockNoDrop in the original, left behind by oil drills */
	public static final DeferredBlock<Block> oil_pipe = generated("oil_pipe", Block::new, props(Mat.IRON, 5.0F, 10.0F), null, BlockModel.cube("blocks/oil_pipe"));
	public static final DeferredBlock<HeaterFirebox> heater_firebox = dummyable("heater_firebox", HeaterFirebox::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<MachineHeatBoiler> machine_boiler = dummyable("machine_boiler", MachineHeatBoiler::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_copper");
	public static final DeferredBlock<MachineRefinery> machine_refinery = dummyable("machine_refinery", MachineRefinery::new, props(Mat.IRON, 5.0F, 20.0F), NtmTab.MACHINE, "blocks/machine_refinery");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineFractionTower> machine_fraction_tower = dummyable("machine_fraction_tower", com.hbm.blocks.machine.MachineFractionTower::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.FractionSpacer> fraction_spacer = dummyable("fraction_spacer", com.hbm.blocks.machine.FractionSpacer::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineCatalyticCracker> machine_catalytic_cracker = dummyable("machine_catalytic_cracker", com.hbm.blocks.machine.MachineCatalyticCracker::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineAssemblyMachine> machine_assembly_machine = dummyable("machine_assembly_machine", com.hbm.blocks.machine.MachineAssemblyMachine::new, props(Mat.IRON, 5.0F, 30.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineChemicalPlant> machine_chemical_plant = dummyable("machine_chemical_plant", com.hbm.blocks.machine.MachineChemicalPlant::new, props(Mat.IRON, 5.0F, 30.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineChemicalFactory> machine_chemical_factory = dummyable("machine_chemical_factory", com.hbm.blocks.machine.MachineChemicalFactory::new, props(Mat.IRON, 5.0F, 30.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.HeaterOven> heater_oven = dummyable("heater_oven", com.hbm.blocks.machine.HeaterOven::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/brick_fire");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineStirling> machine_stirling = dummyable("machine_stirling", p -> new com.hbm.blocks.machine.MachineStirling(p, 0), props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineStirling> machine_stirling_steel = dummyable("machine_stirling_steel", p -> new com.hbm.blocks.machine.MachineStirling(p, 1), props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineStirling> machine_stirling_creative = dummyable("machine_stirling_creative", p -> new com.hbm.blocks.machine.MachineStirling(p, 2), props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.HeaterElectric> heater_electric = dummyable("heater_electric", com.hbm.blocks.machine.HeaterElectric::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.HeaterOilburner> heater_oilburner = dummyable("heater_oilburner", com.hbm.blocks.machine.HeaterOilburner::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.HeaterHeatex> heater_heatex = dummyable("heater_heatex", com.hbm.blocks.machine.HeaterHeatex::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineHeatBoilerIndustrial> machine_industrial_boiler = dummyable("machine_industrial_boiler", com.hbm.blocks.machine.MachineHeatBoilerIndustrial::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachinePump> pump_steam = dummyable("pump_steam", p -> new com.hbm.blocks.machine.MachinePump(p, 0), props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_copper");
	public static final DeferredBlock<com.hbm.blocks.machine.MachinePump> pump_electric = dummyable("pump_electric", p -> new com.hbm.blocks.machine.MachinePump(p, 1), props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineCombustionEngine> machine_combustion_engine = dummyable("machine_combustion_engine", com.hbm.blocks.machine.MachineCombustionEngine::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineCentrifuge> machine_centrifuge = dummyable("machine_centrifuge", com.hbm.blocks.machine.MachineCentrifuge::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineCrystallizer> machine_crystallizer = dummyable("machine_crystallizer", com.hbm.blocks.machine.MachineCrystallizer::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineFluidTank> machine_fluidtank = dummyable("machine_fluidtank", com.hbm.blocks.machine.MachineFluidTank::new, props(Mat.IRON, 5.0F, 20.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineBigAssTank9000> machine_bat9000 = dummyable("machine_bat9000", com.hbm.blocks.machine.MachineBigAssTank9000::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineOrbus> machine_orbus = dummyable("machine_orbus", com.hbm.blocks.machine.MachineOrbus::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.FurnaceSteel> furnace_steel = dummyable("furnace_steel", com.hbm.blocks.machine.FurnaceSteel::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineSteamEngine> machine_steam_engine = dummyable("machine_steam_engine", com.hbm.blocks.machine.MachineSteamEngine::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineCondenser> machine_condenser = generated("machine_condenser", com.hbm.blocks.machine.MachineCondenser::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, BlockModel.cube("blocks/condenser"));
	public static final DeferredBlock<com.hbm.blocks.machine.MachineCondenserPowered> machine_condenser_powered = dummyable("machine_condenser_powered", com.hbm.blocks.machine.MachineCondenserPowered::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel_machine");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineTowerSmall> machine_tower_small = dummyable("machine_tower_small", com.hbm.blocks.machine.MachineTowerSmall::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/brick_concrete");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineTowerLarge> machine_tower_large = dummyable("machine_tower_large", com.hbm.blocks.machine.MachineTowerLarge::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/concrete");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineIndustrialTurbine> machine_industrial_turbine = dummyable("machine_industrial_turbine", com.hbm.blocks.machine.MachineIndustrialTurbine::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineChungus> machine_chungus = dummyable("machine_chungus", com.hbm.blocks.machine.MachineChungus::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineCrucible> machine_crucible = dummyable("machine_crucible", com.hbm.blocks.machine.MachineCrucible::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/brick_fire");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineArcFurnaceLarge> machine_arc_furnace = dummyable("machine_arc_furnace", com.hbm.blocks.machine.MachineArcFurnaceLarge::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineArcWelder> machine_arc_welder = dummyable("machine_arc_welder", com.hbm.blocks.machine.MachineArcWelder::new, props(Mat.IRON, 5.0F, 30.0F), NtmTab.MACHINE, "blocks/block_steel");
	public static final DeferredBlock<com.hbm.blocks.machine.MachineBlastFurnace> machine_blast_furnace = dummyable("machine_blast_furnace", com.hbm.blocks.machine.MachineBlastFurnace::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/brick_fire");
	public static final DeferredBlock<MachinePress> machine_press = dummyable("machine_press", MachinePress::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, "blocks/machine_press");
	public static final DeferredBlock<NTMAnvil> anvil_iron = anvil("anvil_iron", NTMAnvil.TIER_IRON, "blocks/anvil_iron", "blocks/anvil_iron");
	public static final DeferredBlock<NTMAnvil> anvil_lead = anvil("anvil_lead", NTMAnvil.TIER_IRON, "blocks/anvil_lead", "blocks/anvil_lead");
	public static final DeferredBlock<NTMAnvil> anvil_steel = anvil("anvil_steel", NTMAnvil.TIER_STEEL, "blocks/anvil_steel", "blocks/anvil_steel");
	public static final DeferredBlock<NTMAnvil> anvil_desh = anvil("anvil_desh", NTMAnvil.TIER_OIL, "blocks/anvil_desh", "blocks/anvil_desh");
	public static final DeferredBlock<NTMAnvil> anvil_ferrouranium = anvil("anvil_ferrouranium", NTMAnvil.TIER_NUCLEAR, "blocks/anvil_ferrouranium", "blocks/anvil_ferrouranium");
	public static final DeferredBlock<NTMAnvil> anvil_saturnite = anvil("anvil_saturnite", NTMAnvil.TIER_RBMK, "blocks/anvil_saturnite", "blocks/anvil_saturnite");
	public static final DeferredBlock<NTMAnvil> anvil_bismuth_bronze = anvil("anvil_bismuth_bronze", NTMAnvil.TIER_RBMK, "blocks/anvil_bismuth_bronze", "blocks/anvil_bismuth_bronze");
	public static final DeferredBlock<NTMAnvil> anvil_arsenic_bronze = anvil("anvil_arsenic_bronze", NTMAnvil.TIER_RBMK, "blocks/anvil_arsenic_bronze", "blocks/anvil_arsenic_bronze");
	public static final DeferredBlock<NTMAnvil> anvil_schrabidate = anvil("anvil_schrabidate", NTMAnvil.TIER_FUSION, "blocks/anvil_schrabidate", "blocks/anvil_schrabidate");
	public static final DeferredBlock<NTMAnvil> anvil_dnt = anvil("anvil_dnt", NTMAnvil.TIER_PARTICLE, "blocks/anvil_dnt", "blocks/anvil_dnt");
	public static final DeferredBlock<NTMAnvil> anvil_osmiridium = anvil("anvil_osmiridium", NTMAnvil.TIER_GERALD, "blocks/anvil_osmiridium", "blocks/anvil_osmiridium");
	public static final DeferredBlock<NTMAnvil> anvil_murky = anvil("anvil_murky", 1916169, "blocks/anvil_steel", "blocks/anvil_murky");
	/// SCAFFOLDS: the original's color metadata as separate blocks, block -> texture for datagen ///
	public static final Map<DeferredBlock<com.hbm.blocks.generic.BlockScaffold>, String> SCAFFOLDS = new LinkedHashMap<>();
	public static final DeferredBlock<com.hbm.blocks.generic.BlockScaffold> steel_scaffold = scaffold("steel_scaffold", "scaffold_steel");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockScaffold> steel_scaffold_red = scaffold("steel_scaffold_red", "scaffold_red");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockScaffold> steel_scaffold_white = scaffold("steel_scaffold_white", "scaffold_white");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockScaffold> steel_scaffold_yellow = scaffold("steel_scaffold_yellow", "scaffold_yellow");

	/// DECO: steel walls, beams, grates, pipes, fences ///
	public static final DeferredBlock<com.hbm.blocks.generic.DecoBlock> steel_wall = register("steel_wall", p -> new com.hbm.blocks.generic.DecoBlock(p, com.hbm.blocks.generic.DecoBlock.Type.WALL), props(Mat.IRON, 5.0F, 15.0F), NtmTab.BLOCKS);
	public static final DeferredBlock<com.hbm.blocks.generic.DecoBlock> steel_corner = register("steel_corner", p -> new com.hbm.blocks.generic.DecoBlock(p, com.hbm.blocks.generic.DecoBlock.Type.CORNER), props(Mat.IRON, 15.0F, 15.0F), NtmTab.BLOCKS);
	public static final DeferredBlock<com.hbm.blocks.generic.DecoBlock> steel_roof = register("steel_roof", p -> new com.hbm.blocks.generic.DecoBlock(p, com.hbm.blocks.generic.DecoBlock.Type.ROOF), props(Mat.IRON, 5.0F, 15.0F), NtmTab.BLOCKS);
	public static final DeferredBlock<com.hbm.blocks.generic.DecoBlock> steel_beam = register("steel_beam", p -> new com.hbm.blocks.generic.DecoBlock(p, com.hbm.blocks.generic.DecoBlock.Type.BEAM), props(Mat.IRON, 5.0F, 15.0F), NtmTab.BLOCKS);
	public static final DeferredBlock<com.hbm.blocks.generic.BlockGrate> steel_grate = register("steel_grate", p -> new com.hbm.blocks.generic.BlockGrate(p, false), props(Mat.IRON, 2.0F, 5.0F).sound(ModSoundTypes.GRATE), NtmTab.BLOCKS);
	public static final DeferredBlock<com.hbm.blocks.generic.BlockGrate> steel_grate_wide = register("steel_grate_wide", p -> new com.hbm.blocks.generic.BlockGrate(p, true), props(Mat.IRON, 2.0F, 5.0F).sound(ModSoundTypes.GRATE), NtmTab.BLOCKS);

	/** Decorative pipes, used by datagen */
	public static final java.util.List<DeferredBlock<com.hbm.blocks.generic.BlockPipe>> PIPES = new java.util.ArrayList<>();
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe = pipe("deco_pipe", com.hbm.blocks.generic.BlockPipe.Style.PLAIN, "");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_rusted = pipe("deco_pipe_rusted", com.hbm.blocks.generic.BlockPipe.Style.PLAIN, "_rusty");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_green = pipe("deco_pipe_green", com.hbm.blocks.generic.BlockPipe.Style.PLAIN, "_green");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_green_rusted = pipe("deco_pipe_green_rusted", com.hbm.blocks.generic.BlockPipe.Style.PLAIN, "_green_rusty");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_red = pipe("deco_pipe_red", com.hbm.blocks.generic.BlockPipe.Style.PLAIN, "_red");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_marked = pipe("deco_pipe_marked", com.hbm.blocks.generic.BlockPipe.Style.PLAIN, "_marked");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_rim = pipe("deco_pipe_rim", com.hbm.blocks.generic.BlockPipe.Style.RIM, "");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_rim_rusted = pipe("deco_pipe_rim_rusted", com.hbm.blocks.generic.BlockPipe.Style.RIM, "_rusty");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_rim_green = pipe("deco_pipe_rim_green", com.hbm.blocks.generic.BlockPipe.Style.RIM, "_green");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_rim_green_rusted = pipe("deco_pipe_rim_green_rusted", com.hbm.blocks.generic.BlockPipe.Style.RIM, "_green_rusty");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_rim_red = pipe("deco_pipe_rim_red", com.hbm.blocks.generic.BlockPipe.Style.RIM, "_red");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_rim_marked = pipe("deco_pipe_rim_marked", com.hbm.blocks.generic.BlockPipe.Style.RIM, "_marked");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_framed = pipe("deco_pipe_framed", com.hbm.blocks.generic.BlockPipe.Style.FRAMED, "");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_framed_rusted = pipe("deco_pipe_framed_rusted", com.hbm.blocks.generic.BlockPipe.Style.FRAMED, "_rusty");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_framed_green = pipe("deco_pipe_framed_green", com.hbm.blocks.generic.BlockPipe.Style.FRAMED, "_green");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_framed_green_rusted = pipe("deco_pipe_framed_green_rusted", com.hbm.blocks.generic.BlockPipe.Style.FRAMED, "_green_rusty");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_framed_red = pipe("deco_pipe_framed_red", com.hbm.blocks.generic.BlockPipe.Style.FRAMED, "_red");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_framed_marked = pipe("deco_pipe_framed_marked", com.hbm.blocks.generic.BlockPipe.Style.FRAMED, "_marked");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_quad = pipe("deco_pipe_quad", com.hbm.blocks.generic.BlockPipe.Style.QUAD, "");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_quad_rusted = pipe("deco_pipe_quad_rusted", com.hbm.blocks.generic.BlockPipe.Style.QUAD, "_rusty");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_quad_green = pipe("deco_pipe_quad_green", com.hbm.blocks.generic.BlockPipe.Style.QUAD, "_green");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_quad_green_rusted = pipe("deco_pipe_quad_green_rusted", com.hbm.blocks.generic.BlockPipe.Style.QUAD, "_green_rusty");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_quad_red = pipe("deco_pipe_quad_red", com.hbm.blocks.generic.BlockPipe.Style.QUAD, "_red");
	public static final DeferredBlock<com.hbm.blocks.generic.BlockPipe> deco_pipe_quad_marked = pipe("deco_pipe_quad_marked", com.hbm.blocks.generic.BlockPipe.Style.QUAD, "_marked");

	public static final DeferredBlock<com.hbm.blocks.generic.BlockMetalFence> fence_metal = register("fence_metal", p -> new com.hbm.blocks.generic.BlockMetalFence(p, false), props(Mat.IRON, 15.0F, 0.25F), NtmTab.MACHINE);
	public static final DeferredBlock<com.hbm.blocks.generic.BlockMetalFence> fence_metal_post = register("fence_metal_post", p -> new com.hbm.blocks.generic.BlockMetalFence(p, true), props(Mat.IRON, 15.0F, 0.25F), NtmTab.MACHINE);

	/// FOUNDRY: models by datagen, the molten contents by RenderFoundry ///
	public static final DeferredBlock<com.hbm.blocks.machine.FoundryMold> foundry_mold = register("foundry_mold", com.hbm.blocks.machine.FoundryMold::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.MACHINE);
	public static final DeferredBlock<com.hbm.blocks.machine.FoundryBasin> foundry_basin = register("foundry_basin", com.hbm.blocks.machine.FoundryBasin::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.MACHINE);
	public static final DeferredBlock<com.hbm.blocks.machine.FoundryChannel> foundry_channel = register("foundry_channel", com.hbm.blocks.machine.FoundryChannel::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.MACHINE);
	public static final DeferredBlock<com.hbm.blocks.machine.FoundryOutlet> foundry_outlet = register("foundry_outlet", com.hbm.blocks.machine.FoundryOutlet::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.MACHINE);

	public static final DeferredBlock<com.hbm.blocks.machine.MachineShredder> machine_shredder = register("machine_shredder", com.hbm.blocks.machine.MachineShredder::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE);
	public static final DeferredBlock<MachineElectricFurnace> machine_electric_furnace_off = register("machine_electric_furnace_off", MachineElectricFurnace::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE);

	/// GENERATED from the original's declarations by tools/gen_content.py, don't edit by hand ///
	// BEGIN GENERATED
	public static final DeferredBlock<Block> structure_anchor = generated("structure_anchor", Block::new, props(Mat.IRON, 2.5F, 10.0F), null, BlockModel.cube("blocks/structure_anchor"));
	public static final DeferredBlock<BlockOutgas> ore_uranium = generated("ore_uranium", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_uranium"));
	public static final DeferredBlock<BlockOutgas> ore_uranium_scorched = generated("ore_uranium_scorched", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_uranium_scorched"));
	public static final DeferredBlock<Block> ore_thorium = generated("ore_thorium", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_thorium"));
	public static final DeferredBlock<Block> ore_titanium = generated("ore_titanium", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_titanium"));
	public static final DeferredBlock<BlockOre> ore_sulfur = generated("ore_sulfur", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_sulfur"));
	public static final DeferredBlock<BlockOre> ore_niter = generated("ore_niter", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_niter"));
	public static final DeferredBlock<Block> ore_copper = generated("ore_copper", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_copper"));
	public static final DeferredBlock<Block> ore_tungsten = generated("ore_tungsten", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_tungsten"));
	public static final DeferredBlock<Block> ore_aluminium = generated("ore_aluminium", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_aluminium"));
	public static final DeferredBlock<BlockOre> ore_fluorite = generated("ore_fluorite", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_fluorite"));
	public static final DeferredBlock<Block> ore_beryllium = generated("ore_beryllium", Block::new, props(Mat.ROCK, 5.0F, 15.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_beryllium"));
	public static final DeferredBlock<Block> ore_lead = generated("ore_lead", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_lead"));
	public static final DeferredBlock<BlockOre> ore_oil = generated("ore_oil", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_oil"));
	public static final DeferredBlock<Block> ore_oil_empty = generated("ore_oil_empty", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_oil_empty"));
	public static final DeferredBlock<BlockFallingNT> ore_oil_sand = generated("ore_oil_sand", BlockFallingNT::new, props(Mat.SAND, 0.5F, 1.0F).sound(SoundType.SAND), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_oil_sand_alt"));
	public static final DeferredBlock<BlockOre> ore_lignite = generated("ore_lignite", BlockOre::new, props(Mat.ROCK, 5.0F, 15.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_lignite"));
	public static final DeferredBlock<BlockOutgas> ore_asbestos = generated("ore_asbestos", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 5.0F, 15.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_asbestos"));
	public static final DeferredBlock<BlockOre> ore_schrabidium = generated("ore_schrabidium", p -> new BlockOre(p).setRad(0.1F), props(Mat.ROCK, 15.0F, 600.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_schrabidium"));
	public static final DeferredBlock<Block> ore_australium = generated("ore_australium", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_australium"));
	public static final DeferredBlock<BlockOre> ore_rare = generated("ore_rare", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_rare"));
	public static final DeferredBlock<BlockOre> ore_cobalt = generated("ore_cobalt", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_cobalt"));
	public static final DeferredBlock<BlockOre> ore_cinnebar = generated("ore_cinnebar", BlockOre::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_cinnebar"));
	public static final DeferredBlock<BlockOre> ore_coltan = generated("ore_coltan", BlockOre::new, props(Mat.ROCK, 15.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_coltan"));
	public static final DeferredBlock<Block> cluster_iron = generated("cluster_iron", Block::new, props(Mat.ROCK, 5.0F, 15.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/cluster_iron"));
	public static final DeferredBlock<Block> cluster_titanium = generated("cluster_titanium", Block::new, props(Mat.ROCK, 5.0F, 15.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/cluster_titanium"));
	public static final DeferredBlock<Block> cluster_aluminium = generated("cluster_aluminium", Block::new, props(Mat.ROCK, 5.0F, 15.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/cluster_aluminium"));
	public static final DeferredBlock<Block> cluster_copper = generated("cluster_copper", Block::new, props(Mat.ROCK, 5.0F, 15.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/cluster_copper"));
	public static final DeferredBlock<Block> ore_bedrock_oil = generated("ore_bedrock_oil", Block::new, props(Mat.ROCK, 0.0F, 1_000_000).strength(-1.0F, 3600000.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_bedrock_oil"));
	public static final DeferredBlock<BlockOutgas> ore_nether_uranium = generated("ore_nether_uranium", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_nether_uranium"));
	public static final DeferredBlock<BlockOutgas> ore_nether_uranium_scorched = generated("ore_nether_uranium_scorched", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_nether_uranium_scorched"));
	public static final DeferredBlock<Block> ore_nether_plutonium = generated("ore_nether_plutonium", Block::new, props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_nether_plutonium"));
	public static final DeferredBlock<Block> ore_nether_tungsten = generated("ore_nether_tungsten", Block::new, props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_nether_tungsten"));
	public static final DeferredBlock<BlockOre> ore_nether_sulfur = generated("ore_nether_sulfur", BlockOre::new, props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_nether_sulfur"));
	public static final DeferredBlock<BlockOre> ore_nether_fire = generated("ore_nether_fire", BlockOre::new, props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_nether_fire"));
	public static final DeferredBlock<BlockOre> ore_nether_cobalt = generated("ore_nether_cobalt", BlockOre::new, props(Mat.ROCK, 0.4F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_nether_cobalt"));
	public static final DeferredBlock<Block> ore_nether_schrabidium = generated("ore_nether_schrabidium", Block::new, props(Mat.ROCK, 15.0F, 600.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_nether_schrabidium"));
	public static final BlockEnumMulti.Variants<EnumMeteorType> ore_meteor = multi("ore_meteor", EnumMeteorType.class, "block.hbm.ore_meteor.", (p, d, v) -> new BlockEnumMulti(p, d), () -> props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS,
			BlockModel.cube("blocks/ore_meteor.iron"), BlockModel.cube("blocks/ore_meteor.copper"), BlockModel.cube("blocks/ore_meteor.aluminium"), BlockModel.cube("blocks/ore_meteor.rareearth"), BlockModel.cube("blocks/ore_meteor.cobalt"));
	public static final DeferredBlock<BlockOre> ore_gneiss_iron = generated("ore_gneiss_iron", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_gneiss_iron"));
	public static final DeferredBlock<BlockOre> ore_gneiss_gold = generated("ore_gneiss_gold", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_gneiss_gold"));
	public static final DeferredBlock<BlockOutgas> ore_gneiss_uranium = generated("ore_gneiss_uranium", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_gneiss_uranium"));
	public static final DeferredBlock<BlockOutgas> ore_gneiss_uranium_scorched = generated("ore_gneiss_uranium_scorched", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_gneiss_uranium_scorched"));
	public static final DeferredBlock<BlockOre> ore_gneiss_copper = generated("ore_gneiss_copper", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_gneiss_copper"));
	public static final DeferredBlock<BlockOutgas> ore_gneiss_asbestos = generated("ore_gneiss_asbestos", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_gneiss_asbestos"));
	public static final DeferredBlock<BlockOre> ore_gneiss_lithium = generated("ore_gneiss_lithium", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_gneiss_lithium"));
	public static final DeferredBlock<BlockOre> ore_gneiss_schrabidium = generated("ore_gneiss_schrabidium", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_gneiss_schrabidium"));
	public static final DeferredBlock<BlockOre> ore_gneiss_rare = generated("ore_gneiss_rare", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_gneiss_rare"));
	public static final DeferredBlock<BlockOre> ore_gneiss_gas = generated("ore_gneiss_gas", BlockOre::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_gneiss_gas"));
	public static final BlockEnumMulti.Variants<EnumStoneType> stone_resource = multi("stone_resource", EnumStoneType.class, "block.hbm.stone_resource.", (p, d, v) -> new BlockEnumMulti(p, d), () -> props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS,
			BlockModel.cube("blocks/stone_resource.sulfur"), BlockModel.cube("blocks/stone_resource.asbestos"), BlockModel.cube("blocks/stone_resource.hematite"), BlockModel.cube("blocks/stone_resource.malachite"), BlockModel.cube("blocks/stone_resource.limestone"), BlockModel.cube("blocks/stone_resource.bauxite"));
	public static final DeferredBlock<Block> stone_gneiss = generated("stone_gneiss", Block::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/stone_gneiss_var"));
	public static final DeferredBlock<Block> gneiss_brick = generated("gneiss_brick", Block::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/gneiss_brick"));
	public static final DeferredBlock<Block> gneiss_tile = generated("gneiss_tile", Block::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/gneiss_tile"));
	public static final DeferredBlock<Block> gneiss_chiseled = generated("gneiss_chiseled", Block::new, props(Mat.ROCK, 1.5F, 10.0F), NtmTab.BLOCKS, BlockModel.column("blocks/gneiss_chiseled", "blocks/gneiss_tile"));
	public static final DeferredBlock<Block> basalt = generated("basalt", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.column("blocks/basalt", "blocks/basalt_top"));
	public static final DeferredBlock<Block> basalt_smooth = generated("basalt_smooth", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/basalt_smooth"));
	public static final DeferredBlock<Block> basalt_brick = generated("basalt_brick", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/basalt_brick"));
	public static final DeferredBlock<Block> basalt_polished = generated("basalt_polished", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/basalt_polished"));
	public static final DeferredBlock<Block> basalt_tiles = generated("basalt_tiles", Block::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/basalt_tiles"));
	public static final DeferredBlock<BlockHazard> block_uranium = generated("block_uranium", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_uranium"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_u233 = generated("block_u233", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_u233"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_u235 = generated("block_u235", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_u235"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_u238 = generated("block_u238", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_u238"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_uranium_fuel = generated("block_uranium_fuel", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_uranium_fuel"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_neptunium = generated("block_neptunium", BlockHazard::new, props(Mat.IRON, 5.0F, 60.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_neptunium"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_mox_fuel = generated("block_mox_fuel", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_mox_fuel"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_plutonium = generated("block_plutonium", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_plutonium"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_pu239 = generated("block_pu239", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_pu239"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_pu240 = generated("block_pu240", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_pu240"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_pu_mix = generated("block_pu_mix", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_pu_mix"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_plutonium_fuel = generated("block_plutonium_fuel", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_plutonium_fuel"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_thorium = generated("block_thorium", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_thorium"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_thorium_fuel = generated("block_thorium_fuel", BlockHazard::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_thorium_fuel"), Gen.BEACON);
	public static final DeferredBlock<Block> block_titanium = generated("block_titanium", Block::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_titanium"), Gen.BEACON);
	public static final DeferredBlock<Block> block_sulfur = generated("block_sulfur", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/block_sulfur"), Gen.BEACON);
	public static final DeferredBlock<Block> block_niter = generated("block_niter", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/block_niter"), Gen.BEACON);
	public static final DeferredBlock<Block> block_copper = generated("block_copper", Block::new, props(Mat.IRON, 5.0F, 20.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_copper"), Gen.BEACON);
	public static final DeferredBlock<Block> block_red_copper = generated("block_red_copper", Block::new, props(Mat.IRON, 5.0F, 25.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_red_copper"), Gen.BEACON);
	public static final DeferredBlock<Block> block_tungsten = generated("block_tungsten", Block::new, props(Mat.IRON, 5.0F, 20.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_tungsten"), Gen.BEACON);
	public static final DeferredBlock<Block> block_aluminium = generated("block_aluminium", Block::new, props(Mat.IRON, 5.0F, 20.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_aluminium"), Gen.BEACON);
	public static final DeferredBlock<Block> block_fluorite = generated("block_fluorite", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/block_fluorite"), Gen.BEACON);
	public static final DeferredBlock<Block> block_beryllium = generated("block_beryllium", Block::new, props(Mat.IRON, 5.0F, 20.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_beryllium"), Gen.BEACON);
	public static final DeferredBlock<Block> block_cobalt = generated("block_cobalt", Block::new, props(Mat.IRON, 5.0F, 50.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/block_cobalt"), Gen.BEACON);
	public static final DeferredBlock<Block> block_steel = generated("block_steel", Block::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_steel"), Gen.BEACON);
	public static final DeferredBlock<Block> block_tcalloy = generated("block_tcalloy", Block::new, props(Mat.IRON, 5.0F, 70.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_tcalloy"), Gen.BEACON);
	public static final DeferredBlock<Block> block_cdalloy = generated("block_cdalloy", Block::new, props(Mat.IRON, 5.0F, 70.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_cdalloy"), Gen.BEACON);
	public static final DeferredBlock<Block> block_lead = generated("block_lead", Block::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_lead"), Gen.BEACON);
	public static final DeferredBlock<Block> block_bismuth = generated("block_bismuth", Block::new, props(Mat.IRON, 5.0F, 90.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_bismuth"), Gen.BEACON);
	public static final DeferredBlock<Block> block_cadmium = generated("block_cadmium", Block::new, props(Mat.IRON, 5.0F, 90.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_cadmium"), Gen.BEACON);
	public static final DeferredBlock<Block> block_coltan = generated("block_coltan", Block::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_coltan"), Gen.BEACON);
	public static final DeferredBlock<Block> block_tantalium = generated("block_tantalium", Block::new, props(Mat.IRON, 5.0F, 50.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_tantalium"), Gen.BEACON);
	public static final DeferredBlock<Block> block_zirconium = generated("block_zirconium", Block::new, props(Mat.IRON, 5.0F, 30.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_zirconium"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_white_phosphorus = generated("block_white_phosphorus", BlockHazard::new, props(Mat.ROCK, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/block_white_phosphorus"), Gen.BEACON);
	public static final DeferredBlock<BlockFallingNT> block_scrap = generated("block_scrap", BlockFallingNT::new, props(Mat.SAND, 2.5F, 5.0F).sound(SoundType.GRAVEL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_scrap"));
	public static final DeferredBlock<BlockFallingNT> block_electrical_scrap = generated("block_electrical_scrap", BlockFallingNT::new, props(Mat.IRON, 2.5F, 5.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/electrical_scrap"));
	public static final DeferredBlock<Block> block_foam = generated("block_foam", Block::new, props(Mat.SNOW, 0.5F, 0.0F).sound(SoundType.SNOW), NtmTab.BLOCKS, BlockModel.cube("blocks/foam"));
	public static final BlockEnumMulti.Variants<EnumCokeType> block_coke = multi("block_coke", EnumCokeType.class, "block.hbm.block_coke.", com.hbm.blocks.generic.BlockCoke::new, () -> props(Mat.IRON, 5.0F, 10.0F).sound(SoundType.METAL), NtmTab.BLOCKS,
			BlockModel.cube("blocks/block_coke.coal"), BlockModel.cube("blocks/block_coke.lignite"), BlockModel.cube("blocks/block_coke.petroleum"));
	public static final DeferredBlock<Block> block_boron = generated("block_boron", Block::new, props(Mat.IRON, 5.0F, 10.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_boron"), Gen.BEACON);
	public static final DeferredBlock<RotatedPillarBlock> block_insulator = generated("block_insulator", RotatedPillarBlock::new, props(Mat.CLOTH, 5.0F, 10.0F).sound(SoundType.WOOL), NtmTab.BLOCKS, BlockModel.axis("blocks/block_insulator_side", "blocks/block_insulator_top"));
	public static final DeferredBlock<RotatedPillarBlock> block_fiberglass = generated("block_fiberglass", RotatedPillarBlock::new, props(Mat.CLOTH, 5.0F, 15.0F).sound(SoundType.WOOL), NtmTab.BLOCKS, BlockModel.axis("blocks/block_fiberglass_side", "blocks/block_fiberglass_top"));
	public static final DeferredBlock<BlockOutgas> block_asbestos = generated("block_asbestos", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.CLOTH, 5.0F, 15.0F).sound(SoundType.WOOL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_asbestos"));
	public static final DeferredBlock<BlockHazard> block_trinitite = generated("block_trinitite", BlockHazard::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/block_trinitite"), Gen.BEACON);
	public static final DeferredBlock<BlockOutgas> ancient_scrap = generated("ancient_scrap", p -> new BlockOutgas(p).setOutgas(true, 1, true, true), props(Mat.IRON, 100.0F, 6000.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ancient_scrap"));
	public static final DeferredBlock<BlockHazard> block_corium = generated("block_corium", BlockHazard::new, props(Mat.IRON, 100.0F, 6000.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/block_corium"));
	public static final DeferredBlock<BlockOutgas> block_corium_cobble = generated("block_corium_cobble", p -> new BlockOutgas(p).setOutgas(true, 1, true, true), props(Mat.IRON, 100.0F, 6000.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/block_corium_cobble"));
	public static final DeferredBlock<BlockHazard> block_schraranium = generated("block_schraranium", BlockHazard::new, props(Mat.IRON, 5.0F, 250.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_schraranium"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_schrabidium = generated("block_schrabidium", BlockHazard::new, props(Mat.IRON, 5.0F, 600.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_schrabidium"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_schrabidate = generated("block_schrabidate", BlockHazard::new, props(Mat.IRON, 5.0F, 600.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_schrabidate"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_solinium = generated("block_solinium", BlockHazard::new, props(Mat.IRON, 5.0F, 600.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_solinium"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_schrabidium_fuel = generated("block_schrabidium_fuel", BlockHazard::new, props(Mat.IRON, 5.0F, 600.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_schrabidium_fuel"), Gen.BEACON);
	public static final DeferredBlock<Block> block_euphemium = generated("block_euphemium", Block::new, props(Mat.IRON, 5.0F, 60000.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_euphemium"), Gen.BEACON);
	public static final DeferredBlock<RotatedPillarBlock> block_schrabidium_cluster = generated("block_schrabidium_cluster", RotatedPillarBlock::new, props(Mat.ROCK, 5.0F, 60000.0F), NtmTab.BLOCKS, BlockModel.axis("blocks/block_schrabidium_cluster_side", "blocks/block_schrabidium_cluster_top"));
	public static final DeferredBlock<RotatedPillarBlock> block_euphemium_cluster = generated("block_euphemium_cluster", RotatedPillarBlock::new, props(Mat.ROCK, 5.0F, 60000.0F), NtmTab.BLOCKS, BlockModel.axis("blocks/block_euphemium_cluster_side", "blocks/block_euphemium_cluster_top"));
	public static final DeferredBlock<Block> block_dineutronium = generated("block_dineutronium", Block::new, props(Mat.IRON, 5.0F, 60000.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_dineutronium"), Gen.BEACON);
	public static final DeferredBlock<Block> block_magnetized_tungsten = generated("block_magnetized_tungsten", Block::new, props(Mat.IRON, 5.0F, 75.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_magnetized_tungsten"), Gen.BEACON);
	public static final DeferredBlock<Block> block_combine_steel = generated("block_combine_steel", Block::new, props(Mat.IRON, 5.0F, 600.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_combine_steel"), Gen.BEACON);
	public static final DeferredBlock<Block> block_desh = generated("block_desh", Block::new, props(Mat.IRON, 5.0F, 300.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_desh"), Gen.BEACON);
	public static final DeferredBlock<Block> block_dura_steel = generated("block_dura_steel", Block::new, props(Mat.IRON, 5.0F, 200.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_dura_steel"), Gen.BEACON);
	public static final DeferredBlock<Block> block_starmetal = generated("block_starmetal", Block::new, props(Mat.IRON, 5.0F, 400.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_starmetal"), Gen.BEACON);
	public static final DeferredBlock<Block> block_polymer = generated("block_polymer", Block::new, props(Mat.ROCK, 3.0F, 10.0F).sound(SoundType.STONE), NtmTab.BLOCKS, BlockModel.cube("blocks/block_polymer"), Gen.BEACON);
	public static final DeferredBlock<Block> block_bakelite = generated("block_bakelite", Block::new, props(Mat.ROCK, 3.0F, 5.0F).sound(SoundType.STONE), NtmTab.BLOCKS, BlockModel.cube("blocks/block_bakelite"), Gen.BEACON);
	public static final DeferredBlock<Block> block_rubber = generated("block_rubber", Block::new, props(Mat.ROCK, 3.0F, 15.0F).sound(SoundType.STONE), NtmTab.BLOCKS, BlockModel.cube("blocks/block_rubber"), Gen.BEACON);
	public static final DeferredBlock<Block> block_australium = generated("block_australium", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/block_australium"), Gen.BEACON);
	public static final DeferredBlock<Block> block_lanthanium = generated("block_lanthanium", Block::new, props(Mat.IRON, 5.0F, 10.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_lanthanium"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_ra226 = generated("block_ra226", BlockHazard::new, props(Mat.IRON, 5.0F, 10.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_ra226"), Gen.BEACON);
	public static final DeferredBlock<BlockHazard> block_actinium = generated("block_actinium", BlockHazard::new, props(Mat.IRON, 5.0F, 10.0F).sound(SoundType.METAL), NtmTab.BLOCKS, BlockModel.cube("blocks/block_actinium"), Gen.BEACON);
	public static final DeferredBlock<RotatedPillarBlock> block_tritium = generated("block_tritium", RotatedPillarBlock::new, props(Mat.GLASS, 3.0F, 2.0F).sound(SoundType.GLASS), NtmTab.BLOCKS, BlockModel.axis("blocks/block_tritium_side", "blocks/block_tritium_top"));
	public static final DeferredBlock<Block> block_smore = generated("block_smore", Block::new, props(Mat.ROCK, 15.0F, 600.0F), NtmTab.BLOCKS, BlockModel.column("blocks/block_smore_side", "blocks/block_smore_top"));
	public static final DeferredBlock<BlockOre> deco_titanium = generated("deco_titanium", p -> new BlockOre(p).noFortune(), props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/deco_titanium"));
	public static final DeferredBlock<BlockOre> deco_red_copper = generated("deco_red_copper", p -> new BlockOre(p).noFortune(), props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/deco_red_copper"));
	public static final DeferredBlock<BlockOre> deco_tungsten = generated("deco_tungsten", p -> new BlockOre(p).noFortune(), props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/deco_tungsten"));
	public static final DeferredBlock<BlockOre> deco_aluminium = generated("deco_aluminium", p -> new BlockOre(p).noFortune(), props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/deco_aluminium"));
	public static final DeferredBlock<BlockOre> deco_steel = generated("deco_steel", p -> new BlockOre(p).noFortune(), props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/deco_steel"));
	public static final DeferredBlock<BlockOre> deco_rusty_steel = generated("deco_rusty_steel", p -> new BlockOre(p).noFortune(), props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/deco_rusty_steel"));
	public static final DeferredBlock<BlockOre> deco_lead = generated("deco_lead", p -> new BlockOre(p).noFortune(), props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/deco_lead"));
	public static final DeferredBlock<BlockOre> deco_beryllium = generated("deco_beryllium", p -> new BlockOre(p).noFortune(), props(Mat.IRON, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/deco_beryllium"));
	public static final BlockEnumMulti.Variants<PlatemetalType> platemetal = multi("platemetal", PlatemetalType.class, "block.hbm.platemetal.", (p, d, v) -> new BlockEnumMulti(p, d), () -> props(Mat.IRON, 5.0F, 10.0F).sound(ModSoundTypes.PLATEMETAL), NtmTab.BLOCKS,
			BlockModel.cube("blocks/platemetal.base"), BlockModel.cube("blocks/platemetal.black"), BlockModel.cube("blocks/platemetal.white"), BlockModel.cube("blocks/platemetal.red"), BlockModel.cube("blocks/platemetal.green"), BlockModel.cube("blocks/platemetal.light_gray"), BlockModel.cube("blocks/platemetal.blue"), BlockModel.cube("blocks/platemetal.purple"), BlockModel.cube("blocks/platemetal.cyan"), BlockModel.cube("blocks/platemetal.pink"), BlockModel.cube("blocks/platemetal.lime"), BlockModel.cube("blocks/platemetal.yellow"), BlockModel.cube("blocks/platemetal.light_blue"), BlockModel.cube("blocks/platemetal.magenta"), BlockModel.cube("blocks/platemetal.orange"));
	public static final DeferredBlock<BlockOutgas> deco_asbestos = generated("deco_asbestos", p -> new BlockOutgas(p).setOutgas(true, 5, true).noFortune(), props(Mat.CLOTH, 5.0F, 10.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/deco_asbestos"));
	public static final DeferredBlock<Block> deco_rbmk = generated("deco_rbmk", Block::new, props(Mat.IRON, 5.0F, 100.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/rbmk/rbmk_top"));
	public static final DeferredBlock<Block> deco_rbmk_smooth = generated("deco_rbmk_smooth", Block::new, props(Mat.IRON, 5.0F, 100.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/rbmk/rbmk_blank_top"));
	public static final DeferredBlock<BlockFallingNT> gravel_obsidian = generated("gravel_obsidian", BlockFallingNT::new, props(Mat.IRON, 5.0F, 240.0F).sound(SoundType.GRAVEL), NtmTab.BLOCKS, BlockModel.cube("blocks/gravel_obsidian"));
	public static final DeferredBlock<BlockFallingNT> gravel_diamond = generated("gravel_diamond", BlockFallingNT::new, props(Mat.SAND, 0.6F, LEGACY_NONE).sound(SoundType.GRAVEL), NtmTab.BLOCKS, BlockModel.cube("blocks/gravel_diamond"));
	public static final DeferredBlock<Block> reinforced_brick = generated("reinforced_brick", Block::new, props(Mat.ROCK, 15.0F, 300.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/reinforced_brick"));
	public static final DeferredBlock<BlockNTMGlass> reinforced_glass = generated("reinforced_glass", p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), false), props(Mat.ROCK, 2.0F, 25.0F), NtmTab.BLOCKS, BlockModel.glass("blocks/reinforced_glass", false));
	public static final DeferredBlock<Block> reinforced_light = generated("reinforced_light", Block::new, props(Mat.ROCK, 15.0F, 80.0F).lightLevel(s -> (int) (1.0F * 15)), NtmTab.BLOCKS, BlockModel.cube("blocks/reinforced_light"));
	public static final DeferredBlock<Block> reinforced_sand = generated("reinforced_sand", Block::new, props(Mat.ROCK, 15.0F, 40.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/reinforced_sand"));
	public static final DeferredBlock<BlockNTMGlass> reinforced_laminate = generated("reinforced_laminate", p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), true), props(Mat.ROCK, 15.0F, 300.0F), NtmTab.BLOCKS, BlockModel.glass("blocks/reinforced_laminate", true));
	public static final DeferredBlock<Block> reinforced_stone = generated("reinforced_stone", Block::new, props(Mat.ROCK, 15.0F, 100.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/reinforced_stone"));
	public static final DeferredBlock<BlockNoSpawn> reinforced_ducrete = generated("reinforced_ducrete", BlockNoSpawn::new, props(Mat.ROCK, 20.0F, 1000.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/reinforced_ducrete"));
	public static final DeferredBlock<BlockNoSpawn> concrete_smooth = generated("concrete_smooth", BlockNoSpawn::new, props(Mat.ROCK, 15.0F, 140.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/concrete"));
	public static final BlockEnumMulti.Variants<EnumConcreteType> concrete_colored_ext = multi("concrete_colored_ext", EnumConcreteType.class, "block.hbm.concrete_colored_ext.", com.hbm.blocks.generic.BlockConcreteColoredExt::new, () -> props(Mat.ROCK, 15.0F, 140.0F), NtmTab.BLOCKS,
			BlockModel.cube("blocks/concrete_colored_ext.machine"), BlockModel.column("blocks/concrete_colored_ext.machine_stripe", "blocks/concrete_colored_ext.machine"), BlockModel.cube("blocks/concrete_colored_ext.indigo"), BlockModel.cube("blocks/concrete_colored_ext.purple"), BlockModel.cube("blocks/concrete_colored_ext.pink"), BlockModel.cube("blocks/concrete_colored_ext.hazard"), BlockModel.cube("blocks/concrete_colored_ext.sand"), BlockModel.cube("blocks/concrete_colored_ext.bronze"));
	public static final DeferredBlock<BlockNoSpawn> concrete = generated("concrete", BlockNoSpawn::new, props(Mat.ROCK, 15.0F, 140.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/concrete_tile"));
	public static final DeferredBlock<BlockNoSpawn> concrete_asbestos = generated("concrete_asbestos", BlockNoSpawn::new, props(Mat.ROCK, 15.0F, 150.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/concrete_asbestos"));
	public static final DeferredBlock<BlockNoSpawn> concrete_rebar = generated("concrete_rebar", BlockNoSpawn::new, props(Mat.ROCK, 50.0F, 240.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/concrete_rebar"));
	public static final DeferredBlock<BlockFallingNT> concrete_super_broken = generated("concrete_super_broken", BlockFallingNT::new, props(Mat.ROCK, 10.0F, 20.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/concrete_super_broken"));
	public static final DeferredBlock<BlockNoSpawn> ducrete_smooth = generated("ducrete_smooth", BlockNoSpawn::new, props(Mat.ROCK, 20.0F, 500.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ducrete"));
	public static final DeferredBlock<BlockNoSpawn> ducrete = generated("ducrete", BlockNoSpawn::new, props(Mat.ROCK, 20.0F, 500.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/ducrete_tile"));
	public static final DeferredBlock<RotatedPillarBlock> concrete_pillar = generated("concrete_pillar", RotatedPillarBlock::new, props(Mat.ROCK, 15.0F, 180.0F), NtmTab.BLOCKS, BlockModel.axis("blocks/concrete_pillar_side", "blocks/concrete_pillar_top"));
	public static final DeferredBlock<BlockNoSpawn> brick_concrete = generated("brick_concrete", BlockNoSpawn::new, props(Mat.ROCK, 15.0F, 160.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_concrete"));
	public static final DeferredBlock<Block> brick_concrete_mossy = generated("brick_concrete_mossy", Block::new, props(Mat.ROCK, 15.0F, 160.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_concrete_mossy"));
	public static final DeferredBlock<Block> brick_concrete_cracked = generated("brick_concrete_cracked", Block::new, props(Mat.ROCK, 15.0F, 60.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_concrete_cracked"));
	public static final DeferredBlock<Block> brick_concrete_broken = generated("brick_concrete_broken", Block::new, props(Mat.ROCK, 15.0F, 45.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_concrete_broken"));
	public static final DeferredBlock<BlockNoSpawn> brick_ducrete = generated("brick_ducrete", BlockNoSpawn::new, props(Mat.ROCK, 15.0F, 750.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_ducrete"));
	public static final DeferredBlock<Block> brick_obsidian = generated("brick_obsidian", Block::new, props(Mat.ROCK, 15.0F, 120.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_obsidian"));
	public static final DeferredBlock<Block> brick_compound = generated("brick_compound", Block::new, props(Mat.ROCK, 15.0F, 400.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_compound"));
	public static final DeferredBlock<Block> brick_light = generated("brick_light", Block::new, props(Mat.ROCK, 5.0F, 20.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_light"));
	public static final DeferredBlock<BlockOutgas> brick_asbestos = generated("brick_asbestos", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 5.0F, 1000.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_asbestos"));
	public static final DeferredBlock<Block> brick_fire = generated("brick_fire", Block::new, props(Mat.ROCK, 5.0F, 35.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_fire"));
	public static final BlockEnumMulti.Variants<LightstoneType> lightstone = multi("lightstone", LightstoneType.class, "block.hbm.lightstone.", (p, d, v) -> new BlockEnumMulti(p, d), () -> props(Mat.ROCK, 2F, 15.0F), NtmTab.BLOCKS,
			BlockModel.cube("blocks/lightstone.unrefined"), BlockModel.cube("blocks/lightstone.tile"), BlockModel.cube("blocks/lightstone.bricks"), BlockModel.column("blocks/lightstone.bricks_chiseled", "blocks/lightstone.bricks_chiseled.top"), BlockModel.column("blocks/lightstone.chiseled", "blocks/lightstone.chiseled.top"));
	public static final DeferredBlock<StairBlock> concrete_smooth_stairs = stairs("concrete_smooth_stairs", concrete_smooth, NtmTab.BLOCKS, "blocks/concrete");
	public static final DeferredBlock<StairBlock> concrete_stairs = stairs("concrete_stairs", concrete, NtmTab.BLOCKS, "blocks/concrete_tile");
	public static final DeferredBlock<StairBlock> concrete_asbestos_stairs = stairs("concrete_asbestos_stairs", concrete_asbestos, NtmTab.BLOCKS, "blocks/concrete_asbestos");
	public static final DeferredBlock<StairBlock> ducrete_smooth_stairs = stairs("ducrete_smooth_stairs", ducrete_smooth, NtmTab.BLOCKS, "blocks/ducrete");
	public static final DeferredBlock<StairBlock> brick_concrete_stairs = stairs("brick_concrete_stairs", brick_concrete, NtmTab.BLOCKS, "blocks/brick_concrete");
	public static final DeferredBlock<StairBlock> brick_concrete_mossy_stairs = stairs("brick_concrete_mossy_stairs", brick_concrete_mossy, NtmTab.BLOCKS, "blocks/brick_concrete_mossy");
	public static final DeferredBlock<StairBlock> brick_concrete_cracked_stairs = stairs("brick_concrete_cracked_stairs", brick_concrete_cracked, NtmTab.BLOCKS, "blocks/brick_concrete_cracked");
	public static final DeferredBlock<StairBlock> brick_concrete_broken_stairs = stairs("brick_concrete_broken_stairs", brick_concrete_broken, NtmTab.BLOCKS, "blocks/brick_concrete_broken");
	public static final DeferredBlock<StairBlock> brick_ducrete_stairs = stairs("brick_ducrete_stairs", brick_ducrete, NtmTab.BLOCKS, "blocks/brick_ducrete");
	public static final DeferredBlock<StairBlock> reinforced_stone_stairs = stairs("reinforced_stone_stairs", reinforced_stone, NtmTab.BLOCKS, "blocks/reinforced_stone");
	public static final DeferredBlock<StairBlock> reinforced_brick_stairs = stairs("reinforced_brick_stairs", reinforced_brick, NtmTab.BLOCKS, "blocks/reinforced_brick");
	public static final DeferredBlock<StairBlock> brick_obsidian_stairs = stairs("brick_obsidian_stairs", brick_obsidian, NtmTab.BLOCKS, "blocks/brick_obsidian");
	public static final DeferredBlock<StairBlock> brick_light_stairs = stairs("brick_light_stairs", brick_light, NtmTab.BLOCKS, "blocks/brick_light");
	public static final DeferredBlock<StairBlock> brick_compound_stairs = stairs("brick_compound_stairs", brick_compound, NtmTab.BLOCKS, "blocks/brick_compound");
	public static final DeferredBlock<StairBlock> brick_asbestos_stairs = stairs("brick_asbestos_stairs", brick_asbestos, NtmTab.BLOCKS, "blocks/brick_asbestos");
	public static final DeferredBlock<StairBlock> brick_fire_stairs = stairs("brick_fire_stairs", brick_fire, NtmTab.BLOCKS, "blocks/brick_fire");
	public static final DeferredBlock<StairBlock> ducrete_stairs = stairs("ducrete_stairs", ducrete, NtmTab.BLOCKS, "blocks/ducrete_tile");
	public static final DeferredBlock<Block> cmb_brick = generated("cmb_brick", Block::new, props(Mat.ROCK, 25.0F, 5000.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/cmb_brick"));
	public static final DeferredBlock<Block> cmb_brick_reinforced = generated("cmb_brick_reinforced", Block::new, props(Mat.ROCK, 25.0F, 50000.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/cmb_brick_reinforced"));
	public static final BlockEnumMulti.Variants<TileType> vinyl_tile = multi("vinyl_tile", TileType.class, "block.hbm.vinyl_tile.", (p, d, v) -> new BlockEnumMulti(p, d), () -> props(Mat.ROCK, 10.0F, 60.0F).sound(SoundType.GLASS), NtmTab.BLOCKS,
			BlockModel.cube("blocks/vinyl_tile.large"), BlockModel.cube("blocks/vinyl_tile.small"));
	public static final DeferredBlock<BlockOutgas> tile_lab = generated("tile_lab", p -> new BlockOutgas(p).setOutgas(false, 5, true), props(Mat.ROCK, 1.0F, 20.0F).sound(SoundType.GLASS), NtmTab.BLOCKS, BlockModel.cube("blocks/tile_lab"));
	public static final DeferredBlock<BlockOutgas> tile_lab_cracked = generated("tile_lab_cracked", p -> new BlockOutgas(p).setOutgas(false, 5, true), props(Mat.ROCK, 1.0F, 20.0F).sound(SoundType.GLASS), NtmTab.BLOCKS, BlockModel.cube("blocks/tile_lab_cracked"));
	public static final DeferredBlock<BlockOutgas> tile_lab_broken = generated("tile_lab_broken", p -> new BlockOutgas(p).setOutgas(true, 5, true), props(Mat.ROCK, 1.0F, 20.0F).sound(SoundType.GLASS), NtmTab.BLOCKS, BlockModel.cube("blocks/tile_lab_broken"));
	public static final DeferredBlock<BlockOre> block_meteor = generated("block_meteor", p -> new BlockOre(p).noFortune(), props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/meteor"));
	public static final DeferredBlock<BlockOre> block_meteor_cobble = generated("block_meteor_cobble", p -> new BlockOre(p).noFortune(), props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/meteor_cobble"));
	public static final DeferredBlock<BlockOre> block_meteor_broken = generated("block_meteor_broken", p -> new BlockOre(p).noFortune(), props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/meteor_crushed"));
	public static final DeferredBlock<BlockOre> block_meteor_molten = generated("block_meteor_molten", p -> new BlockOre(p).noFortune(), props(Mat.ROCK, 15.0F, 360.0F).lightLevel(s -> (int) (0.75F * 15)), NtmTab.BLOCKS, BlockModel.cube("blocks/meteor_cobble_molten"));
	public static final DeferredBlock<Block> meteor_polished = generated("meteor_polished", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/meteor_polished"));
	public static final DeferredBlock<Block> meteor_brick = generated("meteor_brick", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/meteor_brick"));
	public static final DeferredBlock<Block> meteor_brick_mossy = generated("meteor_brick_mossy", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/meteor_brick_mossy"));
	public static final DeferredBlock<Block> meteor_brick_cracked = generated("meteor_brick_cracked", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/meteor_brick_cracked"));
	public static final DeferredBlock<Block> meteor_brick_chiseled = generated("meteor_brick_chiseled", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/meteor_brick_chiseled"));
	public static final DeferredBlock<RotatedPillarBlock> meteor_pillar = generated("meteor_pillar", RotatedPillarBlock::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.axis("blocks/meteor_pillar", "blocks/meteor_pillar_top"));
	public static final DeferredBlock<Block> meteor_battery = generated("meteor_battery", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.column("blocks/meteor_spawner_side", "blocks/meteor_power"));
	public static final DeferredBlock<Block> brick_jungle = generated("brick_jungle", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_jungle"));
	public static final DeferredBlock<Block> brick_jungle_cracked = generated("brick_jungle_cracked", Block::new, props(Mat.ROCK, 15.0F, 360.0F), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_jungle_cracked"));
	public static final DeferredBlock<Block> brick_jungle_lava = generated("brick_jungle_lava", Block::new, props(Mat.ROCK, 15.0F, 360.0F).lightLevel(s -> (int) (5F/15F * 15)), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_jungle_lava"));
	public static final DeferredBlock<BlockOre> brick_jungle_ooze = generated("brick_jungle_ooze", BlockOre::new, props(Mat.ROCK, 15.0F, 360.0F).lightLevel(s -> (int) (5F/15F * 15)), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_jungle_ooze"));
	public static final DeferredBlock<BlockOre> brick_jungle_mystic = generated("brick_jungle_mystic", BlockOre::new, props(Mat.ROCK, 15.0F, 360.0F).lightLevel(s -> (int) (5F/15F * 15)), NtmTab.BLOCKS, BlockModel.cube("blocks/brick_jungle_mystic"));
	public static final DeferredBlock<BlockFallingNT> moon_turf = generated("moon_turf", BlockFallingNT::new, props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.BLOCKS, BlockModel.cube("blocks/moon_turf"));
	public static final DeferredBlock<BlockOre> waste_planks = generated("waste_planks", BlockOre::new, props(Mat.WOOD, 0.5F, 2.5F).sound(SoundType.WOOD), NtmTab.BLOCKS, BlockModel.cube("blocks/waste_planks"));
	public static final DeferredBlock<BlockOre> frozen_dirt = generated("frozen_dirt", BlockOre::new, props(Mat.GROUND, 0.5F, 2.5F).sound(SoundType.GLASS), NtmTab.BLOCKS, BlockModel.cube("blocks/frozen_dirt"));
	public static final DeferredBlock<BlockOre> frozen_planks = generated("frozen_planks", BlockOre::new, props(Mat.WOOD, 0.5F, 2.5F).sound(SoundType.GLASS), NtmTab.BLOCKS, BlockModel.cube("blocks/frozen_planks"));
	public static final DeferredBlock<BlockFallingNT> dirt_dead = generated("dirt_dead", BlockFallingNT::new, props(Mat.GROUND, 0.5F, LEGACY_NONE).sound(SoundType.GRAVEL), NtmTab.BLOCKS, BlockModel.cube("blocks/dirt_dead"));
	public static final DeferredBlock<BlockFallingNT> dirt_oily = generated("dirt_oily", BlockFallingNT::new, props(Mat.GROUND, 0.5F, LEGACY_NONE).sound(SoundType.GRAVEL), NtmTab.BLOCKS, BlockModel.cube("blocks/dirt_oily"));
	public static final DeferredBlock<BlockFallingNT> sand_dirty = generated("sand_dirty", BlockFallingNT::new, props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.BLOCKS, BlockModel.cube("blocks/sand_dirty"));
	public static final DeferredBlock<BlockFallingNT> sand_dirty_red = generated("sand_dirty_red", BlockFallingNT::new, props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.BLOCKS, BlockModel.cube("blocks/sand_dirty_red"));
	public static final DeferredBlock<BlockFallingNT> stone_cracked = generated("stone_cracked", BlockFallingNT::new, props(Mat.ROCK, 5.0F, LEGACY_NONE).sound(SoundType.STONE), NtmTab.BLOCKS, BlockModel.cube("blocks/stone_cracked"));
	public static final DeferredBlock<Block> tektite = generated("tektite", Block::new, props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.BLOCKS, BlockModel.cube("blocks/tektite"));
	public static final DeferredBlock<Block> ore_tektite_osmiridium = generated("ore_tektite_osmiridium", Block::new, props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.BLOCKS, BlockModel.cube("blocks/ore_tektite_osmiridium"));
	public static final BlockEnumMulti.Variants<EnumSandType> sand_mix = multi("sand_mix", EnumSandType.class, "block.hbm.sand_", com.hbm.blocks.generic.BlockNTMSand::new, () -> props(Mat.SAND, 0.5F, LEGACY_NONE).sound(SoundType.SAND), NtmTab.MACHINE,
			BlockModel.cube("blocks/sand_boron"), BlockModel.cube("blocks/sand_lead"), BlockModel.cube("blocks/sand_uranium"), BlockModel.cube("blocks/sand_polonium"), BlockModel.cube("blocks/sand_quartz"));
	public static final DeferredBlock<BlockNTMGlass> glass_boron = generated("glass_boron", p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), false), props(Mat.GLASS, 0.3F, LEGACY_NONE).sound(SoundType.GLASS), NtmTab.MACHINE, BlockModel.glass("blocks/glass_boron", false));
	public static final DeferredBlock<BlockNTMGlass> glass_lead = generated("glass_lead", p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), false), props(Mat.GLASS, 0.3F, LEGACY_NONE).sound(SoundType.GLASS), NtmTab.MACHINE, BlockModel.glass("blocks/glass_lead", false));
	public static final DeferredBlock<BlockNTMGlass> glass_uranium = generated("glass_uranium", p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), false), props(Mat.GLASS, 0.3F, LEGACY_NONE).sound(SoundType.GLASS).lightLevel(s -> (int) (5F/15F * 15)), NtmTab.MACHINE, BlockModel.glass("blocks/glass_uranium", true));
	public static final DeferredBlock<BlockNTMGlass> glass_trinitite = generated("glass_trinitite", p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), false), props(Mat.GLASS, 0.3F, LEGACY_NONE).sound(SoundType.GLASS).lightLevel(s -> (int) (5F/15F * 15)), NtmTab.MACHINE, BlockModel.glass("blocks/glass_trinitite", true));
	public static final DeferredBlock<BlockNTMGlass> glass_polonium = generated("glass_polonium", p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), false), props(Mat.GLASS, 0.3F, LEGACY_NONE).sound(SoundType.GLASS).lightLevel(s -> (int) (5F/15F * 15)), NtmTab.MACHINE, BlockModel.glass("blocks/glass_polonium", true));
	public static final DeferredBlock<BlockNTMGlass> glass_ash = generated("glass_ash", p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), false), props(Mat.GLASS, 3F, LEGACY_NONE).sound(SoundType.GLASS), NtmTab.MACHINE, BlockModel.glass("blocks/glass_ash", true));
	public static final DeferredBlock<BlockNTMGlass> glass_quartz = generated("glass_quartz", p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), true), props(Mat.GLASS, 1.0F, 40.0F).sound(SoundType.GLASS), NtmTab.BLOCKS, BlockModel.glass("blocks/glass_quartz", false));
	public static final DeferredBlock<BlockNTMGlass> glass_polarized = generated("glass_polarized", p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), false), props(Mat.GLASS, 0.3F, LEGACY_NONE).sound(SoundType.GLASS), NtmTab.MACHINE, BlockModel.glass("blocks/glass_polarized", false));
	public static final DeferredBlock<Block> seal_frame = generated("seal_frame", Block::new, props(Mat.IRON, 10.0F, 100.0F), NtmTab.MACHINE, BlockModel.cube("blocks/seal_frame"));
	public static final DeferredBlock<Block> struct_launcher = generated("struct_launcher", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MISSILE, BlockModel.cube("blocks/struct_launcher"));
	public static final DeferredBlock<Block> struct_scaffold = generated("struct_scaffold", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MISSILE, BlockModel.cube("blocks/struct_scaffold"));
	public static final BlockEnumMulti.Variants<EnumCMMaterials> cm_block = multi("cm_block", EnumCMMaterials.class, "block.hbm.cm_block.", (p, d, v) -> new BlockEnumMulti(p, d), () -> props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE,
			BlockModel.cube("blocks/cm_block_steel"), BlockModel.cube("blocks/cm_block_alloy"), BlockModel.cube("blocks/cm_block_desh"), BlockModel.cube("blocks/cm_block_tcalloy"));
	public static final BlockEnumMulti.Variants<EnumCMMaterials> cm_sheet = multi("cm_sheet", EnumCMMaterials.class, "block.hbm.cm_sheet.", (p, d, v) -> new BlockEnumMulti(p, d), () -> props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE,
			BlockModel.cube("blocks/cm_sheet_steel"), BlockModel.cube("blocks/cm_sheet_alloy"), BlockModel.cube("blocks/cm_sheet_desh"), BlockModel.cube("blocks/cm_sheet_tcalloy"));
	public static final BlockEnumMulti.Variants<EnumCMEngines> cm_engine = multi("cm_engine", EnumCMEngines.class, "block.hbm.cm_engine.", (p, d, v) -> new BlockEnumMulti(p, d), () -> props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE,
			BlockModel.cube("blocks/cm_engine_standard"), BlockModel.cube("blocks/cm_engine_desh"), BlockModel.cube("blocks/cm_engine_bismuth"));
	public static final BlockEnumMulti.Variants<EnumCMMaterials> cm_tank = multi("cm_tank", EnumCMMaterials.class, "block.hbm.cm_tank.", com.hbm.blocks.machine.BlockCMGlass::new, () -> props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE,
			BlockModel.glass("blocks/cm_tank_steel", false), BlockModel.glass("blocks/cm_tank_alloy", false), BlockModel.glass("blocks/cm_tank_desh", false), BlockModel.glass("blocks/cm_tank_tcalloy", false));
	public static final BlockEnumMulti.Variants<EnumCMCircuit> cm_circuit = multi("cm_circuit", EnumCMCircuit.class, "block.hbm.cm_circuit.", (p, d, v) -> new BlockEnumMulti(p, d), () -> props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE,
			BlockModel.cube("blocks/cm_circuit_aluminium"), BlockModel.cube("blocks/cm_circuit_copper"), BlockModel.cube("blocks/cm_circuit_red_copper"), BlockModel.cube("blocks/cm_circuit_gold"), BlockModel.cube("blocks/cm_circuit_schrabidium"));
	public static final BlockEnumMulti.Variants<EnumCMMaterials> cm_port = multi("cm_port", EnumCMMaterials.class, "block.hbm.cm_port.", (p, d, v) -> new BlockEnumMulti(p, d), () -> props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE,
			BlockModel.cube("blocks/cm_port_steel"), BlockModel.cube("blocks/cm_port_alloy"), BlockModel.cube("blocks/cm_port_desh"), BlockModel.cube("blocks/cm_port_tcalloy"));
	public static final DeferredBlock<Block> fusion_heater = generated("fusion_heater", Block::new, props(Mat.IRON, 5.0F, 10.0F), null, BlockModel.column("blocks/fusion_heater_side", "blocks/fusion_heater_top"));
	public static final DeferredBlock<Block> watz_element = generated("watz_element", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, BlockModel.column("blocks/watz_element_side", "blocks/watz_element_top"));
	public static final DeferredBlock<Block> watz_cooler = generated("watz_cooler", Block::new, props(Mat.IRON, 5.0F, 10.0F), NtmTab.MACHINE, BlockModel.column("blocks/watz_cooler_side", "blocks/watz_cooler_top"));
	public static final DeferredBlock<Block> crystal_hardened = generated("crystal_hardened", Block::new, props(Mat.IRON, 15.0F, Float.POSITIVE_INFINITY), null, BlockModel.cube("blocks/crystal_hardened"));
	public static final DeferredBlock<Block> pink_planks = generated("pink_planks", Block::new, props(Mat.WOOD, 0.0F, LEGACY_NONE).sound(SoundType.WOOD), null, BlockModel.cube("blocks/pink_planks"));
	public static final DeferredBlock<StairBlock> pink_stairs = stairs("pink_stairs", pink_planks, null, "blocks/pink_planks");
	// END GENERATED

	/** Registers a block together with its BlockItem and adds it to the given creative tab (null for none). */
	public static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties props, NtmTab tab) {
		DeferredBlock<T> block = BLOCKS.registerBlock(name, factory, props);
		ModItems.ITEMS.registerSimpleBlockItem(block);
		if(tab != null) tab.add(block);
		return block;
	}

	/** Block with its own BlockItem class */
	private static <T extends Block> DeferredBlock<T> registerWithItem(String name, Function<BlockBehaviour.Properties, T> factory, PropsWithTool props, NtmTab tab, Function<T, ? extends net.minecraft.world.item.BlockItem> item) {
		DeferredBlock<T> block = BLOCKS.registerBlock(name, factory, props.props);
		ModItems.ITEMS.register(name, () -> item.apply(block.get()));
		if(props.tool != null) TOOLS.put(block, props.tool);
		if(tab != null) tab.add(block);
		return block;
	}

	public enum Gen { BEACON }

	/**
	 * Model description for datagen, textures are paths like "blocks/ore_uranium".
	 * @param type cube (one texture), column (side + top/bottom), axis (rotatable column), stairs, glass (cutout), glass_translucent
	 */
	public record BlockModel(String type, String texture, String end) {
		public static BlockModel cube(String texture) { return new BlockModel("cube", texture, null); }
		public static BlockModel column(String side, String end) { return new BlockModel("column", side, end); }
		public static BlockModel axis(String side, String end) { return new BlockModel("axis", side, end); }
		public static BlockModel stairs(String texture) { return new BlockModel("stairs", texture, null); }
		public static BlockModel glass(String texture, boolean translucent) { return new BlockModel(translucent ? "glass_translucent" : "glass", texture, null); }
	}

	/** Registration for generated blocks */
	private static <T extends Block> DeferredBlock<T> generated(String name, Function<BlockBehaviour.Properties, T> factory, PropsWithTool props, NtmTab tab, BlockModel model, Gen... flags) {
		DeferredBlock<T> block = register(name, factory, props.props, tab);
		MODELS.put(block, model);
		if(props.tool != null) TOOLS.put(block, props.tool);
		for(Gen flag : flags) if(flag == Gen.BEACON) BEACON_BASES.add(block);
		return block;
	}

	/**
	 * BlockEnumMulti: one block per enum value named [name]_[value], one model per value in enum order. The translation
	 * key is [descriptionPrefix][value] (the original's multi names, e.g. "block.hbm.lightstone."), block.hbm.[name] if null.
	 */
	private static <E extends Enum<E>> BlockEnumMulti.Variants<E> multi(String name, Class<E> theEnum, String descriptionPrefix, BlockEnumMulti.VariantFactory<E> factory,
			java.util.function.Supplier<PropsWithTool> props, NtmTab tab, BlockModel... models) {
		BlockEnumMulti.Variants<E> variants = new BlockEnumMulti.Variants<>(name, theEnum);
		for(E value : theEnum.getEnumConstants()) {
			String descriptionId = descriptionPrefix == null ? "block.hbm." + name : descriptionPrefix + value.name().toLowerCase(java.util.Locale.US);
			DeferredBlock<Block> block = register(BlockEnumMulti.Variants.variantName(name, value), p -> factory.create(p, descriptionId, value), props.get(), tab);
			MODELS.put(block, models[value.ordinal()]);
			variants.put(value, block);
		}
		return variants;
	}

	/** Stairs made of another block, with its properties and texture (BlockGenericStairs in the original) */
	private static DeferredBlock<StairBlock> stairs(String name, DeferredBlock<?> base, NtmTab tab, String texture) {
		DeferredBlock<StairBlock> block = BLOCKS.register(name, () -> new StairBlock(base.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(base.get())));
		ModItems.ITEMS.registerSimpleBlockItem(block);
		if(tab != null) tab.add(block);
		MODELS.put(block, BlockModel.stairs(texture));
		Tool tool = TOOLS.get(base);
		if(tool != null) TOOLS.put(block, tool);
		return block;
	}


	/** Block drawn by its tile entity renderer (the original's getRenderType -1), the item uses the NTM item renderer */
	private static <T extends Block> DeferredBlock<T> tileRendered(String name, Function<BlockBehaviour.Properties, T> factory, PropsWithTool props, NtmTab tab, String particle) {
		DeferredBlock<T> block = register(name, factory, props, tab);
		TILE_RENDERED.put(block, particle);
		return block;
	}


	/** Decorative pipe with the textures pipe_top[suffix] and pipe_side[suffix] */
	private static DeferredBlock<com.hbm.blocks.generic.BlockPipe> pipe(String name, com.hbm.blocks.generic.BlockPipe.Style style, String suffix) {
		DeferredBlock<com.hbm.blocks.generic.BlockPipe> block = register(name, p -> new com.hbm.blocks.generic.BlockPipe(p, style, "blocks/pipe_top" + suffix, "blocks/pipe_side" + suffix),
				props(Mat.IRON, 2.0F, 5.0F).sound(ModSoundTypes.GRATE), NtmTab.BLOCKS);
		PIPES.add(block);
		return block;
	}

	private static DeferredBlock<com.hbm.blocks.generic.BlockScaffold> scaffold(String name, String texture) {
		DeferredBlock<com.hbm.blocks.generic.BlockScaffold> block = register(name, p -> new com.hbm.blocks.generic.BlockScaffold(p, "block.hbm.steel_scaffold"), props(Mat.IRON, 5.0F, 15.0F), NtmTab.BLOCKS);
		SCAFFOLDS.put(block, texture);
		return block;
	}

	private static DeferredBlock<NTMAnvil> anvil(String name, int tier, String side, String top) {
		DeferredBlock<NTMAnvil> block = register(name, p -> new NTMAnvil(p, tier), props(Mat.IRON, 5.0F, 100.0F).sound(SoundType.ANVIL), NtmTab.MACHINE);
		ANVILS.put(block, new String[] { side, top });
		return block;
	}

	private static <T extends BlockDummyable> DeferredBlock<T> dummyable(String name, Function<BlockBehaviour.Properties, T> factory, PropsWithTool props, NtmTab tab, String particle) {
		DeferredBlock<T> block = register(name, factory, props, tab);
		DUMMYABLES.add(block);
		TILE_RENDERED.put(block, particle);
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
		SNOW(MapColor.SNOW, Tool.SHOVEL, false),
		GLASS(MapColor.NONE, null, false);

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
