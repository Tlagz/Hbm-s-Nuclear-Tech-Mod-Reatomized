package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityCondenserPowered;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** High-power condenser with its two fans spinning while it condenses */
public class RenderCondenser implements BlockEntityRenderer<TileEntityCondenserPowered> {

	public RenderCondenser(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityCondenserPowered condenser, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(condenser.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.condenser_tex));
		ResourceManager.condenser.renderPart("Condenser", pose, buffer, light, overlay);

		float rot = condenser.lastSpin + (condenser.spin - condenser.lastSpin) * interp;

		pose.pushPose();
		pose.translate(0, 1.5, 0);
		pose.mulPose(Axis.XP.rotationDegrees(rot));
		pose.translate(0, -1.5, 0);
		ResourceManager.condenser.renderPart("Fan1", pose, buffer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(0, 1.5, 0);
		pose.mulPose(Axis.XN.rotationDegrees(rot));
		pose.translate(0, -1.5, 0);
		ResourceManager.condenser.renderPart("Fan2", pose, buffer, light, overlay);
		pose.popPose();

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityCondenserPowered tile) {
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
				pose.translate(-1, -1, 0);
				pose.scale(2.75F, 2.75F, 2.75F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.75F, 0.75F, 0.75F);
				pose.translate(0.5, 0, 0);
				ResourceManager.condenser.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.condenser_tex)), light, overlay);
			}
		};
	}
}
