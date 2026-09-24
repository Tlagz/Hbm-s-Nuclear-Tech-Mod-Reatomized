package com.hbm.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Item renderer for machines, same structure as the original: renderInventory/renderNonInv set up the
 * transform (in the original's units, pixels for the inventory), renderCommon draws the model.
 * The GL transforms of the original map 1:1 to PoseStack calls.
 */
public abstract class ItemRenderBase {

	public void render(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();

		if(context == ItemDisplayContext.GUI) {
			// 1.7.10 inventory space: pixels, origin at the slot's top left, y pointing down
			pose.translate(0, 1, 0);
			pose.scale(1F / 16F, -1F / 16F, 1F / 16F);
			pose.translate(8, 10, 0);
			pose.mulPose(Axis.XP.rotationDegrees(-30));
			pose.mulPose(Axis.YP.rotationDegrees(45));
			pose.scale(-1, -1, -1);
			renderInventory(pose);
		} else {
			pose.translate(0.5, 0.25, 0.5);

			if(context == ItemDisplayContext.GROUND)
				pose.scale(1.5F, 1.5F, 1.5F);

			pose.scale(0.25F, 0.25F, 0.25F);

			if(context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND && context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
				pose.mulPose(Axis.YP.rotationDegrees(90));
			renderNonInv(pose);
		}

		renderCommon(pose, buffers, light, overlay);
		renderCommonWithStack(stack, pose, buffers, light, overlay);

		pose.popPose();
	}

	public void renderNonInv(PoseStack pose) { }
	public void renderInventory(PoseStack pose) { }
	public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) { }
	public void renderCommonWithStack(ItemStack item, PoseStack pose, MultiBufferSource buffers, int light, int overlay) { }
}
