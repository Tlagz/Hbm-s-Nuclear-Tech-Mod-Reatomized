package com.hbm.render.entity;

import java.util.Random;

import com.hbm.entity.item.EntityMovingItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Items on conveyors: flat items lie on the belt, blocks stand on it (the original rendered an item frame item) */
public class RenderMovingItem extends EntityRenderer<EntityMovingItem> {

	private final ItemRenderer itemRenderer;

	public RenderMovingItem(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(EntityMovingItem item, float yaw, float interp, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();

		Random rand = new Random(item.getId());
		pose.translate(0, rand.nextDouble() * 0.0625, 0);

		ItemStack stack = item.getItemStack();
		BakedModel model = itemRenderer.getModel(stack, item.level(), null, item.getId());

		if(!model.isGui3d()) {
			pose.mulPose(Axis.XP.rotationDegrees(90F));
			pose.translate(0.0, -0.1875, 0.0);
		} else {
			pose.translate(0.0, 0.125, 0.0);
		}

		pose.scale(0.5F, 0.5F, 0.5F);
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, item.level(), item.getId());

		pose.popPose();
		super.render(item, yaw, interp, pose, buffers, light);
	}

	@Override
	@SuppressWarnings("deprecation")
	public ResourceLocation getTextureLocation(EntityMovingItem entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
