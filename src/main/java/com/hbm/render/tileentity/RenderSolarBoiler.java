package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntitySolarBoiler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/** Solar tower boiler plus a faint additive light beam from every heliostat aimed at it (fancy graphics only) */
public class RenderSolarBoiler implements BlockEntityRenderer<TileEntitySolarBoiler> {

	/** The original's ClientConfig RENDER_HELIOSTAT_BEAM_LIMIT default */
	public static final int BEAM_LIMIT = 250;

	public RenderSolarBoiler(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntitySolarBoiler boiler, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0D, 0.5D);

		pose.pushPose();
		switch(BlockDummyable.getRotation(boiler.getBlockState())) {
		case NORTH: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case WEST: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case SOUTH: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		default: break;
		}
		ResourceManager.solar_boiler.renderPart("Base", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.solar_tex)), light, overlay);
		pose.popPose();

		if(Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST) {

			int beamCount = 0;

			for(BlockPos co : boiler.secondary) {
				beamCount++;
				if(beamCount > BEAM_LIMIT) break;

				int dx = boiler.getBlockPos().getX() - co.getX();
				int dy = boiler.getBlockPos().getY() - co.getY();
				int dz = boiler.getBlockPos().getZ() - co.getZ();

				double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

				float min = 0.005F;
				float max = 0.01F;

				pose.pushPose();
				pose.translate(-dx, -dy, -dz);

				double pitch = Math.toDegrees(-Math.asin((dy + 0.5) / dist)) + 90;
				double yaw = Math.toDegrees(-Math.atan2(dz, dx)) + 180;

				pose.translate(0, 1, 0);
				pose.mulPose(Axis.YP.rotationDegrees((float) yaw));
				pose.mulPose(Axis.ZP.rotationDegrees((float) pitch));
				pose.translate(0, -1, 0);

				VertexConsumer buf = buffers.getBuffer(RenderType.lightning());
				Matrix4f m = pose.last().pose();
				float d = (float) dist;

				quad(buf, m, 0.5F, 0.5F, 0.5F, -0.5F, d, max, min);
				quad(buf, m, -0.5F, 0.5F, -0.5F, -0.5F, d, max, min);
				quadZ(buf, m, 0.5F, d, max, min);
				quadZ(buf, m, -0.5F, d, max, min);

				pose.popPose();
			}
		}

		pose.popPose();
	}

	/** A beam side at constant x, from the mirror (bright) to the boiler (faint) */
	private static void quad(VertexConsumer buf, Matrix4f m, float x0, float z0, float x1, float z1, float dist, float max, float min) {
		buf.addVertex(m, x0, 1.0625F, z0).setColor(1F, 1F, 1F, max);
		buf.addVertex(m, x1, 1.0625F, z1).setColor(1F, 1F, 1F, max);
		buf.addVertex(m, x1, dist, z1).setColor(1F, 1F, 1F, min);
		buf.addVertex(m, x0, dist, z0).setColor(1F, 1F, 1F, min);
	}

	/** A beam side at constant z */
	private static void quadZ(VertexConsumer buf, Matrix4f m, float z, float dist, float max, float min) {
		buf.addVertex(m, 0.5F, 1.0625F, z).setColor(1F, 1F, 1F, max);
		buf.addVertex(m, -0.5F, 1.0625F, z).setColor(1F, 1F, 1F, max);
		buf.addVertex(m, -0.5F, dist, z).setColor(1F, 1F, 1F, min);
		buf.addVertex(m, 0.5F, dist, z).setColor(1F, 1F, 1F, min);
	}

	@Override
	public AABB getRenderBoundingBox(TileEntitySolarBoiler tile) {
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
				pose.translate(0, -2.5, 0);
				pose.scale(3.25F, 3.25F, 3.25F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.solar_boiler.renderPart("Base", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.solar_tex)), light, overlay);
			}
		};
	}
}
