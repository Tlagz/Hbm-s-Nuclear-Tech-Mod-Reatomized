package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityDeuteriumTower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The tower model, anchored at the core's corner like in the original */
public class RenderDeuteriumTower implements BlockEntityRenderer<TileEntityDeuteriumTower> {

	public RenderDeuteriumTower(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityDeuteriumTower tower, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(180F));

		switch(BlockDummyable.getMeta(tower.getBlockState()) - 10) {
		case 2:
			pose.translate(0F, 0F, -1F);
			break;
		case 3:
			pose.mulPose(Axis.YP.rotationDegrees(180F));
			pose.translate(1F, 0F, 0F);
			break;
		case 4:
			pose.mulPose(Axis.YP.rotationDegrees(90F));
			pose.translate(1F, 0F, -1F);
			break;
		case 5:
			pose.mulPose(Axis.YP.rotationDegrees(270F));
			break;
		}

		ResourceManager.deuterium_tower.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.deuterium_tower_tex)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityDeuteriumTower tile) {
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
				pose.translate(0, -5, 0);
				pose.scale(1.75F, 1.75F, 1.75F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.translate(0.5, 0, -0.5);
				ResourceManager.deuterium_tower.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.deuterium_tower_tex)), light, overlay);
			}
		};
	}
}
