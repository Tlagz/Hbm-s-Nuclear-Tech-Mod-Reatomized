package com.hbm.main;

import com.hbm.hazard.HazardSystem;
import com.hbm.lib.RefStrings;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Client-only game bus events */
@EventBusSubscriber(modid = RefStrings.MODID, value = Dist.CLIENT)
public class ModEventHandlerClient {

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		HazardSystem.addFullTooltip(event.getItemStack(), event.getEntity(), event.getToolTip());
	}
}
