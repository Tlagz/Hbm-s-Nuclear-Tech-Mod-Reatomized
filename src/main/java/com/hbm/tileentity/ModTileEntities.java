package com.hbm.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineCapacitor.TileEntityCapacitor;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.hbm.tileentity.machine.TileEntityHeaterFirebox;
import com.hbm.tileentity.machine.TileEntityMachinePress;
import com.hbm.tileentity.machine.TileEntityHeatBoiler;
import com.hbm.tileentity.machine.oil.TileEntityMachineOilWell;
import com.hbm.tileentity.machine.oil.TileEntityMachineRefinery;
import com.hbm.tileentity.network.TileEntityCableBaseNT;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTileEntities {

	public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RefStrings.MODID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCableBaseNT>> CABLE = TILES.register("cable",
			() -> BlockEntityType.Builder.of(TileEntityCableBaseNT::new, ModBlocks.red_cable.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCapacitor>> CAPACITOR = TILES.register("capacitor",
			() -> BlockEntityType.Builder.of(TileEntityCapacitor::new, ModBlocks.capacitor_copper.get(), ModBlocks.capacitor_gold.get(),
					ModBlocks.capacitor_niobium.get(), ModBlocks.capacitor_tantalium.get(), ModBlocks.capacitor_schrabidate.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMachinePress>> PRESS = TILES.register("machine_press",
			() -> BlockEntityType.Builder.of(TileEntityMachinePress::new, ModBlocks.machine_press.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineEPress>> EPRESS = TILES.register("machine_epress",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineEPress::new, ModBlocks.machine_epress.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityHeaterFirebox>> FIREBOX = TILES.register("heater_firebox",
			() -> BlockEntityType.Builder.of(TileEntityHeaterFirebox::new, ModBlocks.heater_firebox.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityHeatBoiler>> BOILER = TILES.register("machine_boiler",
			() -> BlockEntityType.Builder.of(TileEntityHeatBoiler::new, ModBlocks.machine_boiler.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine>> ASSEMBLY_MACHINE = TILES.register("machine_assembly_machine",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine::new, ModBlocks.machine_assembly_machine.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineChemicalPlant>> CHEMICAL_PLANT = TILES.register("machine_chemical_plant",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineChemicalPlant::new, ModBlocks.machine_chemical_plant.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineChemicalFactory>> CHEMICAL_FACTORY = TILES.register("machine_chemical_factory",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineChemicalFactory::new, ModBlocks.machine_chemical_factory.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory>> ASSEMBLY_FACTORY = TILES.register("machine_assembly_factory",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory::new, ModBlocks.machine_assembly_factory.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachinePUREX>> PUREX = TILES.register("machine_purex",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachinePUREX::new, ModBlocks.machine_purex.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineRockMill>> ROCK_MILL = TILES.register("machine_rockmill",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineRockMill::new, ModBlocks.machine_rockmill.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityHeaterOven>> HEATER_OVEN = TILES.register("heater_oven",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityHeaterOven::new, ModBlocks.heater_oven.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityAshpit>> ASHPIT = TILES.register("machine_ashpit",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityAshpit::new, ModBlocks.machine_ashpit.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityStirling>> STIRLING = TILES.register("machine_stirling",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityStirling::new, ModBlocks.machine_stirling.get(), ModBlocks.machine_stirling_steel.get(), ModBlocks.machine_stirling_creative.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntitySawmill>> SAWMILL = TILES.register("machine_sawmill",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntitySawmill::new, ModBlocks.machine_sawmill.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityHeaterElectric>> HEATER_ELECTRIC = TILES.register("heater_electric",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityHeaterElectric::new, ModBlocks.heater_electric.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityHeaterOilburner>> HEATER_OILBURNER = TILES.register("heater_oilburner",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityHeaterOilburner::new, ModBlocks.heater_oilburner.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityHeaterHeatex>> HEATER_HEATEX = TILES.register("heater_heatex",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityHeaterHeatex::new, ModBlocks.heater_heatex.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityHeatBoilerIndustrial>> BOILER_INDUSTRIAL = TILES.register("machine_industrial_boiler",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityHeatBoilerIndustrial::new, ModBlocks.machine_industrial_boiler.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachinePumpSteam>> PUMP_STEAM = TILES.register("pump_steam",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachinePumpSteam::new, ModBlocks.pump_steam.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachinePumpElectric>> PUMP_ELECTRIC = TILES.register("pump_electric",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachinePumpElectric::new, ModBlocks.pump_electric.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineCombustionEngine>> COMBUSTION_ENGINE = TILES.register("machine_combustion_engine",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineCombustionEngine::new, ModBlocks.machine_combustion_engine.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineCentrifuge>> CENTRIFUGE = TILES.register("machine_centrifuge",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineCentrifuge::new, ModBlocks.machine_centrifuge.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineCrystallizer>> CRYSTALLIZER = TILES.register("machine_crystallizer",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineCrystallizer::new, ModBlocks.machine_crystallizer.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank>> FLUID_TANK = TILES.register("machine_fluidtank",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank::new, ModBlocks.machine_fluidtank.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.storage.TileEntityMachineBAT9000>> BAT9000 = TILES.register("machine_bat9000",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.storage.TileEntityMachineBAT9000::new, ModBlocks.machine_bat9000.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.storage.TileEntityMachineOrbus>> ORBUS = TILES.register("machine_orbus",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.storage.TileEntityMachineOrbus::new, ModBlocks.machine_orbus.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachineFractionTower>> FRACTION_TOWER = TILES.register("machine_fraction_tower",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachineFractionTower::new, ModBlocks.machine_fraction_tower.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntitySpacer>> SPACER = TILES.register("fraction_spacer",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntitySpacer::new, ModBlocks.fraction_spacer.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticCracker>> CATALYTIC_CRACKER = TILES.register("machine_catalytic_cracker",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticCracker::new, ModBlocks.machine_catalytic_cracker.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachinePumpjack>> PUMPJACK = TILES.register("machine_pumpjack",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachinePumpjack::new, ModBlocks.machine_pumpjack.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachineFrackingTower>> FRACKING_TOWER = TILES.register("machine_fracking_tower",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachineFrackingTower::new, ModBlocks.machine_fracking_tower.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare>> GAS_FLARE = TILES.register("machine_flare",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare::new, ModBlocks.machine_flare.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachineVacuumDistill>> VACUUM_DISTILL = TILES.register("machine_vacuum_distill",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachineVacuumDistill::new, ModBlocks.machine_vacuum_distill.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticReformer>> CATALYTIC_REFORMER = TILES.register("machine_catalytic_reformer",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticReformer::new, ModBlocks.machine_catalytic_reformer.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater>> HYDROTREATER = TILES.register("machine_hydrotreater",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater::new, ModBlocks.machine_hydrotreater.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor>> LIQUEFACTOR = TILES.register("machine_liquefactor",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor::new, ModBlocks.machine_liquefactor.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier>> SOLIDIFIER = TILES.register("machine_solidifier",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier::new, ModBlocks.machine_solidifier.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachineCoker>> COKER = TILES.register("machine_coker",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachineCoker::new, ModBlocks.machine_coker.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven>> PYRO_OVEN = TILES.register("machine_pyrooven",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven::new, ModBlocks.machine_pyrooven.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityElectrolyser>> ELECTROLYSER = TILES.register("machine_electrolyser",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityElectrolyser::new, ModBlocks.machine_electrolyser.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineGasCent>> GAS_CENT = TILES.register("machine_gascent",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineGasCent::new, ModBlocks.machine_gascent.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineTurbineGas>> TURBINE_GAS = TILES.register("machine_turbinegas",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineTurbineGas::new, ModBlocks.machine_turbinegas.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.storage.TileEntityBatterySocket>> BATTERY_SOCKET = TILES.register("machine_battery_socket",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.storage.TileEntityBatterySocket::new, ModBlocks.machine_battery_socket.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.storage.TileEntityBatteryREDD>> BATTERY_REDD = TILES.register("machine_battery_redd",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.storage.TileEntityBatteryREDD::new, ModBlocks.machine_battery_redd.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineSolderingStation>> SOLDERING_STATION = TILES.register("machine_soldering_station",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineSolderingStation::new, ModBlocks.machine_soldering_station.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineCompressor>> COMPRESSOR = TILES.register("machine_compressor",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineCompressor::new, ModBlocks.machine_compressor.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineCompressorCompact>> COMPRESSOR_COMPACT = TILES.register("machine_compressor_compact",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineCompressorCompact::new, ModBlocks.machine_compressor_compact.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineMixer>> MIXER = TILES.register("machine_mixer",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineMixer::new, ModBlocks.machine_mixer.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFurnaceIron>> FURNACE_IRON = TILES.register("furnace_iron",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFurnaceIron::new, ModBlocks.furnace_iron.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFurnaceBrick>> FURNACE_BRICK = TILES.register("furnace_brick",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFurnaceBrick::new, ModBlocks.machine_furnace_brick_off.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityRtgFurnace>> RTG_FURNACE = TILES.register("machine_rtg_furnace",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityRtgFurnace::new, ModBlocks.machine_rtg_furnace_off.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineTurbine>> TURBINE = TILES.register("machine_turbine",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineTurbine::new, ModBlocks.machine_turbine.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineFunnel>> FUNNEL = TILES.register("machine_funnel",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineFunnel::new, ModBlocks.machine_funnel.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineRTG>> RTG = TILES.register("machine_rtg_grey",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineRTG::new, ModBlocks.machine_rtg_grey.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityCharger>> CHARGER = TILES.register("charger",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityCharger::new, ModBlocks.charger.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMicrowave>> MICROWAVE = TILES.register("machine_microwave",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMicrowave::new, ModBlocks.machine_microwave.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineAutosaw>> AUTOSAW = TILES.register("machine_autosaw",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineAutosaw::new, ModBlocks.machine_autosaw.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineThresher>> THRESHER = TILES.register("machine_thresher",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineThresher::new, ModBlocks.machine_thresher.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineRadGen>> RADGEN = TILES.register("machine_radgen",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineRadGen::new, ModBlocks.machine_radgen.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineTurbofan>> TURBOFAN = TILES.register("machine_turbofan",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineTurbofan::new, ModBlocks.machine_turbofan.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntitySolarBoiler>> SOLAR_BOILER = TILES.register("machine_solar_boiler",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntitySolarBoiler::new, ModBlocks.machine_solar_boiler.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntitySolarMirror>> SOLAR_MIRROR = TILES.register("solar_mirror",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntitySolarMirror::new, ModBlocks.solar_mirror.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityTesla>> TESLA = TILES.register("tesla",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityTesla::new, ModBlocks.tesla.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityDecon>> DECON = TILES.register("decon",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityDecon::new, ModBlocks.decon.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineSiren>> SIREN = TILES.register("machine_siren",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineSiren::new, ModBlocks.machine_siren.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineMiningLaser>> MINING_LASER = TILES.register("machine_mining_laser",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineMiningLaser::new, ModBlocks.machine_mining_laser.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityChimneyBrick>> CHIMNEY_BRICK = TILES.register("chimney_brick",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityChimneyBrick::new, ModBlocks.chimney_brick.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityChimneyIndustrial>> CHIMNEY_INDUSTRIAL = TILES.register("chimney_industrial",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityChimneyIndustrial::new, ModBlocks.chimney_industrial.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineRadiolysis>> RADIOLYSIS = TILES.register("machine_radiolysis",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineRadiolysis::new, ModBlocks.machine_radiolysis.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineExposureChamber>> EXPOSURE_CHAMBER = TILES.register("machine_exposure_chamber",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineExposureChamber::new, ModBlocks.machine_exposure_chamber.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntitySILEX>> SILEX = TILES.register("machine_silex",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntitySILEX::new, ModBlocks.machine_silex.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFEL>> FEL = TILES.register("machine_fel",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFEL::new, ModBlocks.machine_fel.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineIntake>> INTAKE = TILES.register("machine_intake",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineIntake::new, ModBlocks.machine_intake.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineDrain>> DRAIN = TILES.register("machine_drain",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineDrain::new, ModBlocks.machine_drain.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityDeuteriumExtractor>> DEUTERIUM_EXTRACTOR = TILES.register("machine_deuterium_extractor",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityDeuteriumExtractor::new, ModBlocks.machine_deuterium_extractor.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityDeuteriumTower>> DEUTERIUM_TOWER = TILES.register("machine_deuterium_tower",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityDeuteriumTower::new, ModBlocks.machine_deuterium_tower.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityDiFurnace>> DI_FURNACE = TILES.register("machine_difurnace",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityDiFurnace::new, ModBlocks.machine_difurnace_off.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFurnaceCombination>> FURNACE_COMBINATION = TILES.register("furnace_combination",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFurnaceCombination::new, ModBlocks.furnace_combination.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace>> ROTARY_FURNACE = TILES.register("machine_rotary_furnace",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace::new, ModBlocks.machine_rotary_furnace.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineStrandCaster>> STRAND_CASTER = TILES.register("machine_strand_caster",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineStrandCaster::new, ModBlocks.machine_strand_caster.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.storage.TileEntityCrate>> CRATE = TILES.register("crate",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.storage.TileEntityCrate::new, ModBlocks.crate_iron.get(), ModBlocks.crate_steel.get(), ModBlocks.crate_desh.get(), ModBlocks.crate_tungsten.get(), ModBlocks.safe.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFurnaceSteel>> FURNACE_STEEL = TILES.register("furnace_steel",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFurnaceSteel::new, ModBlocks.furnace_steel.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntitySteamEngine>> STEAM_ENGINE = TILES.register("machine_steam_engine",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntitySteamEngine::new, ModBlocks.machine_steam_engine.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityCondenser>> CONDENSER = TILES.register("machine_condenser",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityCondenser::new, ModBlocks.machine_condenser.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityCondenserPowered>> CONDENSER_POWERED = TILES.register("machine_condenser_powered",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityCondenserPowered::new, ModBlocks.machine_condenser_powered.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityTowerSmall>> TOWER_SMALL = TILES.register("machine_tower_small",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityTowerSmall::new, ModBlocks.machine_tower_small.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityTowerLarge>> TOWER_LARGE = TILES.register("machine_tower_large",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityTowerLarge::new, ModBlocks.machine_tower_large.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineIndustrialTurbine>> INDUSTRIAL_TURBINE = TILES.register("machine_industrial_turbine",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineIndustrialTurbine::new, ModBlocks.machine_industrial_turbine.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityChungus>> CHUNGUS = TILES.register("machine_chungus",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityChungus::new, ModBlocks.machine_chungus.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityCrucible>> CRUCIBLE = TILES.register("machine_crucible",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityCrucible::new, ModBlocks.machine_crucible.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFoundryMold>> FOUNDRY_MOLD = TILES.register("foundry_mold",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFoundryMold::new, ModBlocks.foundry_mold.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFoundryBasin>> FOUNDRY_BASIN = TILES.register("foundry_basin",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFoundryBasin::new, ModBlocks.foundry_basin.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFoundryChannel>> FOUNDRY_CHANNEL = TILES.register("foundry_channel",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFoundryChannel::new, ModBlocks.foundry_channel.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFoundryOutlet>> FOUNDRY_OUTLET = TILES.register("foundry_outlet",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFoundryOutlet::new, ModBlocks.foundry_outlet.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFoundrySlagtap>> FOUNDRY_SLAGTAP = TILES.register("foundry_slagtap",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFoundrySlagtap::new, ModBlocks.foundry_slagtap.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityFoundryTank>> FOUNDRY_TANK = TILES.register("foundry_tank",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityFoundryTank::new, ModBlocks.foundry_tank.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntitySlag>> SLAG = TILES.register("slag",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntitySlag::new, ModBlocks.slag.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge>> ARC_FURNACE = TILES.register("machine_arc_furnace",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge::new, ModBlocks.machine_arc_furnace.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineArcWelder>> ARC_WELDER = TILES.register("machine_arc_welder",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineArcWelder::new, ModBlocks.machine_arc_welder.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineBlastFurnace>> BLAST_FURNACE = TILES.register("machine_blast_furnace",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineBlastFurnace::new, ModBlocks.machine_blast_furnace.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.hbm.tileentity.machine.TileEntityMachineShredder>> SHREDDER = TILES.register("machine_shredder",
			() -> BlockEntityType.Builder.of(com.hbm.tileentity.machine.TileEntityMachineShredder::new, ModBlocks.machine_shredder.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMachineRefinery>> REFINERY = TILES.register("machine_refinery",
			() -> BlockEntityType.Builder.of(TileEntityMachineRefinery::new, ModBlocks.machine_refinery.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMachineOilWell>> OIL_WELL = TILES.register("machine_well",
			() -> BlockEntityType.Builder.of(TileEntityMachineOilWell::new, ModBlocks.machine_well.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMachineDiesel>> DIESEL = TILES.register("machine_diesel",
			() -> BlockEntityType.Builder.of(TileEntityMachineDiesel::new, ModBlocks.machine_diesel.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityBarrel>> BARREL = TILES.register("barrel",
			() -> BlockEntityType.Builder.of(TileEntityBarrel::new, ModBlocks.barrel_plastic.get(), ModBlocks.barrel_steel.get(), ModBlocks.barrel_tcalloy.get(), ModBlocks.barrel_antimatter.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityPipeBaseNT>> PIPE = TILES.register("pipe",
			() -> BlockEntityType.Builder.of(TileEntityPipeBaseNT::new, ModBlocks.fluid_duct_neo.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityProxyCombo>> PROXY_COMBO = TILES.register("proxy_combo",
			() -> BlockEntityType.Builder.of(TileEntityProxyCombo::new, java.util.stream.Stream.concat(ModBlocks.DUMMYABLES.stream().map(b -> (net.minecraft.world.level.block.Block) b.get()), java.util.stream.Stream.of(ModBlocks.machine_difurnace_extension.get())).toArray(net.minecraft.world.level.block.Block[]::new)).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMachineWoodBurner>> WOOD_BURNER = TILES.register("machine_wood_burner",
			() -> BlockEntityType.Builder.of(TileEntityMachineWoodBurner::new, ModBlocks.machine_wood_burner.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMachineElectricFurnace>> ELECTRIC_FURNACE = TILES.register("machine_electric_furnace",
			() -> BlockEntityType.Builder.of(TileEntityMachineElectricFurnace::new, ModBlocks.machine_electric_furnace_off.get()).build(null));
}
