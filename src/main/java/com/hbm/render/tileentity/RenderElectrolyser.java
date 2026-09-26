package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityElectrolyser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Electrolysis machine, a static model */
public class RenderElectrolyser implements BlockEntityRenderer<TileEntityElectrolyser> {

	public RenderElectrolyser(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityElectrolyser tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(tile.getBlockState()) - BlockDummyable.offset) {
		case 4: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		pose.mulPose(Axis.YP.rotationDegrees(180));
		ResourceManager.electrolyser.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.electrolyser_tex)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityElectrolyser tile) {
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
				pose.translate(-1, -1, 0);
				pose.scale(2.5F, 2.5F, 2.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				ResourceManager.electrolyser.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.electrolyser_tex)), light, overlay);
			}
		};
	}
}
