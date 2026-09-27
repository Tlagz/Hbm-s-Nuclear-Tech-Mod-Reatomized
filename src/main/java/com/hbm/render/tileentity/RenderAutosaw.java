package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineAutosaw;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Automatic buzz saw: turning base, three-part folding arm and the spinning blade */
public class RenderAutosaw implements BlockEntityRenderer<TileEntityMachineAutosaw> {

	public RenderAutosaw(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineAutosaw saw, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0D, 0.5D);

		double turn = saw.prevRotationYaw + (saw.rotationYaw - saw.prevRotationYaw) * interp;
		double angle = 80 - (saw.prevRotationPitch + (saw.rotationPitch - saw.prevRotationPitch) * interp);
		float spin = saw.lastSpin + (saw.spin - saw.lastSpin) * interp;
		double engine = saw.isOn ? Math.sin(saw.getLevel().getGameTime() * 2 % (Math.PI * 2) + interp) : 0;
		renderCommon(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.autosaw_tex)), light, overlay, turn, angle, spin, engine);

		pose.popPose();
	}

	public static void renderCommon(PoseStack pose, VertexConsumer consumer, int light, int overlay, double turn, double angle, double spin, double engine) {

		ResourceManager.autosaw.renderPart("Base", pose, consumer, light, overlay);

		pose.mulPose(Axis.YN.rotationDegrees((float) turn));
		ResourceManager.autosaw.renderPart("Main", pose, consumer, light, overlay);
		pose.pushPose();
		pose.translate(0, engine * 0.01, 0);
		ResourceManager.autosaw.renderPart("Engine", pose, consumer, light, overlay);
		pose.popPose();

		pose.translate(0, 1.75, 0);
		pose.mulPose(Axis.XP.rotationDegrees((float) angle));
		pose.translate(0, -1.75, 0);
		ResourceManager.autosaw.renderPart("ArmUpper", pose, consumer, light, overlay);

		pose.translate(0, 1.75, -4);
		pose.mulPose(Axis.XP.rotationDegrees((float) (angle * -2)));
		pose.translate(0, -1.75, 4);
		pose.translate(-0.01, 0, 0);
		ResourceManager.autosaw.renderPart("ArmLower", pose, consumer, light, overlay);
		pose.translate(0.01, 0, 0);

		pose.translate(0, 1.75, -8);
		pose.mulPose(Axis.XP.rotationDegrees((float) angle));
		pose.translate(0, -1.75, 8);
		ResourceManager.autosaw.renderPart("ArmTip", pose, consumer, light, overlay);

		pose.translate(0, 1.75, -10);
		pose.mulPose(Axis.YN.rotationDegrees((float) spin));
		pose.translate(0, -1.75, 10);
		ResourceManager.autosaw.renderPart("Sawblade", pose, consumer, light, overlay);
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineAutosaw tile) {
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
				pose.translate(0, -3.5, -3);
				pose.scale(5, 5, 5);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				pose.mulPose(Axis.YP.rotationDegrees(-90));
				RenderAutosaw.renderCommon(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.autosaw_tex)), light, overlay, 0D, 80D, System.currentTimeMillis() % 3600 * 0.1D, 0);
			}
		};
	}
}
