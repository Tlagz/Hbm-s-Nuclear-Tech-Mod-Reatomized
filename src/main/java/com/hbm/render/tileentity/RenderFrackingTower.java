package com.hbm.render.tileentity;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachineFrackingTower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/** Fracking tower model and the four pipe stubs of its ports */
public class RenderFrackingTower implements BlockEntityRenderer<TileEntityMachineFrackingTower> {

	public static final ResourceLocation pipe_tex = RefStrings.loc("textures/blocks/pipe_silver.png");

	public RenderFrackingTower(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineFrackingTower tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(180));

		ResourceManager.fracking_tower.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.fracking_tower_tex)), light, overlay);

		pose.translate(0, 0.5, 0);
		VertexConsumer pipe = buffers.getBuffer(RenderType.entityCutoutNoCull(pipe_tex));
		for(String part : new String[] {"pX", "nX", "pZ", "nZ"}) ResourceManager.pipe_neo.renderPart(part, pose, pipe, light, overlay);

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineFrackingTower tile) {
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
				pose.scale(2.5F, 2.5F, 2.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.25F, 0.25F, 0.25F);
				ResourceManager.fracking_tower.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.fracking_tower_tex)), light, overlay);
			}
		};
	}
}
