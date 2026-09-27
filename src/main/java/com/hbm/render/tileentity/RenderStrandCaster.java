package com.hbm.render.tileentity;

import org.joml.Matrix4f;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * Strand caster: the molten metal glows in the funnel and the cast plate slides out along the line.
 * TODO the original clipped the plate at the funnel with a GL clip plane
 */
public class RenderStrandCaster implements BlockEntityRenderer<TileEntityMachineStrandCaster> {

	public static final ResourceLocation lava = RefStrings.loc("textures/models/machines/lava_gray.png");

	public RenderStrandCaster(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineStrandCaster caster, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(caster.getBlockState()) - BlockDummyable.offset) {
		case 4: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(180));

		ResourceManager.strand_caster.renderPart("caster", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.strand_caster_tex)), light, overlay);

		if(caster.amount != 0 && caster.type != null && caster.getInstalledMold() != null) {

			float level = (float) ((double) caster.amount / (double) caster.getCapacity() * 0.675D);
			double offset = ((double) caster.amount / (double) caster.getInstalledMold().getCost()) * 0.375D;

			int color = caster.type.moltenColor;
			float r = (color >> 16 & 0xFF) / 255F;
			float g = (color >> 8 & 0xFF) / 255F;
			float b = (color & 0xFF) / 255F;

			pose.pushPose();
			pose.translate(0, 0, Math.max(-offset + 3.4, 0));
			ResourceManager.strand_caster.renderPart("plate", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.white_tex)), light, overlay, r, g, b, 1F);
			pose.popPose();

			// the glowing metal surface in the funnel
			Matrix4f matrix = pose.last().pose();
			VertexConsumer surface = buffers.getBuffer(RenderType.entityCutoutNoCull(lava));
			float y = 2.3F + level;
			vertex(surface, matrix, pose, -0.9F, y, -0.999F, 0, 0, r, g, b);
			vertex(surface, matrix, pose, -0.9F, y, 0.999F, 0, 1, r, g, b);
			vertex(surface, matrix, pose, 0.9F, y, 0.999F, 1, 1, r, g, b);
			vertex(surface, matrix, pose, 0.9F, y, -0.999F, 1, 0, r, g, b);
		}

		pose.popPose();
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrix, PoseStack pose, float x, float y, float z, float u, float v, float r, float g, float b) {
		consumer.addVertex(matrix, x, y, z).setColor(r, g, b, 1F).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 1, 0);
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineStrandCaster tile) {
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
				pose.translate(2, 0, 2);
				pose.scale(2F, 2F, 2F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.strand_caster.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.strand_caster_tex)), light, overlay);
			}
		};
	}
}
