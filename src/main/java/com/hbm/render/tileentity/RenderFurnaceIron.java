package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityFurnaceIron;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Iron furnace, the fire door glows while it smelts */
public class RenderFurnaceIron implements BlockEntityRenderer<TileEntityFurnaceIron> {

	public RenderFurnaceIron(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityFurnaceIron furnace, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(furnace.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		pose.translate(-0.5, 0, -0.5);

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.furnace_iron_tex));
		ResourceManager.furnace_iron.renderPart("Main", pose, buffer, light, overlay);

		if(furnace.wasOn) {
			ResourceManager.furnace_iron.renderPart("On", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.furnace_iron_tex)), LightTexture.FULL_BRIGHT, overlay);
		} else {
			ResourceManager.furnace_iron.renderPart("Off", pose, buffer, light, overlay);
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityFurnaceIron tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -2, 0);
				pose.scale(5F, 5F, 5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.furnace_iron_tex));
				ResourceManager.furnace_iron.renderPart("Main", pose, buffer, light, overlay);
				ResourceManager.furnace_iron.renderPart("Off", pose, buffer, light, overlay);
			}
		};
	}
}
