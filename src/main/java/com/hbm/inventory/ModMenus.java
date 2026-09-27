package com.hbm.inventory;

import com.hbm.inventory.container.ContainerBarrel;
import com.hbm.inventory.container.ContainerElectricFurnace;
import com.hbm.inventory.container.ContainerMachineDiesel;
import com.hbm.inventory.container.ContainerAnvil;
import com.hbm.inventory.container.ContainerMachinePress;
import com.hbm.inventory.container.ContainerMachineRefinery;
import com.hbm.inventory.container.ContainerFirebox;
import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.inventory.container.ContainerMachineWoodBurner;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Menu types. Every machine menu is opened with the tile's position in the extra data, the client
 * looks the tile up at that position (the original's GUIHandler did the same with x/y/z).
 */
public class ModMenus {

	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, RefStrings.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerElectricFurnace>> ELECTRIC_FURNACE =
			tile("electric_furnace", TileEntityMachineElectricFurnace.class, ContainerElectricFurnace::new);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerMachineWoodBurner>> WOOD_BURNER =
			tile("machine_wood_burner", TileEntityMachineWoodBurner.class, ContainerMachineWoodBurner::new);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerBarrel>> BARREL =
			tile("barrel", TileEntityBarrel.class, ContainerBarrel::new);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerMachineDiesel>> DIESEL =
			tile("machine_diesel", TileEntityMachineDiesel.class, ContainerMachineDiesel::new);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerMachineOilWell>> OIL_WELL =
			tile("machine_well", com.hbm.tileentity.machine.oil.TileEntityOilDrillBase.class, ContainerMachineOilWell::new);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerFirebox>> FIREBOX =
			tile("heater_firebox", com.hbm.tileentity.machine.TileEntityFireboxBase.class, ContainerFirebox::new);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerMachineRefinery>> REFINERY =
			tile("machine_refinery", com.hbm.tileentity.machine.oil.TileEntityMachineRefinery.class, ContainerMachineRefinery::new);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerMachinePress>> PRESS =
			tile("machine_press", com.hbm.tileentity.machine.TileEntityMachinePress.class, ContainerMachinePress::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineEPress>> EPRESS =
			tile("machine_epress", com.hbm.tileentity.machine.TileEntityMachineEPress.class, com.hbm.inventory.container.ContainerMachineEPress::new);

	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineAssemblyMachine>> ASSEMBLY_MACHINE =
			tile("machine_assembly_machine", com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine.class, com.hbm.inventory.container.ContainerMachineAssemblyMachine::new);

	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineChemicalPlant>> CHEMICAL_PLANT =
			tile("machine_chemical_plant", com.hbm.tileentity.machine.TileEntityMachineChemicalPlant.class, com.hbm.inventory.container.ContainerMachineChemicalPlant::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineChemicalFactory>> CHEMICAL_FACTORY =
			tile("machine_chemical_factory", com.hbm.tileentity.machine.TileEntityMachineChemicalFactory.class, com.hbm.inventory.container.ContainerMachineChemicalFactory::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineAssemblyFactory>> ASSEMBLY_FACTORY =
			tile("machine_assembly_factory", com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory.class, com.hbm.inventory.container.ContainerMachineAssemblyFactory::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachinePUREX>> PUREX =
			tile("machine_purex", com.hbm.tileentity.machine.TileEntityMachinePUREX.class, com.hbm.inventory.container.ContainerMachinePUREX::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineRockMill>> ROCK_MILL =
			tile("machine_rockmill", com.hbm.tileentity.machine.TileEntityMachineRockMill.class, com.hbm.inventory.container.ContainerMachineRockMill::new);

	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerFurnaceSteel>> FURNACE_STEEL =
			tile("furnace_steel", com.hbm.tileentity.machine.TileEntityFurnaceSteel.class, com.hbm.inventory.container.ContainerFurnaceSteel::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerCrucible>> CRUCIBLE =
			tile("machine_crucible", com.hbm.tileentity.machine.TileEntityCrucible.class, com.hbm.inventory.container.ContainerCrucible::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerOilburner>> OILBURNER =
			tile("heater_oilburner", com.hbm.tileentity.machine.TileEntityHeaterOilburner.class, com.hbm.inventory.container.ContainerOilburner::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerHeaterHeatex>> HEATER_HEATEX =
			tile("heater_heatex", com.hbm.tileentity.machine.TileEntityHeaterHeatex.class, com.hbm.inventory.container.ContainerHeaterHeatex::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerCombustionEngine>> COMBUSTION_ENGINE =
			tile("machine_combustion_engine", com.hbm.tileentity.machine.TileEntityMachineCombustionEngine.class, com.hbm.inventory.container.ContainerCombustionEngine::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerCentrifuge>> CENTRIFUGE =
			tile("machine_centrifuge", com.hbm.tileentity.machine.TileEntityMachineCentrifuge.class, com.hbm.inventory.container.ContainerCentrifuge::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerCrystallizer>> CRYSTALLIZER =
			tile("machine_crystallizer", com.hbm.tileentity.machine.TileEntityMachineCrystallizer.class, com.hbm.inventory.container.ContainerCrystallizer::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineFluidTank>> FLUID_TANK =
			tile("machine_fluidtank", com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank.class, com.hbm.inventory.container.ContainerMachineFluidTank::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineGasFlare>> GAS_FLARE =
			tile("machine_flare", com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare.class, com.hbm.inventory.container.ContainerMachineGasFlare::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerOilProcessor>> VACUUM_DISTILL =
			tile("machine_vacuum_distill", com.hbm.tileentity.machine.oil.TileEntityMachineVacuumDistill.class, (id, inv, tile) -> new com.hbm.inventory.container.ContainerOilProcessor(ModMenus.VACUUM_DISTILL.get(), id, inv, tile));
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerOilProcessor>> CATALYTIC_REFORMER =
			tile("machine_catalytic_reformer", com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticReformer.class, (id, inv, tile) -> new com.hbm.inventory.container.ContainerOilProcessor(ModMenus.CATALYTIC_REFORMER.get(), id, inv, tile));
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerOilProcessor>> HYDROTREATER =
			tile("machine_hydrotreater", com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater.class, (id, inv, tile) -> new com.hbm.inventory.container.ContainerOilProcessor(ModMenus.HYDROTREATER.get(), id, inv, tile));
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerLiquefactor>> LIQUEFACTOR =
			tile("machine_liquefactor", com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor.class, com.hbm.inventory.container.ContainerLiquefactor::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerSolidifier>> SOLIDIFIER =
			tile("machine_solidifier", com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier.class, com.hbm.inventory.container.ContainerSolidifier::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineCoker>> COKER =
			tile("machine_coker", com.hbm.tileentity.machine.oil.TileEntityMachineCoker.class, com.hbm.inventory.container.ContainerMachineCoker::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerPyroOven>> PYRO_OVEN =
			tile("machine_pyrooven", com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven.class, com.hbm.inventory.container.ContainerPyroOven::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerElectrolyserFluid>> ELECTROLYSER_FLUID =
			tile("machine_electrolyser_fluid", com.hbm.tileentity.machine.TileEntityElectrolyser.class, com.hbm.inventory.container.ContainerElectrolyserFluid::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerElectrolyserMetal>> ELECTROLYSER_METAL =
			tile("machine_electrolyser_metal", com.hbm.tileentity.machine.TileEntityElectrolyser.class, com.hbm.inventory.container.ContainerElectrolyserMetal::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineGasCent>> GAS_CENT =
			tile("machine_gascent", com.hbm.tileentity.machine.TileEntityMachineGasCent.class, com.hbm.inventory.container.ContainerMachineGasCent::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineTurbineGas>> TURBINE_GAS =
			tile("machine_turbinegas", com.hbm.tileentity.machine.TileEntityMachineTurbineGas.class, com.hbm.inventory.container.ContainerMachineTurbineGas::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerCrate>> CRATE =
			tile("crate", com.hbm.tileentity.machine.storage.TileEntityCrate.class, com.hbm.inventory.container.ContainerCrate::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerBatterySocket>> BATTERY_SOCKET =
			tile("machine_battery_socket", com.hbm.tileentity.machine.storage.TileEntityBatterySocket.class, com.hbm.inventory.container.ContainerBatterySocket::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerBatteryREDD>> BATTERY_REDD =
			tile("machine_battery_redd", com.hbm.tileentity.machine.storage.TileEntityBatteryREDD.class, com.hbm.inventory.container.ContainerBatteryREDD::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineSolderingStation>> SOLDERING_STATION =
			tile("machine_soldering_station", com.hbm.tileentity.machine.TileEntityMachineSolderingStation.class, com.hbm.inventory.container.ContainerMachineSolderingStation::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerCompressor>> COMPRESSOR =
			tile("machine_compressor", com.hbm.tileentity.machine.TileEntityMachineCompressorBase.class, com.hbm.inventory.container.ContainerCompressor::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMixer>> MIXER =
			tile("machine_mixer", com.hbm.tileentity.machine.TileEntityMachineMixer.class, com.hbm.inventory.container.ContainerMixer::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerFurnaceIron>> FURNACE_IRON =
			tile("furnace_iron", com.hbm.tileentity.machine.TileEntityFurnaceIron.class, com.hbm.inventory.container.ContainerFurnaceIron::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerFurnaceBrick>> FURNACE_BRICK =
			tile("furnace_brick", com.hbm.tileentity.machine.TileEntityFurnaceBrick.class, com.hbm.inventory.container.ContainerFurnaceBrick::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerRtgFurnace>> RTG_FURNACE =
			tile("machine_rtg_furnace", com.hbm.tileentity.machine.TileEntityRtgFurnace.class, com.hbm.inventory.container.ContainerRtgFurnace::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerAshpit>> ASHPIT =
			tile("machine_ashpit", com.hbm.tileentity.machine.TileEntityAshpit.class, com.hbm.inventory.container.ContainerAshpit::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineTurbine>> TURBINE =
			tile("machine_turbine", com.hbm.tileentity.machine.TileEntityMachineTurbine.class, com.hbm.inventory.container.ContainerMachineTurbine::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerFunnel>> FUNNEL =
			tile("machine_funnel", com.hbm.tileentity.machine.TileEntityMachineFunnel.class, com.hbm.inventory.container.ContainerFunnel::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineRTG>> RTG =
			tile("machine_rtg_grey", com.hbm.tileentity.machine.TileEntityMachineRTG.class, com.hbm.inventory.container.ContainerMachineRTG::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMicrowave>> MICROWAVE =
			tile("machine_microwave", com.hbm.tileentity.machine.TileEntityMicrowave.class, com.hbm.inventory.container.ContainerMicrowave::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerRadiolysis>> RADIOLYSIS =
			tile("machine_radiolysis", com.hbm.tileentity.machine.TileEntityMachineRadiolysis.class, com.hbm.inventory.container.ContainerRadiolysis::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineExposureChamber>> EXPOSURE_CHAMBER =
			tile("machine_exposure_chamber", com.hbm.tileentity.machine.TileEntityMachineExposureChamber.class, com.hbm.inventory.container.ContainerMachineExposureChamber::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerSILEX>> SILEX =
			tile("machine_silex", com.hbm.tileentity.machine.TileEntitySILEX.class, com.hbm.inventory.container.ContainerSILEX::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerFEL>> FEL =
			tile("machine_fel", com.hbm.tileentity.machine.TileEntityFEL.class, com.hbm.inventory.container.ContainerFEL::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerDiFurnace>> DI_FURNACE =
			tile("machine_difurnace", com.hbm.tileentity.machine.TileEntityDiFurnace.class, com.hbm.inventory.container.ContainerDiFurnace::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerFurnaceCombo>> FURNACE_COMBINATION =
			tile("furnace_combination", com.hbm.tileentity.machine.TileEntityFurnaceCombination.class, com.hbm.inventory.container.ContainerFurnaceCombo::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineRotaryFurnace>> ROTARY_FURNACE =
			tile("machine_rotary_furnace", com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace.class, com.hbm.inventory.container.ContainerMachineRotaryFurnace::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineStrandCaster>> STRAND_CASTER =
			tile("machine_strand_caster", com.hbm.tileentity.machine.TileEntityMachineStrandCaster.class, com.hbm.inventory.container.ContainerMachineStrandCaster::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineArcFurnaceLarge>> ARC_FURNACE =
			tile("machine_arc_furnace", com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge.class, com.hbm.inventory.container.ContainerMachineArcFurnaceLarge::new);
	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineArcWelder>> ARC_WELDER =
			tile("machine_arc_welder", com.hbm.tileentity.machine.TileEntityMachineArcWelder.class, com.hbm.inventory.container.ContainerMachineArcWelder::new);

	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerBlastFurnace>> BLAST_FURNACE =
			tile("machine_blast_furnace", com.hbm.tileentity.machine.TileEntityMachineBlastFurnace.class, com.hbm.inventory.container.ContainerBlastFurnace::new);

	public static final DeferredHolder<MenuType<?>, MenuType<com.hbm.inventory.container.ContainerMachineShredder>> SHREDDER =
			tile("machine_shredder", com.hbm.tileentity.machine.TileEntityMachineShredder.class, com.hbm.inventory.container.ContainerMachineShredder::new);

	/** The anvil has no block entity, its tier comes with the menu */
	public static final DeferredHolder<MenuType<?>, MenuType<ContainerAnvil>> ANVIL = MENUS.register("anvil",
			() -> IMenuTypeExtension.create((int id, Inventory inv, RegistryFriendlyByteBuf buf) -> new ContainerAnvil(id, inv, buf.readInt())));

	@FunctionalInterface
	public interface TileMenuFactory<T extends BlockEntity, M extends AbstractContainerMenu> {
		M create(int id, Inventory inv, T tile);
	}

	private static <T extends BlockEntity, M extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<M>> tile(String name, Class<T> tileClass, TileMenuFactory<T, M> factory) {
		return MENUS.register(name, () -> IMenuTypeExtension.create((int id, Inventory inv, RegistryFriendlyByteBuf buf) -> {
			BlockPos pos = buf.readBlockPos();
			BlockEntity tile = inv.player.level().getBlockEntity(pos);
			if(!tileClass.isInstance(tile)) throw new IllegalStateException("No " + tileClass.getSimpleName() + " at " + pos);
			return factory.create(id, inv, tileClass.cast(tile));
		}));
	}
}
