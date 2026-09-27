package com.hbm.render.tileentity;

import com.hbm.blocks.machine.MachineThresher;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineThresher;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Thresher: engine, folding arm and the spinning reel at the front */
public class RenderThresher implements BlockEntityRenderer<TileEntityMachineThresher> {

	public RenderThresher(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineThresher thresher, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0D, 0.5D);

		switch(thresher.getBlockState().getValue(MachineThresher.FACING)) {
		case SOUTH: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case EAST: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case WEST: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		default: break;
		}

		double angle = thresher.prevAngle + (thresher.angle - thresher.prevAngle) * interp;
		double spin = thresher.lastSpin + (thresher.spin - thresher.lastSpin) * interp;
		double engine = thresher.isOn ? Math.sin(thresher.getLevel().getGameTime() * 2 % (Math.PI * 2) + interp) : 0;

		renderCommon(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.thresher_tex)), light, overlay, 82.5 - angle, spin, engine);

		pose.popPose();
	}

	public static void renderCommon(PoseStack pose, VertexConsumer consumer, int light, int overlay, double angle, double spin, double engine) {

		ResourceManager.thresher.renderPart("Base", pose, consumer, light, overlay);

		pose.pushPose();
		pose.translate(0, engine * 0.01, 0);
		ResourceManager.thresher.renderPart("Engine", pose, consumer, light, overlay);
		pose.popPose();

		pose.translate(0, 0.5, -1);
		pose.mulPose(Axis.XP.rotationDegrees((float) angle));
		pose.translate(0, -0.5, 1);
		ResourceManager.thresher.renderPart("ArmUpper", pose, consumer, light, overlay);

		pose.translate(0, 0.5, -5);
		pose.mulPose(Axis.XP.rotationDegrees((float) (angle * -2)));
		pose.translate(0, -0.5, 5);
		pose.translate(-0.01, 0, 0);
		ResourceManager.thresher.renderPart("ArmLower", pose, consumer, light, overlay);
		pose.translate(0.01, 0, 0);

		pose.translate(0, 0.5, -9);
		pose.mulPose(Axis.XP.rotationDegrees((float) angle));
		pose.translate(0, -0.5, 9);
		pose.translate(0.01, 0, 0);
		ResourceManager.thresher.renderPart("Front", pose, consumer, light, overlay);

		pose.translate(0, 0.5, -11);
		pose.mulPose(Axis.XP.rotationDegrees((float) -spin));
		pose.translate(0, -0.5, 11);
		ResourceManager.thresher.renderPart("Wheel", pose, consumer, light, overlay);
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineThresher tile) {
		return tile.getRenderBoundingBox();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, 4, -8);
				pose.scale(4.5F, 4.5F, 4.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				pose.mulPose(Axis.YN.rotationDegrees(90));
				RenderThresher.renderCommon(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.thresher_tex)), light, overlay, 80D, System.currentTimeMillis() % 3600 * 0.25D, 0);
			}
		};
	}
}
