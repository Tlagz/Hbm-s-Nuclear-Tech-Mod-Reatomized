package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityFurnaceSteel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/** Steel furnace, with a pulsing orange glow in the opening while it smelts */
public class RenderFurnaceSteel implements BlockEntityRenderer<TileEntityFurnaceSteel> {

	public RenderFurnaceSteel(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityFurnaceSteel furnace, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(furnace.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		pose.mulPose(Axis.YP.rotationDegrees(-90));

		ResourceManager.furnace_steel.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.furnace_steel_tex)), light, overlay);

		if(furnace.wasOn) {
			// additive glow planes in front of the opening
			VertexConsumer glow = buffers.getBuffer(RenderType.lightning());
			Matrix4f m = pose.last().pose();
			float col = (float) Math.sin(System.currentTimeMillis() * 0.001);
			int r = (int) ((0.875F + col * 0.125F) * 255), g = (int) ((0.625F + col * 0.375F) * 255), a = 127;

			for(int i = 0; i < 4; i++) {
				float x = 1 + i * 0.0625F;
				glow.addVertex(m, x, 1, -1).setColor(r, g, 0, a);
				glow.addVertex(m, x, 1, 1).setColor(r, g, 0, a);
				glow.addVertex(m, x, 0.5F, 1).setColor(r, g, 0, a);
				glow.addVertex(m, x, 0.5F, -1).setColor(r, g, 0, a);
				// the back side, the original drew them without face culling
				glow.addVertex(m, x, 0.5F, -1).setColor(r, g, 0, a);
				glow.addVertex(m, x, 0.5F, 1).setColor(r, g, 0, a);
				glow.addVertex(m, x, 1, 1).setColor(r, g, 0, a);
				glow.addVertex(m, x, 1, -1).setColor(r, g, 0, a);
			}
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityFurnaceSteel tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -1.5, 0);
				pose.scale(3.5F, 3.5F, 3.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.furnace_steel.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.furnace_steel_tex)), light, overlay);
			}
		};
	}
}
