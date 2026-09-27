package com.hbm.render.tileentity;

import com.hbm.blocks.machine.MachineMicrowave;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMicrowave;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** The microwave, its window and the plate spinning while it's cooking */
public class RenderMicrowave implements BlockEntityRenderer<TileEntityMicrowave> {

	public RenderMicrowave(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMicrowave mic, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, -0.785, 0.5D);

		switch(mic.getBlockState().getValue(MachineMicrowave.FACING)) {
		case WEST: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case SOUTH: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case EAST: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		default: break;
		}

		pose.translate(-0.5D, 0.0D, 0.65D);

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.microwave_tex));
		ResourceManager.microwave.renderPart("mainbody_Cube.001", pose, consumer, light, overlay);
		ResourceManager.microwave.renderPart("window_Cube.002", pose, consumer, light, overlay);

		if(mic.time > 0) {
			double rot = (System.currentTimeMillis() * mic.speed / 10D) % 360;
			pose.translate(0.575D, 0.0D, -0.45D);
			pose.mulPose(Axis.YP.rotationDegrees((float) rot));
			pose.translate(-0.575D, 0.0D, 0.45D);
		}

		ResourceManager.microwave.renderPart("plate_Cylinder", pose, consumer, light, overlay);

		pose.popPose();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -1.5, 0);
				pose.scale(4F, 4F, 4F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.translate(-0.5, -0.785, 0.65);
				ResourceManager.microwave.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.microwave_tex)), light, overlay);
			}
		};
	}
}
