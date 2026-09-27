package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineIntake;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The intake and its spinning fan */
public class RenderIntake implements BlockEntityRenderer<TileEntityMachineIntake> {

	public RenderIntake(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineIntake intake, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0, 0.5D);

		switch(BlockDummyable.getMeta(intake.getBlockState()) - 10) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		pose.translate(-0.5, 0, 0.5);

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.intake_tex));
		ResourceManager.intake.renderPart("Base", pose, consumer, light, overlay);

		float rot = intake.prevFan + (intake.fan - intake.prevFan) * interp;
		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(-rot));
		ResourceManager.intake.renderPart("Fan", pose, consumer, light, overlay);
		pose.popPose();

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineIntake tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.scale(5F, 5F, 5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.intake.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.intake_tex)), light, overlay);
			}
		};
	}
}
