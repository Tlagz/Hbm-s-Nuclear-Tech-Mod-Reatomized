package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityCrucible;
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
import org.joml.Matrix4f;

/** Crucible: the model and a glowing lava surface rising with the amount of molten material */
public class RenderCrucible implements BlockEntityRenderer<TileEntityCrucible> {

	public static final ResourceLocation lava = RefStrings.loc("textures/models/machines/lava.png");

	public RenderCrucible(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityCrucible crucible, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(crucible.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		}

		ResourceManager.crucible_heat.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.crucible_tex)), light, overlay);

		if(!crucible.recipeStack.isEmpty() || !crucible.wasteStack.isEmpty()) {
			int totalCap = TileEntityCrucible.recipeZCapacity + TileEntityCrucible.wasteZCapacity;
			int totalMass = 0;

			for(MaterialStack stack : crucible.recipeStack) totalMass += stack.amount;
			for(MaterialStack stack : crucible.wasteStack) totalMass += stack.amount;

			float level = (float) (0.5 + ((double) totalMass / (double) totalCap) * 0.875D);

			VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(lava));
			Matrix4f m = pose.last().pose();
			int bright = LightTexture.FULL_BRIGHT;
			buffer.addVertex(m, -1, level, -1).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(bright).setNormal(pose.last(), 0, 1, 0);
			buffer.addVertex(m, -1, level, 1).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(bright).setNormal(pose.last(), 0, 1, 0);
			buffer.addVertex(m, 1, level, 1).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(bright).setNormal(pose.last(), 0, 1, 0);
			buffer.addVertex(m, 1, level, -1).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(bright).setNormal(pose.last(), 0, 1, 0);
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityCrucible tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -2, 0);
				pose.scale(4.5F, 4.5F, 4.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.crucible_heat.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.crucible_tex)), light, overlay);
			}
		};
	}
}
