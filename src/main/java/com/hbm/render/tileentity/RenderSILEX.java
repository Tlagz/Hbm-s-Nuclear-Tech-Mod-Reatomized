package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntitySILEX;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public class RenderSILEX implements BlockEntityRenderer<TileEntitySILEX> {

	public RenderSILEX(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntitySILEX silex, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0, 0.5D);

		switch(BlockDummyable.getMeta(silex.getBlockState()) - BlockDummyable.offset) {
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		}

		ResourceManager.silex.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.silex_tex)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntitySILEX tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -2, 0);
				pose.scale(4F, 4F, 4F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.silex.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.silex_tex)), light, overlay);
			}
		};
	}
}
