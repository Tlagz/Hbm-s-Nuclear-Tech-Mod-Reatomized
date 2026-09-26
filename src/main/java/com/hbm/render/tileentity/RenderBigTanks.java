package com.hbm.render.tileentity;

import org.joml.Matrix4f;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.storage.TileEntityMachineBAT9000;
import com.hbm.tileentity.machine.storage.TileEntityMachineOrbus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;

/**
 * The original's RenderBAT9000 (model and the fill gauges on its four sides) and RenderOrbus (model and the floating
 * sphere of the stored gas).
 *
 * TODO the NFPA diamonds (DiamondPronter), the Orbus beams (BeamPronter)
 */
public class RenderBigTanks {

	/** Flat colored quad through the white texture, full bright like the untextured GL quads of the original */
	private static void quad(VertexConsumer c, Matrix4f m, float r, float g, float b, float x0, float y0, float z0, float x1, float y1, float z1) {
		int light = LightTexture.FULL_BRIGHT;
		c.addVertex(m, x0, y0, z0).setColor(r, g, b, 1F).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
		c.addVertex(m, x0, y1, z0).setColor(r, g, b, 1F).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
		c.addVertex(m, x1, y1, z1).setColor(r, g, b, 1F).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
		c.addVertex(m, x1, y0, z1).setColor(r, g, b, 1F).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
	}

	public static class BAT9000 implements BlockEntityRenderer<TileEntityMachineBAT9000> {

		public BAT9000(BlockEntityRendererProvider.Context context) { }

		@Override
		public void render(TileEntityMachineBAT9000 bat, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
			pose.pushPose();
			pose.translate(0.5, 0, 0.5);

			ResourceManager.bat9000.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.bat9000_tex)), light, overlay);

			if(bat.tank.getTankType() != Fluids.NONE && bat.tank.getMaxFill() > 0) {
				int color = bat.tank.getTankType().getColor();
				float r = ((color & 0xff0000) >> 16) / 255F, g = ((color & 0x00ff00) >> 8) / 255F, b = (color & 0x0000ff) / 255F;
				float height = bat.tank.getFill() * 1.5F / bat.tank.getMaxFill();
				float off = 2.2F;
				VertexConsumer c = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.white_tex));
				Matrix4f m = pose.last().pose();
				quad(c, m, r, g, b, -off, 1.5F, -0.5F, -off, 1.5F + height, 0.5F);
				quad(c, m, r, g, b, off, 1.5F, -0.5F, off, 1.5F + height, 0.5F);
				quad(c, m, r, g, b, -0.5F, 1.5F, -off, 0.5F, 1.5F + height, -off);
				quad(c, m, r, g, b, -0.5F, 1.5F, off, 0.5F, 1.5F + height, off);
			}

			pose.popPose();
		}

		@Override
		public AABB getRenderBoundingBox(TileEntityMachineBAT9000 tile) {
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
					pose.translate(0, -3, 0);
					pose.scale(2F, 2F, 2F);
				}

				@Override
				public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
					ResourceManager.bat9000.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.bat9000_tex)), light, overlay);
				}
			};
		}
	}

	public static class Orbus implements BlockEntityRenderer<TileEntityMachineOrbus> {

		public Orbus(BlockEntityRendererProvider.Context context) { }

		@Override
		public void render(TileEntityMachineOrbus orbus, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
			pose.pushPose();

			// the model's center is the corner shared by the 2x2 core column
			switch(BlockDummyable.getMeta(orbus.getBlockState()) - BlockDummyable.offset) {
			case 2: pose.translate(1F, 0F, 1F); break;
			case 4: pose.translate(1F, 0F, 0F); break;
			case 3: pose.translate(0F, 0F, 0F); break;
			case 5: pose.translate(0F, 0F, 1F); break;
			}

			float scale = orbus.tank.getMaxFill() > 0 ? (float) orbus.tank.getFill() / (float) orbus.tank.getMaxFill() : 0F;

			if(orbus.tank.getFill() > 0) {
				int c = orbus.tank.getTankType().getColor();
				pose.pushPose();
				double time = orbus.getLevel().getGameTime() + interp;
				pose.translate(0, 2.5D + Math.sin((time * 0.1D) % (Math.PI * 2D)) * 0.125 * scale, 0);
				pose.scale(scale, scale, scale);
				ResourceManager.sphere_uv.renderPart("Sphere", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.white_tex)), LightTexture.FULL_BRIGHT, overlay,
						((c & 0xff0000) >> 16) / 255F, ((c & 0x00ff00) >> 8) / 255F, (c & 0x0000ff) / 255F, 1F);
				pose.popPose();
			}

			ResourceManager.orbus.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.orbus_tex)), light, overlay);

			pose.popPose();
		}

		@Override
		public AABB getRenderBoundingBox(TileEntityMachineOrbus tile) {
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
					pose.translate(0, -3, 0);
					pose.scale(2F, 2F, 2F);
				}

				@Override
				public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
					ResourceManager.orbus.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.orbus_tex)), light, overlay);
				}
			};
		}
	}
}
