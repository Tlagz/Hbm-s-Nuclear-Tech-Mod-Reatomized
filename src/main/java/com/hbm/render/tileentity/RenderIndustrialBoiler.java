package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityHeatBoilerIndustrial;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Industrial boiler, a static model */
public class RenderIndustrialBoiler implements BlockEntityRenderer<TileEntityHeatBoilerIndustrial> {

	public RenderIndustrialBoiler(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityHeatBoilerIndustrial tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		ResourceManager.boiler_industrial.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.boiler_industrial_tex)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityHeatBoilerIndustrial tile) {
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
				pose.translate(0, -3, 0);
				pose.scale(2.5F, 2.5F, 2.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				ResourceManager.boiler_industrial.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.boiler_industrial_tex)), light, overlay);
			}
		};
	}
}
