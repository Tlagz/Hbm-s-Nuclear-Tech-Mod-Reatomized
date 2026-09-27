package com.hbm.render.util;

import java.util.Random;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Lightning-like beams along a vector, either jittering (RANDOM) or winding (SPIRAL) around it. LINE beams are drawn
 * with the line render type, SOLID beams as additive layered quads (the original's GL_ONE blending).
 */
public class BeamPronter {

	public static Random rand = new Random();

	public static enum EnumWaveType {
		RANDOM, SPIRAL
	}

	public static enum EnumBeamType {
		SOLID, LINE
	}

	/** The original's Vec3.rotateAroundY */
	private static double[] rotateAroundY(double x, double z, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new double[] {x * cos + z * sin, z * cos - x * sin};
	}

	public static void prontBeam(PoseStack pose, MultiBufferSource buffers, Vec3 skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {

		pose.pushPose();

		float sYaw = (float) (Math.atan2(skeleton.x, skeleton.z) * 180F / Math.PI);
		float sqrt = Mth.sqrt((float) (skeleton.x * skeleton.x + skeleton.z * skeleton.z));
		float sPitch = (float) (Math.atan2(skeleton.y, (double) sqrt) * 180F / Math.PI);

		pose.mulPose(Axis.YP.rotationDegrees(180));
		pose.mulPose(Axis.YP.rotationDegrees(sYaw));
		pose.mulPose(Axis.XP.rotationDegrees(sPitch - 90));

		Matrix4f matrix = pose.last().pose();
		VertexConsumer consumer = buffers.getBuffer(beam == EnumBeamType.LINE ? RenderType.lines() : RenderType.lightning());

		rand.setSeed(start);
		double length = skeleton.length();
		double segLength = length / segments;
		double lastX = 0;
		double lastY = 0;
		double lastZ = 0;

		for(int i = 0; i <= segments; i++) {

			double[] spinner = new double[] {size, 0};

			if(wave == EnumWaveType.SPIRAL) {
				spinner = rotateAroundY(spinner[0], spinner[1], Math.PI * start / 180D);
				spinner = rotateAroundY(spinner[0], spinner[1], Math.PI * 45D / 180D * i);
			} else if(wave == EnumWaveType.RANDOM) {
				spinner = rotateAroundY(spinner[0], spinner[1], Math.PI * 2 * rand.nextFloat());
				spinner = rotateAroundY(spinner[0], spinner[1], Math.PI * 2 * rand.nextFloat());
			}

			double pX = spinner[0];
			double pY = segLength * i;
			double pZ = spinner[1];

			if(beam == EnumBeamType.LINE && i > 0) {
				line(consumer, pose, matrix, pX, pY, pZ, lastX, lastY, lastZ, outerColor);
			}

			if(beam == EnumBeamType.SOLID && i > 0) {

				float radius = thickness / layers;

				for(int j = 1; j <= layers; j++) {

					float inter = layers > 1 ? (float) (j - 1) / (float) (layers - 1) : 0F;

					int r1 = (outerColor & 0xFF0000) >> 16;
					int g1 = (outerColor & 0x00FF00) >> 8;
					int b1 = outerColor & 0x0000FF;
					int r2 = (innerColor & 0xFF0000) >> 16;
					int g2 = (innerColor & 0x00FF00) >> 8;
					int b2 = innerColor & 0x0000FF;

					int r = (int) (r1 + (r2 - r1) * inter);
					int g = (int) (g1 + (g2 - g1) * inter);
					int b = (int) (b1 + (b2 - b1) * inter);

					float d = radius * j;
					quad(consumer, matrix, r, g, b, lastX + d, lastY, lastZ + d, lastX + d, lastY, lastZ - d, pX + d, pY, pZ - d, pX + d, pY, pZ + d);
					quad(consumer, matrix, r, g, b, lastX - d, lastY, lastZ + d, lastX - d, lastY, lastZ - d, pX - d, pY, pZ - d, pX - d, pY, pZ + d);
					quad(consumer, matrix, r, g, b, lastX + d, lastY, lastZ + d, lastX - d, lastY, lastZ + d, pX - d, pY, pZ + d, pX + d, pY, pZ + d);
					quad(consumer, matrix, r, g, b, lastX + d, lastY, lastZ - d, lastX - d, lastY, lastZ - d, pX - d, pY, pZ - d, pX + d, pY, pZ - d);
				}
			}

			lastX = pX;
			lastY = pY;
			lastZ = pZ;
		}

		if(beam == EnumBeamType.LINE) {
			line(consumer, pose, matrix, 0, 0, 0, 0, length, 0, innerColor);
		}

		pose.popPose();
	}

	private static void line(VertexConsumer consumer, PoseStack pose, Matrix4f matrix, double x0, double y0, double z0, double x1, double y1, double z1, int color) {
		float nx = (float) (x1 - x0);
		float ny = (float) (y1 - y0);
		float nz = (float) (z1 - z0);
		float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
		if(len > 0) { nx /= len; ny /= len; nz /= len; }
		int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
		consumer.addVertex(matrix, (float) x0, (float) y0, (float) z0).setColor(r, g, b, 255).setNormal(pose.last(), nx, ny, nz);
		consumer.addVertex(matrix, (float) x1, (float) y1, (float) z1).setColor(r, g, b, 255).setNormal(pose.last(), nx, ny, nz);
	}

	/** One quad, both windings since the original drew them without face culling */
	private static void quad(VertexConsumer consumer, Matrix4f m, int r, int g, int b, double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
		consumer.addVertex(m, (float) x0, (float) y0, (float) z0).setColor(r, g, b, 255);
		consumer.addVertex(m, (float) x1, (float) y1, (float) z1).setColor(r, g, b, 255);
		consumer.addVertex(m, (float) x2, (float) y2, (float) z2).setColor(r, g, b, 255);
		consumer.addVertex(m, (float) x3, (float) y3, (float) z3).setColor(r, g, b, 255);
		consumer.addVertex(m, (float) x3, (float) y3, (float) z3).setColor(r, g, b, 255);
		consumer.addVertex(m, (float) x2, (float) y2, (float) z2).setColor(r, g, b, 255);
		consumer.addVertex(m, (float) x1, (float) y1, (float) z1).setColor(r, g, b, 255);
		consumer.addVertex(m, (float) x0, (float) y0, (float) z0).setColor(r, g, b, 255);
	}
}
