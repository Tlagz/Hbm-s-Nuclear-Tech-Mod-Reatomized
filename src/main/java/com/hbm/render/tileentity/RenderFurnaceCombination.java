package com.hbm.render.tileentity;

import org.joml.Matrix4f;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/** Combination furnace, a fire billboard (the RBMK fire animation) burns on top while it works */
public class RenderFurnaceCombination implements BlockEntityRenderer<TileEntityFurnaceCombination> {

	public static final ResourceLocation texture = RefStrings.loc("textures/particle/rbmk_fire.png");

	public RenderFurnaceCombination(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityFurnaceCombination furnace, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		ResourceManager.combination_oven.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.combination_oven_tex)), light, overlay);

		if(furnace.wasOn) {
			int texIndex = (int) (furnace.getLevel().getGameTime() / 2 % 14);
			float f0 = 1F / 14F;
			float uMin = texIndex % 5 * f0;
			float uMax = uMin + f0;

			pose.pushPose();
			pose.translate(0, 1.75, 0);
			pose.mulPose(Axis.YP.rotationDegrees(-Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));

			float scaleH = 1F;
			float scaleV = 3F;

			Matrix4f matrix = pose.last().pose();
			VertexConsumer fire = buffers.getBuffer(RenderType.eyes(texture));
			vertex(fire, matrix, pose, -scaleH, 0, uMax, 1F);
			vertex(fire, matrix, pose, -scaleH, scaleV, uMax, 0F);
			vertex(fire, matrix, pose, scaleH, scaleV, uMin, 0F);
			vertex(fire, matrix, pose, scaleH, 0, uMin, 1F);

			pose.popPose();
		}

		pose.popPose();
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrix, PoseStack pose, float x, float y, float u, float v) {
		consumer.addVertex(matrix, x, y, 0).setColor(1F, 1F, 1F, 1F).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 1, 0);
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityFurnaceCombination tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -1.5, 0);
				pose.scale(3.25F, 3.25F, 3.25F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.combination_oven.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.combination_oven_tex)), light, overlay);
			}
		};
	}
}
