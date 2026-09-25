package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineBlastFurnace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public class RenderBlastFurnace implements BlockEntityRenderer<TileEntityMachineBlastFurnace> {

	public RenderBlastFurnace(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineBlastFurnace furnace, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(furnace.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		if(furnace.tilted) {
			pose.translate(0, -0.25, 0);
			pose.mulPose(Axis.ZP.rotationDegrees(10));
			pose.mulPose(Axis.YP.rotationDegrees(5));
		}

		// the original rendered without face culling
		ResourceManager.blast_furnace.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.blast_furnace_tex)), light, overlay);

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineBlastFurnace tile) {
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
				pose.translate(0, -4.5, 0);
				pose.scale(4, 4, 4);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				ResourceManager.blast_furnace.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.blast_furnace_tex)), light, overlay);
			}
		};
	}
}
