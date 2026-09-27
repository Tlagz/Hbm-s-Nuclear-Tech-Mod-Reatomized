package com.hbm.render.tileentity;

import com.hbm.blocks.machine.Charger;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityCharger;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** The base, the slide extending while charging and the two arms swiveling out, plus the orange light */
public class RenderCharger implements BlockEntityRenderer<TileEntityCharger> {

	public RenderCharger(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityCharger charger, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(90));

		switch(charger.getBlockState().getValue(Charger.FACING)) {
		case WEST: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case SOUTH: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case EAST: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		default: break;
		}

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.charger_tex));
		ResourceManager.charger.renderPart("Base", pose, consumer, light, overlay);

		double time = (charger.lastUsingTicks + (charger.usingTicks - charger.lastUsingTicks) * interp) / (double) TileEntityCharger.delay;
		double extend = Math.min(1, time * 2);
		double swivel = Math.max(0, (time - 0.5) * 2);

		pose.pushPose();
		tilt(pose);
		pose.translate(0, -0.25 * extend, 0);

		pose.pushPose();
		pose.translate(0, 0.28D, 0);
		pose.mulPose(Axis.XP.rotationDegrees((float) (30 * swivel)));
		pose.translate(0, -0.28D, 0);
		ResourceManager.charger.renderPart("Left", pose, consumer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(0, 0.28D, 0);
		pose.mulPose(Axis.XP.rotationDegrees((float) (-30 * swivel)));
		pose.translate(0, -0.28D, 0);
		ResourceManager.charger.renderPart("Right", pose, consumer, light, overlay);
		pose.popPose();

		pose.popPose();

		// the light is untextured and fullbright, the slide is fullbright too
		ResourceManager.charger.renderPart("Light", pose, buffers.getBuffer(RenderType.debugQuads()), LightTexture.FULL_BRIGHT, overlay, 1F, 0.75F, 0F, 1F);

		pose.pushPose();
		tilt(pose);
		pose.translate(0, -0.25 * extend, 0);
		// getting the light's buffer ended the textured one's batch, it has to be fetched again
		ResourceManager.charger.renderPart("Slide", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.charger_tex)), LightTexture.FULL_BRIGHT, overlay);
		pose.popPose();

		pose.popPose();
	}

	/** The slide and arms sit on a 10 degree incline */
	private static void tilt(PoseStack pose) {
		pose.translate(-0.34375D, 0.25D, 0);
		pose.mulPose(Axis.ZP.rotationDegrees(10));
		pose.translate(0.34375D, -0.25D, 0);
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -3, 0);
				pose.scale(8F, 8F, 8F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.charger_tex));
				ResourceManager.charger.renderPart("Base", pose, consumer, light, overlay);
				ResourceManager.charger.renderPart("Left", pose, consumer, light, overlay);
				ResourceManager.charger.renderPart("Right", pose, consumer, light, overlay);
				ResourceManager.charger.renderPart("Slide", pose, consumer, light, overlay);
			}
		};
	}
}
