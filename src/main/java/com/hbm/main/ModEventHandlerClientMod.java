package com.hbm.main;

import com.hbm.inventory.ModMenus;
import com.hbm.inventory.gui.GUIMachineElectricFurnace;
import com.hbm.lib.RefStrings;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only mod bus events (screens, renderers), counterpart of the original's ClientProxy registration */
@EventBusSubscriber(modid = RefStrings.MODID, value = Dist.CLIENT)
public class ModEventHandlerClientMod {

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(ModMenus.ELECTRIC_FURNACE.get(), GUIMachineElectricFurnace::new);
	}
}
