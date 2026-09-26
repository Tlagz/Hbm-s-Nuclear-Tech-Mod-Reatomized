package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityHeaterOilburner;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Fluid burner, a static model */
public class RenderOilburner implements BlockEntityRenderer<TileEntityHeaterOilburner> {

	public RenderOilburner(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityHeaterOilburner tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		ResourceManager.heater_oilburner.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.heater_oilburner_tex)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityHeaterOilburner tile) {
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
				pose.translate(0, -1.5, 0);
				pose.scale(3.25F, 3.25F, 3.25F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.heater_oilburner.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.heater_oilburner_tex)), light, overlay);
			}
		};
	}
}
