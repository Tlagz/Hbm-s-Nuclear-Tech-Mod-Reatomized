package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityHeaterHeatex;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Heat exchanger, a static model turned with the block */
public class RenderHeaterHeatex implements BlockEntityRenderer<TileEntityHeaterHeatex> {

	public RenderHeaterHeatex(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityHeaterHeatex tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(tile.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		}

		ResourceManager.heater_heatex.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.heater_heatex_tex)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityHeaterHeatex tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -1, 0);
				pose.scale(3.25F, 3.25F, 3.25F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.heater_heatex.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.heater_heatex_tex)), light, overlay);
			}
		};
	}
}
