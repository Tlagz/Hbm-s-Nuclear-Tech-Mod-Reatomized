package com.hbm.render.item;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The one BlockEntityWithoutLevelRenderer for all items with custom (OBJ) rendering, dispatching to the
 * per item ItemRenderBase like the original's IItemRenderer registrations. Items using it need a model with
 * parent builtin/entity.
 */
public class NTMItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final Map<Item, ItemRenderBase> RENDERERS = new HashMap<>();

	private static NTMItemRenderer instance;

	public static NTMItemRenderer get() {
		if(instance == null) instance = new NTMItemRenderer();
		return instance;
	}

	private NTMItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		ItemRenderBase renderer = RENDERERS.get(stack.getItem());
		if(renderer != null) renderer.render(stack, context, pose, buffers, light, overlay);
	}
}
