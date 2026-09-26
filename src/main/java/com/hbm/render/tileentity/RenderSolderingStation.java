package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineSolderingStation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;

/** Soldering station, the recipe's output lies on the table */
public class RenderSolderingStation implements BlockEntityRenderer<TileEntityMachineSolderingStation> {

	public RenderSolderingStation(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineSolderingStation solderer, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(solderer.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		pose.translate(-0.5, 0, 0.5);

		ResourceManager.soldering_station.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.soldering_station_tex)), light, overlay);

		if(!solderer.display.isEmpty()) {
			pose.pushPose();
			pose.translate(0.0625D * 2.5D, 1.125D, 0D);
			pose.mulPose(Axis.YP.rotationDegrees(90));
			pose.mulPose(Axis.XP.rotationDegrees(-90));
			pose.scale(0.75F, 0.75F, 0.75F);
			Minecraft.getInstance().getItemRenderer().renderStatic(solderer.display, ItemDisplayContext.FIXED, light, overlay, pose, buffers, solderer.getLevel(), 0);
			pose.popPose();
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineSolderingStation tile) {
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
				pose.translate(0, -1, 0);
				pose.scale(5F, 5F, 5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.soldering_station.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.soldering_station_tex)), light, overlay);
			}
		};
	}
}
