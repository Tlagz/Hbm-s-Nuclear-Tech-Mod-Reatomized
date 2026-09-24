package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityHeatBoiler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public class RenderBoiler implements BlockEntityRenderer<TileEntityHeatBoiler> {

	public RenderBoiler(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityHeatBoiler boiler, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(boiler.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		if(!boiler.hasExploded) {

			// the boiler bulges when the output is almost full
			if(boiler.tanks[1].getFill() > boiler.tanks[1].getMaxFill() * 0.9) {
				double sine = Math.sin(System.currentTimeMillis() / 50D % (Math.PI * 2));
				sine *= 0.01D;
				pose.scale((float) (1 - sine), (float) (1 + sine), (float) (1 - sine));
			}

			ResourceManager.boiler.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.boiler_tex)), light, overlay);
		} else {
			ResourceManager.boiler_burst.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.boiler_tex)), light, overlay);
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityHeatBoiler tile) {
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
				pose.scale(3, 3, 3);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				ResourceManager.boiler.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.boiler_tex)), light, overlay);
			}
		};
	}
}
