package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityHeaterElectric;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public class RenderElectricHeater implements BlockEntityRenderer<TileEntityHeaterElectric> {

	public RenderElectricHeater(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityHeaterElectric heater, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(heater.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		}

		ResourceManager.heater_electric.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.heater_electric_tex)), light, overlay);

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityHeaterElectric tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, 0, 0);
				pose.scale(3F, 3F, 3F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.translate(0.5, 0, 0);
				ResourceManager.heater_electric.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.heater_electric_tex)), light, overlay);
			}
		};
	}
}
