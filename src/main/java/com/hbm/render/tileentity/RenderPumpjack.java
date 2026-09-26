package com.hbm.render.tileentity;

import org.joml.Matrix4f;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachinePumpjack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Pumpjack: the rotor turns, the head nods, the carriage and the rod move up and down, cables drawn as quads */
public class RenderPumpjack implements BlockEntityRenderer<TileEntityMachinePumpjack> {

	public RenderPumpjack(BlockEntityRendererProvider.Context context) { }

	/** 1.7.10's Vec3.rotateAroundX */
	private static Vec3 rotX(Vec3 v, double angle) {
		double c = Math.cos(angle), s = Math.sin(angle);
		return new Vec3(v.x, v.y * c + v.z * s, v.z * c - v.y * s);
	}

	private static void vertex(VertexConsumer c, Matrix4f m, float r, float g, float b, int light, double x, double y, double z) {
		c.addVertex(m, (float) x, (float) y, (float) z).setColor(r, g, b, 1F).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
	}

	@Override
	public void render(TileEntityMachinePumpjack pj, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(pj.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		float rotation = pj.prevRot + (pj.rot - pj.prevRot) * interp;
		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.pumpjack_tex));

		ResourceManager.pumpjack.renderPart("Base", pose, buffer, light, overlay);

		pose.pushPose();
		pose.translate(0, 1.5, -5.5);
		pose.mulPose(Axis.XP.rotationDegrees(rotation - 90));
		pose.translate(0, -1.5, 5.5);
		ResourceManager.pumpjack.renderPart("Rotor", pose, buffer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(0, 3.5, -3.5);
		pose.mulPose(Axis.XP.rotationDegrees((float) (Math.toDegrees(Math.sin(Math.toRadians(rotation))) * 0.25)));
		pose.translate(0, -3.5, 3.5);
		ResourceManager.pumpjack.renderPart("Head", pose, buffer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(0, -Math.sin(Math.toRadians(rotation)), 0);
		ResourceManager.pumpjack.renderPart("Carriage", pose, buffer, light, overlay);
		pose.popPose();

		// the connecting rods, the cable over the head and the polished rod
		Vec3 backPos = rotX(new Vec3(0, 0, -2), -(float) Math.sin(Math.toRadians(rotation)) * 0.25F);
		Vec3 rot = rotX(new Vec3(0, 0.5, 0), -(float) Math.toRadians(rotation - 90));

		VertexConsumer c = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.white_tex));
		Matrix4f m = pose.last().pose();

		for(int i = -1; i <= 1; i += 2) {
			vertex(c, m, 0.5F, 0.5F, 0.5F, light, 0.53125 * i, 1.5 + rot.y, -5.5 + rot.z - 0.0625D);
			vertex(c, m, 0.5F, 0.5F, 0.5F, light, 0.53125 * i, 1.5 + rot.y, -5.5 + rot.z + 0.0625D);
			vertex(c, m, 0.5F, 0.5F, 0.5F, light, 0.53125 * i, 3.5 + backPos.y, -3.5 + backPos.z + 0.0625D);
			vertex(c, m, 0.5F, 0.5F, 0.5F, light, 0.53125 * i, 3.5 + backPos.y, -3.5 + backPos.z - 0.0625D);
		}

		float g = 0.2F;
		double pd = 0.03125D;
		double width = 0.25D;
		double height = -Math.sin(Math.toRadians(rotation));

		for(int i = -1; i <= 1; i += 2) {

			float pRot = -(float) (Math.sin(Math.toRadians(rotation)) * 0.25);
			Vec3 frontPos = rotX(new Vec3(0, 0, 1), pRot);
			double dist = 0.03125D;
			double cutlet = 360D / 32D;
			Vec3 frontRad = rotX(rotX(new Vec3(0, 0, 2.5 + dist), pRot), -(float) Math.toRadians(cutlet * -3));

			for(int j = 0; j < 4; j++) {
				double sumY = frontPos.y + frontRad.y;
				double sumZ = frontPos.z + frontRad.z;
				if(frontRad.y < 0) sumZ = 3.5 + dist * 0.5;
				vertex(c, m, g, g, g, light, (width - pd) * i, 3.5 + sumY, -3.5 + sumZ);
				vertex(c, m, g, g, g, light, (width + pd) * i, 3.5 + sumY, -3.5 + sumZ);

				frontRad = rotX(frontRad, -(float) Math.toRadians(cutlet));

				sumY = frontPos.y + frontRad.y;
				sumZ = frontPos.z + frontRad.z;
				if(frontRad.y < 0) sumZ = 3.5 + dist * 0.5;
				vertex(c, m, g, g, g, light, (width + pd) * i, 3.5 + sumY, -3.5 + sumZ);
				vertex(c, m, g, g, g, light, (width - pd) * i, 3.5 + sumY, -3.5 + sumZ);
			}

			double sumY = frontPos.y + frontRad.y;
			double sumZ = frontPos.z + frontRad.z;
			if(frontRad.y < 0) sumZ = 3.5 + dist * 0.5;
			vertex(c, m, g, g, g, light, (width + pd) * i, 3.5 + sumY, -3.5 + sumZ);
			vertex(c, m, g, g, g, light, (width - pd) * i, 3.5 + sumY, -3.5 + sumZ);
			vertex(c, m, g, g, g, light, (width - pd) * i, 2 + height, 0);
			vertex(c, m, g, g, g, light, (width + pd) * i, 2 + height, 0);
		}

		double p = 0.03125D;
		vertex(c, m, g, g, g, light, p, height + 1.5, p);
		vertex(c, m, g, g, g, light, -p, height + 1.5, -p);
		vertex(c, m, g, g, g, light, -p, 0.75, -p);
		vertex(c, m, g, g, g, light, p, 0.75, p);
		vertex(c, m, g, g, g, light, -p, height + 1.5, p);
		vertex(c, m, g, g, g, light, p, height + 1.5, -p);
		vertex(c, m, g, g, g, light, p, 0.75, -p);
		vertex(c, m, g, g, g, light, -p, 0.75, p);

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachinePumpjack tile) {
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
				pose.translate(0, -2, 0);
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.scale(4F, 4F, 4F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				pose.translate(0, 0, 3);
				ResourceManager.pumpjack.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.pumpjack_tex)), light, overlay);
			}
		};
	}
}
