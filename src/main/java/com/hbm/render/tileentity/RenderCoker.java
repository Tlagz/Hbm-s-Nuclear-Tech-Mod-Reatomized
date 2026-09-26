package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachineCoker;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Coker unit, a static model */
public class RenderCoker implements BlockEntityRenderer<TileEntityMachineCoker> {

	public RenderCoker(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineCoker tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		ResourceManager.coker.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.coker_tex)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineCoker tile) {
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
				pose.scale(2.75F, 2.75F, 2.75F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.25F, 0.25F, 0.25F);
				ResourceManager.coker.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.coker_tex)), light, overlay);
			}
		};
	}
}
