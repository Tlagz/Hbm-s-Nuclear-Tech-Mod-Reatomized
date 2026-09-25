package com.hbm.render.item;

import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.main.ResourceManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;

/** Battery packs: the battery socket's battery or capacitor model with the pack's texture */
public class ItemRenderBatteryPack extends ItemRenderBase {

	@Override
	public void renderInventory(PoseStack pose) {
		pose.translate(0, -3, 0);
		pose.scale(5, 5, 5);
	}

	@Override
	public void renderCommonWithStack(ItemStack item, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		if(!(item.getItem() instanceof ItemBatteryPack battery)) return;
		ResourceManager.battery_socket.renderPart(battery.pack.isCapacitor() ? "Capacitor" : "Battery", pose, buffers.getBuffer(RenderType.entityCutout(battery.pack.texture)), light, overlay);
	}
}
