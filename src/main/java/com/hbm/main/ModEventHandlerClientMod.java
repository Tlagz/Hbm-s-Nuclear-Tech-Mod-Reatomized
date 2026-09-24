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
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModTileEntities.WOOD_BURNER.get(), RenderWoodBurner::new);
		event.registerBlockEntityRenderer(ModTileEntities.DIESEL.get(), com.hbm.render.tileentity.RenderDieselGen::new);
		event.registerBlockEntityRenderer(ModTileEntities.OIL_WELL.get(), com.hbm.render.tileentity.RenderDerrick::new);
	}

	@SubscribeEvent
	public static void registerItemRenderers(RegisterClientExtensionsEvent event) {
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_wood_burner.get().asItem(), RenderWoodBurner.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_diesel.get().asItem(), com.hbm.render.tileentity.RenderDieselGen.itemRenderer());
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_well.get().asItem(), com.hbm.render.tileentity.RenderDerrick.itemRenderer());

		IClientItemExtensions extension = new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return NTMItemRenderer.get();
			}
		};
		for(var block : ModBlocks.TILE_RENDERED.keySet()) event.registerItem(extension, block.get().asItem());
	}

	/** Tinted layers of fluid containers, the original's getColorFromItemStack(stack, pass) */
	@SubscribeEvent
	public static void registerItemColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
		event.register((stack, tint) -> 0xFF000000 | com.hbm.items.machine.ItemCanister.getColor(stack, tint), com.hbm.items.ModItems.canister_full.get());
		event.register((stack, tint) -> 0xFF000000 | com.hbm.items.machine.ItemGasTank.getColor(stack, tint), com.hbm.items.ModItems.gas_full.get());
		event.register((stack, tint) -> 0xFF000000 | com.hbm.items.machine.ItemFluidTank.getColor(stack, tint),
				com.hbm.items.ModItems.fluid_tank_full.get(), com.hbm.items.ModItems.fluid_tank_lead_full.get(), com.hbm.items.ModItems.fluid_barrel_full.get());
		event.register((stack, tint) -> 0xFF000000 | com.hbm.items.machine.ItemFluidIDMulti.getColor(stack, tint), com.hbm.items.ModItems.fluid_identifier_multi.get());
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
		});
	}

	/** OBJ models are parsed lazily, drop them when resource packs change */
	@SubscribeEvent
	public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener((ResourceManagerReloadListener) manager -> HFRWavefrontObject.clearCaches());
	}
}
