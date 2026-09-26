package com.hbm.main;

import com.hbm.inventory.ModMenus;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMachineElectricFurnace;
import com.hbm.inventory.gui.GUIMachineWoodBurner;
import com.hbm.render.item.NTMItemRenderer;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.tileentity.RenderWoodBurner;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.lib.RefStrings;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Client-only mod bus events (screens, renderers), counterpart of the original's ClientProxy registration */
@EventBusSubscriber(modid = RefStrings.MODID, value = Dist.CLIENT)
public class ModEventHandlerClientMod {

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(ModMenus.ELECTRIC_FURNACE.get(), GUIMachineElectricFurnace::new);
		event.register(ModMenus.WOOD_BURNER.get(), GUIMachineWoodBurner::new);
		event.register(ModMenus.BARREL.get(), com.hbm.inventory.gui.GUIBarrel::new);
		event.register(ModMenus.DIESEL.get(), com.hbm.inventory.gui.GUIMachineDiesel::new);
		event.register(ModMenus.OIL_WELL.get(), com.hbm.inventory.gui.GUIMachineOilWell::new);
		event.register(ModMenus.FIREBOX.get(), com.hbm.inventory.gui.GUIFirebox::new);
		event.register(ModMenus.REFINERY.get(), com.hbm.inventory.gui.GUIMachineRefinery::new);
		event.register(ModMenus.PRESS.get(), com.hbm.inventory.gui.GUIMachinePress::new);
		event.register(ModMenus.ANVIL.get(), com.hbm.inventory.gui.GUIAnvil::new);
		event.register(ModMenus.ASSEMBLY_MACHINE.get(), com.hbm.inventory.gui.GUIMachineAssemblyMachine::new);
		event.register(ModMenus.CHEMICAL_PLANT.get(), com.hbm.inventory.gui.GUIMachineChemicalPlant::new);
		event.register(ModMenus.ARC_WELDER.get(), com.hbm.inventory.gui.GUIMachineArcWelder::new);
		event.register(ModMenus.ARC_FURNACE.get(), com.hbm.inventory.gui.GUIMachineArcFurnaceLarge::new);
		event.register(ModMenus.CRUCIBLE.get(), com.hbm.inventory.gui.GUICrucible::new);
		event.register(ModMenus.FURNACE_STEEL.get(), com.hbm.inventory.gui.GUIFurnaceSteel::new);
		event.register(ModMenus.OILBURNER.get(), com.hbm.inventory.gui.GUIOilburner::new);
		event.register(ModMenus.HEATER_HEATEX.get(), com.hbm.inventory.gui.GUIHeaterHeatex::new);
		event.register(ModMenus.COMBUSTION_ENGINE.get(), com.hbm.inventory.gui.GUICombustionEngine::new);
		event.register(ModMenus.CENTRIFUGE.get(), com.hbm.inventory.gui.GUIMachineCentrifuge::new);
		event.register(ModMenus.CRYSTALLIZER.get(), com.hbm.inventory.gui.GUICrystallizer::new);
		event.register(ModMenus.FLUID_TANK.get(), com.hbm.inventory.gui.GUIMachineFluidTank::new);
		event.register(ModMenus.GAS_FLARE.get(), com.hbm.inventory.gui.GUIMachineGasFlare::new);
		event.register(ModMenus.VACUUM_DISTILL.get(), com.hbm.inventory.gui.GUIOilProcessor::vacuumDistill);
		event.register(ModMenus.CATALYTIC_REFORMER.get(), com.hbm.inventory.gui.GUIOilProcessor::catalyticReformer);
		event.register(ModMenus.HYDROTREATER.get(), com.hbm.inventory.gui.GUIOilProcessor::hydrotreater);
		event.<com.hbm.inventory.container.ContainerLiquefactor, com.hbm.inventory.gui.GUILiquefactor<com.hbm.inventory.container.ContainerLiquefactor>>register(ModMenus.LIQUEFACTOR.get(), com.hbm.inventory.gui.GUILiquefactor::liquefactor);
		event.<com.hbm.inventory.container.ContainerSolidifier, com.hbm.inventory.gui.GUILiquefactor<com.hbm.inventory.container.ContainerSolidifier>>register(ModMenus.SOLIDIFIER.get(), com.hbm.inventory.gui.GUILiquefactor::solidifier);
		event.register(ModMenus.COKER.get(), com.hbm.inventory.gui.GUIMachineCoker::new);
		event.register(ModMenus.PYRO_OVEN.get(), com.hbm.inventory.gui.GUIPyroOven::new);
		event.register(ModMenus.ELECTROLYSER_FLUID.get(), com.hbm.inventory.gui.GUIElectrolyserFluid::new);
		event.register(ModMenus.ELECTROLYSER_METAL.get(), com.hbm.inventory.gui.GUIElectrolyserMetal::new);
		event.register(ModMenus.GAS_CENT.get(), com.hbm.inventory.gui.GUIMachineGasCent::new);
		event.register(ModMenus.TURBINE_GAS.get(), com.hbm.inventory.gui.GUIMachineTurbineGas::new);
		event.register(ModMenus.CRATE.get(), com.hbm.inventory.gui.GUICrate::new);
		event.register(ModMenus.BLAST_FURNACE.get(), com.hbm.inventory.gui.GUIBlastFurnace::new);
		event.register(ModMenus.SHREDDER.get(), com.hbm.inventory.gui.GUIMachineShredder::new);
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModTileEntities.WOOD_BURNER.get(), RenderWoodBurner::new);
		event.registerBlockEntityRenderer(ModTileEntities.DIESEL.get(), com.hbm.render.tileentity.RenderDieselGen::new);
		event.registerBlockEntityRenderer(ModTileEntities.OIL_WELL.get(), com.hbm.render.tileentity.RenderDerrick::new);
		event.registerBlockEntityRenderer(ModTileEntities.FIREBOX.get(), com.hbm.render.tileentity.RenderFirebox::new);
		event.registerBlockEntityRenderer(ModTileEntities.BOILER.get(), com.hbm.render.tileentity.RenderBoiler::new);
		event.registerBlockEntityRenderer(ModTileEntities.REFINERY.get(), com.hbm.render.tileentity.RenderRefinery::new);
		event.registerBlockEntityRenderer(ModTileEntities.PRESS.get(), com.hbm.render.tileentity.RenderPress::new);
		event.registerBlockEntityRenderer(ModTileEntities.ASSEMBLY_MACHINE.get(), com.hbm.render.tileentity.RenderAssemblyMachine::new);
		event.registerBlockEntityRenderer(ModTileEntities.CHEMICAL_PLANT.get(), com.hbm.render.tileentity.RenderChemicalPlant::new);
		event.registerBlockEntityRenderer(ModTileEntities.ARC_WELDER.get(), com.hbm.render.tileentity.RenderArcWelder::new);
		event.registerBlockEntityRenderer(ModTileEntities.ARC_FURNACE.get(), com.hbm.render.tileentity.RenderArcFurnace::new);
		event.registerBlockEntityRenderer(ModTileEntities.CRUCIBLE.get(), com.hbm.render.tileentity.RenderCrucible::new);
		event.registerBlockEntityRenderer(ModTileEntities.STIRLING.get(), com.hbm.render.tileentity.RenderStirling::new);
		event.registerBlockEntityRenderer(ModTileEntities.HEATER_OVEN.get(), com.hbm.render.tileentity.RenderHeatingOven::new);
		event.registerBlockEntityRenderer(ModTileEntities.HEATER_ELECTRIC.get(), com.hbm.render.tileentity.RenderElectricHeater::new);
		event.registerBlockEntityRenderer(ModTileEntities.HEATER_OILBURNER.get(), com.hbm.render.tileentity.RenderOilburner::new);
		event.registerBlockEntityRenderer(ModTileEntities.HEATER_HEATEX.get(), com.hbm.render.tileentity.RenderHeaterHeatex::new);
		event.registerBlockEntityRenderer(ModTileEntities.BOILER_INDUSTRIAL.get(), com.hbm.render.tileentity.RenderIndustrialBoiler::new);
		event.registerBlockEntityRenderer(ModTileEntities.PUMP_STEAM.get(), com.hbm.render.tileentity.RenderPump::new);
		event.registerBlockEntityRenderer(ModTileEntities.PUMP_ELECTRIC.get(), com.hbm.render.tileentity.RenderPump::new);
		event.registerBlockEntityRenderer(ModTileEntities.COMBUSTION_ENGINE.get(), com.hbm.render.tileentity.RenderCombustionEngine::new);
		event.registerBlockEntityRenderer(ModTileEntities.CENTRIFUGE.get(), com.hbm.render.tileentity.RenderCentrifuge::new);
		event.registerBlockEntityRenderer(ModTileEntities.CRYSTALLIZER.get(), com.hbm.render.tileentity.RenderCrystallizer::new);
		event.registerBlockEntityRenderer(ModTileEntities.FLUID_TANK.get(), com.hbm.render.tileentity.RenderFluidTank::new);
		event.registerBlockEntityRenderer(ModTileEntities.BAT9000.get(), com.hbm.render.tileentity.RenderBigTanks.BAT9000::new);
		event.registerBlockEntityRenderer(ModTileEntities.ORBUS.get(), com.hbm.render.tileentity.RenderBigTanks.Orbus::new);
		event.registerBlockEntityRenderer(ModTileEntities.FRACTION_TOWER.get(), com.hbm.render.tileentity.RenderOilTowers.FractionTower::new);
		event.registerBlockEntityRenderer(ModTileEntities.SPACER.get(), com.hbm.render.tileentity.RenderOilTowers.Spacer::new);
		event.registerBlockEntityRenderer(ModTileEntities.CATALYTIC_CRACKER.get(), com.hbm.render.tileentity.RenderOilTowers.Cracker::new);
		event.registerBlockEntityRenderer(ModTileEntities.PUMPJACK.get(), com.hbm.render.tileentity.RenderPumpjack::new);
		event.registerBlockEntityRenderer(ModTileEntities.FRACKING_TOWER.get(), com.hbm.render.tileentity.RenderFrackingTower::new);
		event.registerBlockEntityRenderer(ModTileEntities.GAS_FLARE.get(), com.hbm.render.tileentity.RenderGasFlare::new);
		event.registerBlockEntityRenderer(ModTileEntities.VACUUM_DISTILL.get(), com.hbm.render.tileentity.RenderOilProcessors.vacuumDistill());
		event.registerBlockEntityRenderer(ModTileEntities.CATALYTIC_REFORMER.get(), com.hbm.render.tileentity.RenderOilProcessors.catalyticReformer());
		event.registerBlockEntityRenderer(ModTileEntities.HYDROTREATER.get(), com.hbm.render.tileentity.RenderOilProcessors.hydrotreater());
		event.registerBlockEntityRenderer(ModTileEntities.LIQUEFACTOR.get(), com.hbm.render.tileentity.RenderLiquefactor.liquefactor());
		event.registerBlockEntityRenderer(ModTileEntities.SOLIDIFIER.get(), com.hbm.render.tileentity.RenderLiquefactor.solidifier());
		event.registerBlockEntityRenderer(ModTileEntities.COKER.get(), com.hbm.render.tileentity.RenderCoker::new);
		event.registerBlockEntityRenderer(ModTileEntities.PYRO_OVEN.get(), com.hbm.render.tileentity.RenderPyroOven::new);
		event.registerBlockEntityRenderer(ModTileEntities.ELECTROLYSER.get(), com.hbm.render.tileentity.RenderElectrolyser::new);
		event.registerBlockEntityRenderer(ModTileEntities.GAS_CENT.get(), com.hbm.render.tileentity.RenderGasCent::new);
		event.registerBlockEntityRenderer(ModTileEntities.TURBINE_GAS.get(), com.hbm.render.tileentity.RenderTurbineGas::new);
		event.registerBlockEntityRenderer(ModTileEntities.FURNACE_STEEL.get(), com.hbm.render.tileentity.RenderFurnaceSteel::new);
		event.registerBlockEntityRenderer(ModTileEntities.STEAM_ENGINE.get(), com.hbm.render.tileentity.RenderSteamEngine::new);
		event.registerBlockEntityRenderer(ModTileEntities.CONDENSER_POWERED.get(), com.hbm.render.tileentity.RenderCondenser::new);
		event.registerBlockEntityRenderer(ModTileEntities.TOWER_SMALL.get(), com.hbm.render.tileentity.RenderCoolingTower.small());
		event.registerBlockEntityRenderer(ModTileEntities.TOWER_LARGE.get(), com.hbm.render.tileentity.RenderCoolingTower.large());
		event.registerBlockEntityRenderer(ModTileEntities.INDUSTRIAL_TURBINE.get(), com.hbm.render.tileentity.RenderIndustrialTurbine::new);
		event.registerBlockEntityRenderer(ModTileEntities.CHUNGUS.get(), com.hbm.render.tileentity.RenderChungus::new);
		event.registerEntityRenderer(com.hbm.entity.ModEntities.COG.get(), com.hbm.render.entity.RenderCog::new);
		event.registerBlockEntityRenderer(ModTileEntities.FOUNDRY_MOLD.get(), com.hbm.render.tileentity.RenderFoundry::new);
		event.registerBlockEntityRenderer(ModTileEntities.FOUNDRY_BASIN.get(), com.hbm.render.tileentity.RenderFoundry::new);
		event.registerBlockEntityRenderer(ModTileEntities.FOUNDRY_CHANNEL.get(), com.hbm.render.tileentity.RenderFoundry::new);
		event.registerBlockEntityRenderer(ModTileEntities.BLAST_FURNACE.get(), com.hbm.render.tileentity.RenderBlastFurnace::new);
	}

	@SubscribeEvent
	public static void registerParticles(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(com.hbm.particle.ModParticles.BASE.get(), sprites -> {
			com.hbm.particle.ParticleEffectsNT.baseSprites = sprites;
			return (type, level, x, y, z, mx, my, mz) -> new com.hbm.particle.ParticleCoolingTower(level, x, y, z, sprites);
		});
		event.registerSpriteSet(com.hbm.particle.ModParticles.GAS_FLAME.get(), sprites -> {
			com.hbm.particle.ParticleEffectsNT.gasFlameSprites = sprites;
			return (type, level, x, y, z, mx, my, mz) -> new com.hbm.particle.ParticleGasFlame(level, x, y, z, mx, my, mz, 6.5F, sprites);
		});
	}

	@SubscribeEvent
	public static void registerItemRenderers(RegisterClientExtensionsEvent event) {
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_wood_burner.get().asItem(), RenderWoodBurner.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_diesel.get().asItem(), com.hbm.render.tileentity.RenderDieselGen.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_well.get().asItem(), com.hbm.render.tileentity.RenderDerrick.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.heater_firebox.get().asItem(), com.hbm.render.tileentity.RenderFirebox.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_boiler.get().asItem(), com.hbm.render.tileentity.RenderBoiler.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_refinery.get().asItem(), com.hbm.render.tileentity.RenderRefinery.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_press.get().asItem(), com.hbm.render.tileentity.RenderPress.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_assembly_machine.get().asItem(), com.hbm.render.tileentity.RenderAssemblyMachine.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_chemical_plant.get().asItem(), com.hbm.render.tileentity.RenderChemicalPlant.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_arc_welder.get().asItem(), com.hbm.render.tileentity.RenderArcWelder.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_arc_furnace.get().asItem(), com.hbm.render.tileentity.RenderArcFurnace.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_crucible.get().asItem(), com.hbm.render.tileentity.RenderCrucible.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.heater_oven.get().asItem(), com.hbm.render.tileentity.RenderHeatingOven.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.heater_electric.get().asItem(), com.hbm.render.tileentity.RenderElectricHeater.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.heater_oilburner.get().asItem(), com.hbm.render.tileentity.RenderOilburner.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.heater_heatex.get().asItem(), com.hbm.render.tileentity.RenderHeaterHeatex.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_industrial_boiler.get().asItem(), com.hbm.render.tileentity.RenderIndustrialBoiler.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.pump_steam.get().asItem(), com.hbm.render.tileentity.RenderPump.itemRenderer(0));
		NTMItemRenderer.RENDERERS.put(ModBlocks.pump_electric.get().asItem(), com.hbm.render.tileentity.RenderPump.itemRenderer(1));
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_combustion_engine.get().asItem(), com.hbm.render.tileentity.RenderCombustionEngine.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_centrifuge.get().asItem(), com.hbm.render.tileentity.RenderCentrifuge.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_crystallizer.get().asItem(), com.hbm.render.tileentity.RenderCrystallizer.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_fluidtank.get().asItem(), com.hbm.render.tileentity.RenderFluidTank.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_bat9000.get().asItem(), com.hbm.render.tileentity.RenderBigTanks.BAT9000.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_orbus.get().asItem(), com.hbm.render.tileentity.RenderBigTanks.Orbus.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_fraction_tower.get().asItem(), com.hbm.render.tileentity.RenderOilTowers.FractionTower.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.fraction_spacer.get().asItem(), com.hbm.render.tileentity.RenderOilTowers.Spacer.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_catalytic_cracker.get().asItem(), com.hbm.render.tileentity.RenderOilTowers.Cracker.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_pumpjack.get().asItem(), com.hbm.render.tileentity.RenderPumpjack.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_fracking_tower.get().asItem(), com.hbm.render.tileentity.RenderFrackingTower.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_flare.get().asItem(), com.hbm.render.tileentity.RenderGasFlare.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_vacuum_distill.get().asItem(), com.hbm.render.tileentity.RenderOilProcessors.itemRenderer(com.hbm.main.ResourceManager.vacuum_distill, com.hbm.main.ResourceManager.vacuum_distill_tex, 4, 3F));
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_catalytic_reformer.get().asItem(), com.hbm.render.tileentity.RenderOilProcessors.itemRenderer(com.hbm.main.ResourceManager.catalytic_reformer, com.hbm.main.ResourceManager.catalytic_reformer_tex, 3, 3.5F));
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_hydrotreater.get().asItem(), com.hbm.render.tileentity.RenderOilProcessors.itemRenderer(com.hbm.main.ResourceManager.hydrotreater, com.hbm.main.ResourceManager.hydrotreater_tex, 4, 4F));
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_liquefactor.get().asItem(), com.hbm.render.tileentity.RenderLiquefactor.itemRenderer(com.hbm.main.ResourceManager.liquefactor, com.hbm.main.ResourceManager.liquefactor_tex));
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_solidifier.get().asItem(), com.hbm.render.tileentity.RenderLiquefactor.itemRenderer(com.hbm.main.ResourceManager.solidifier, com.hbm.main.ResourceManager.solidifier_tex));
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_coker.get().asItem(), com.hbm.render.tileentity.RenderCoker.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_pyrooven.get().asItem(), com.hbm.render.tileentity.RenderPyroOven.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_electrolyser.get().asItem(), com.hbm.render.tileentity.RenderElectrolyser.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_gascent.get().asItem(), com.hbm.render.tileentity.RenderGasCent.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_turbinegas.get().asItem(), com.hbm.render.tileentity.RenderTurbineGas.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.furnace_steel.get().asItem(), com.hbm.render.tileentity.RenderFurnaceSteel.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_steam_engine.get().asItem(), com.hbm.render.tileentity.RenderSteamEngine.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_condenser_powered.get().asItem(), com.hbm.render.tileentity.RenderCondenser.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_tower_small.get().asItem(), com.hbm.render.tileentity.RenderCoolingTower.itemRendererSmall());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_tower_large.get().asItem(), com.hbm.render.tileentity.RenderCoolingTower.itemRendererLarge());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_industrial_turbine.get().asItem(), com.hbm.render.tileentity.RenderIndustrialTurbine.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_chungus.get().asItem(), com.hbm.render.tileentity.RenderChungus.itemRenderer());
		for(var stirling : java.util.List.of(ModBlocks.machine_stirling, ModBlocks.machine_stirling_steel, ModBlocks.machine_stirling_creative)) {
			NTMItemRenderer.RENDERERS.put(stirling.get().asItem(), com.hbm.render.tileentity.RenderStirling.itemRenderer(stirling.get()));
		}
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_blast_furnace.get().asItem(), com.hbm.render.tileentity.RenderBlastFurnace.itemRenderer());

		IClientItemExtensions extension = new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return NTMItemRenderer.get();
			}
		};
		for(var block : ModBlocks.TILE_RENDERED.keySet()) event.registerItem(extension, block.get().asItem());

		var batteryRenderer = new com.hbm.render.item.ItemRenderBatteryPack();
		for(var pack : com.hbm.items.ModItems.battery_pack.values()) NTMItemRenderer.RENDERERS.put(pack.get(), batteryRenderer);
		for(var item : com.hbm.items.ModItems.ITEM_RENDERED) event.registerItem(extension, item.get());
	}

	/** Tinted layers of fluid containers, the original's getColorFromItemStack(stack, pass) */
	@SubscribeEvent
	public static void registerItemColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
		event.register((stack, tint) -> 0xFF000000 | com.hbm.items.machine.ItemCanister.getColor(stack, tint), com.hbm.items.ModItems.canister_full.get());
		event.register((stack, tint) -> 0xFF000000 | com.hbm.items.machine.ItemGasTank.getColor(stack, tint), com.hbm.items.ModItems.gas_full.get());
		event.register((stack, tint) -> 0xFF000000 | com.hbm.items.machine.ItemFluidTank.getColor(stack, tint),
				com.hbm.items.ModItems.fluid_tank_full.get(), com.hbm.items.ModItems.fluid_tank_lead_full.get(), com.hbm.items.ModItems.fluid_barrel_full.get());
		event.register((stack, tint) -> 0xFF000000 | com.hbm.items.machine.ItemFluidIDMulti.getColor(stack, tint), com.hbm.items.ModItems.fluid_identifier_multi.get());
		event.register((stack, tint) -> 0xFF000000 | com.hbm.items.machine.ItemFluidIcon.getColor(stack), com.hbm.items.ModItems.fluid_icon.get());
		// autogen items without their own or a recolored texture are tinted with the molten color
		for(var set : com.hbm.items.ModItems.AUTOGEN) for(var mat : set.materials()) {
			if(set.isTinted(mat)) event.register((stack, tint) -> 0xFF000000 | mat.moltenColor, set.get(mat).get());
		}
		// ore byproducts tint the whole texture, chemical dyes and crayons only the overlay
		for(var type : com.hbm.items.special.ItemByproduct.EnumByproduct.values()) {
			event.register((stack, tint) -> 0xFF000000 | type.color, com.hbm.items.ModItems.ore_byproduct.get(type).get());
		}
		for(var dye : com.hbm.items.machine.ItemChemicalDye.EnumChemDye.values()) {
			event.register((stack, tint) -> 0xFF000000 | (tint == 1 ? dye.color : 0xFFFFFF), com.hbm.items.ModItems.chemical_dye.get(dye).get(), com.hbm.items.ModItems.crayon.get(dye).get());
		}
		// scraps in their material's color
		event.register((stack, tint) -> 0xFF000000 | com.hbm.items.machine.ItemScraps.getColor(stack), com.hbm.items.ModItems.scraps.get());
		// the original's inventory pipe uses the color of NONE
		event.register((stack, tint) -> 0xFF000000 | (tint == 0 ? com.hbm.inventory.fluid.Fluids.NONE.getColor() : 0xFFFFFF), com.hbm.blocks.ModBlocks.fluid_duct_neo.get());
	}

	/** Pipe overlays are tinted with the pipe's fluid color, read from the tile entity */
	@SubscribeEvent
	public static void registerBlockColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block event) {
		event.register((state, world, pos, tint) -> {
			if(tint != 0 || world == null || pos == null) return 0xFFFFFF;
			return com.hbm.blocks.network.FluidDuctStandard.getType(world, pos).getColor();
		}, com.hbm.blocks.ModBlocks.fluid_duct_neo.get());
	}

	@SubscribeEvent
	public static void clientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			// item model overrides pick the pipe texture by the placed style
			net.minecraft.client.renderer.item.ItemProperties.register(com.hbm.blocks.ModBlocks.fluid_duct_neo.get().asItem(), com.hbm.lib.RefStrings.loc("style"),
					(stack, level, entity, seed) -> com.hbm.items.block.ItemBlockStyled.getValue(stack, com.hbm.blocks.network.FluidDuctStandard.STYLE));
			// scraps texture: solid, liquid or liquid additive
			net.minecraft.client.renderer.item.ItemProperties.register(com.hbm.items.ModItems.scraps.get(), com.hbm.lib.RefStrings.loc("scrap_type"),
					(stack, level, entity, seed) -> com.hbm.items.machine.ItemScraps.getModelType(stack));
			// blueprint texture by pool type
			net.minecraft.client.renderer.item.ItemProperties.register(com.hbm.items.ModItems.blueprints.get(), com.hbm.lib.RefStrings.loc("pool"),
					(stack, level, entity, seed) -> com.hbm.items.machine.ItemBlueprints.poolType(stack));
		});
	}

	/** The original's RenderGameOverlayEvent hook for ILookOverlay blocks and items, drawn above the crosshair */
	@SubscribeEvent
	public static void registerGuiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
		event.registerAbove(net.neoforged.neoforge.client.gui.VanillaGuiLayers.CROSSHAIR, com.hbm.lib.RefStrings.loc("look_overlay"), (graphics, delta) -> {
			net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
			if(mc.level == null || mc.player == null || mc.options.hideGui || mc.screen != null) return;
			if(!(mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit) || hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return;

			net.minecraft.core.BlockPos pos = hit.getBlockPos();
			if(mc.player.getMainHandItem().getItem() instanceof com.hbm.blocks.ILookOverlay overlay) {
				overlay.printHook(graphics, mc.level, pos);
			} else if(mc.level.getBlockState(pos).getBlock() instanceof com.hbm.blocks.ILookOverlay overlay) {
				overlay.printHook(graphics, mc.level, pos);
			}
		});
	}

	/** OBJ models are parsed lazily, drop them when resource packs change */
	@SubscribeEvent
	public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener((ResourceManagerReloadListener) manager -> HFRWavefrontObject.clearCaches());
	}
}
