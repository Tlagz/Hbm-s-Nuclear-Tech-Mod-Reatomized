package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Flare stack, leaning when tilted */
public class RenderGasFlare implements BlockEntityRenderer<TileEntityMachineGasFlare> {

	public RenderGasFlare(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineGasFlare flare, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(180));

		if(flare.tilted) {
			pose.translate(0, -0.25, 0);
			pose.mulPose(Axis.ZP.rotationDegrees(10));
			pose.mulPose(Axis.YP.rotationDegrees(5));
		}

		ResourceManager.oilflare.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.oilflare_tex)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineGasFlare tile) {
		return AABB.INFINITE;
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -4, 0);
				pose.scale(2.25F, 2.25F, 2.25F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				ResourceManager.oilflare.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.oilflare_tex)), light, overlay);
			}
		};
	}
}
