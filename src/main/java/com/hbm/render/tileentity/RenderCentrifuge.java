package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Centrifuge, a static model (the gas centrifuge shares this renderer in the original, TODO once it's ported) */
public class RenderCentrifuge implements BlockEntityRenderer<TileEntityMachineCentrifuge> {

	public RenderCentrifuge(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineCentrifuge tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(tile.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		ResourceManager.centrifuge.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.centrifuge_tex)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineCentrifuge tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -4, 0);
				pose.scale(3.5F, 3.5F, 3.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.centrifuge.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.centrifuge_tex)), light, overlay);
			}
		};
	}
}
