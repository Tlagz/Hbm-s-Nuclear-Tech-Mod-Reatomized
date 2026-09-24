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
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModTileEntities.WOOD_BURNER.get(), RenderWoodBurner::new);
	}

	@SubscribeEvent
	public static void registerItemRenderers(RegisterClientExtensionsEvent event) {
		NTMItemRenderer.RENDERERS.put(ModBlocks.machine_wood_burner.get().asItem(), RenderWoodBurner.itemRenderer());

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
	}

	/** OBJ models are parsed lazily, drop them when resource packs change */
	@SubscribeEvent
	public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener((ResourceManagerReloadListener) manager -> HFRWavefrontObject.clearCaches());
	}
}
