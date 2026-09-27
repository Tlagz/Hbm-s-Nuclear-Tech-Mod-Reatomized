package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachinePUREX;
import com.hbm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Base (and the frame if something is on top), the fan and the sliding pump while working */
public class RenderPUREX implements BlockEntityRenderer<TileEntityMachinePUREX> {

	public RenderPUREX(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachinePUREX purex, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(90));

		switch(BlockDummyable.getMeta(purex.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		float anim = purex.prevAnim + (purex.anim - purex.prevAnim) * interp;

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.purex_tex));
		ResourceManager.purex.renderPart("Base", pose, consumer, light, overlay);
		if(purex.frame) ResourceManager.purex.renderPart("Frame", pose, consumer, light, overlay);

		pose.pushPose();
		pose.translate(1.5, 1.25, 0);
		pose.mulPose(Axis.ZP.rotationDegrees(anim * 45));
		pose.translate(-1.5, -1.25, 0);
		ResourceManager.purex.renderPart("Fan", pose, consumer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(BobMathUtil.sps(anim * 0.25) * 0.5, 0, 0);
		ResourceManager.purex.renderPart("Pump", pose, consumer, light, overlay);
		pose.popPose();

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachinePUREX tile) {
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
				pose.translate(0, -2.5, 0);
				pose.scale(2.5F, 2.5F, 2.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.scale(0.75F, 0.75F, 0.75F);
				VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.purex_tex));
				ResourceManager.purex.renderPart("Base", pose, consumer, light, overlay);
				ResourceManager.purex.renderPart("Frame", pose, consumer, light, overlay);
				ResourceManager.purex.renderPart("Fan", pose, consumer, light, overlay);
				ResourceManager.purex.renderPart("Pump", pose, consumer, light, overlay);
			}
		};
	}
}
