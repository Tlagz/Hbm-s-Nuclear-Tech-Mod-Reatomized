package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachinePress;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;

/** Press body, the head moving down with the press progress and the item being pressed lying on the bed */
public class RenderPress implements BlockEntityRenderer<TileEntityMachinePress> {

	public RenderPress(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachinePress press, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {

		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(180));
		ResourceManager.press_body.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.press_body_tex)), light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.scale(0.99F, 1, 0.99F);
		double p = (press.lastPress + (press.renderPress - press.lastPress) * interp) / (double) TileEntityMachinePress.maxPress;
		pose.translate(0, Mth.clamp(1D - p, 0D, 1D) * 0.875D, 0);
		ResourceManager.press_head.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.press_head_tex)), light, overlay);
		pose.popPose();

		if(!press.syncStack.isEmpty()) {
			pose.pushPose();
			// flat on the press bed, one block up
			pose.translate(0.5, 1 + 0.0625 * 0.35, 0.5);
			pose.mulPose(Axis.XP.rotationDegrees(90));
			pose.scale(0.5F, 0.5F, 0.5F);
			Minecraft.getInstance().getItemRenderer().renderStatic(press.syncStack.copyWithCount(1), ItemDisplayContext.FIXED, light, overlay, pose, buffers, press.getLevel(), 0);
			pose.popPose();
		}
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachinePress tile) {
		return tile.getRenderBoundingBox();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	/** The original had no item renderer for the press, it used the block icon; this shows the model instead */
	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -3.5, 0);
				pose.scale(4.5F, 4.5F, 4.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				ResourceManager.press_body.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.press_body_tex)), light, overlay);
				ResourceManager.press_head.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.press_head_tex)), light, overlay);
			}
		};
	}
}
