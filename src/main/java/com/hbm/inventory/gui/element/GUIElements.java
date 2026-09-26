package com.hbm.inventory.gui.element;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

/** Custom drawn GUI elements (gauge needles etc.) */
public class GUIElements {

	public static void drawSmoothGauge(GuiGraphics graphics, int x, int y, double z, double progress, double tipLength, double backLength, double backSide, int color) {
		drawSmoothGauge(graphics, x, y, z, progress, tipLength, backLength, backSide, color, 0x000000);
	}

	/** A gauge needle (triangle with an outline), progress 0-1 covers 270 degrees */
	public static void drawSmoothGauge(GuiGraphics graphics, int x, int y, double z, double progress, double tipLength, double backLength, double backSide, int color, int colorOuter) {

		progress = Mth.clamp(progress, 0, 1);

		float angle = (float) Math.toRadians(-progress * 270 - 45);
		Vector3f tip = new Vector3f(0, (float) tipLength, 0).rotateZ(angle);
		Vector3f left = new Vector3f((float) backSide, (float) -backLength, 0).rotateZ(angle);
		Vector3f right = new Vector3f((float) -backSide, (float) -backLength, 0).rotateZ(angle);

		Matrix4f matrix = graphics.pose().last().pose();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
		float mult = 1.5F;
		int outer = 0xFF000000 | colorOuter;
		int inner = 0xFF000000 | color;
		// gui y points down, the original's tessellator did the same
		buffer.addVertex(matrix, x + tip.x * mult, y + tip.y * mult, (float) z).setColor(outer);
		buffer.addVertex(matrix, x + left.x * mult, y + left.y * mult, (float) z).setColor(outer);
		buffer.addVertex(matrix, x + right.x * mult, y + right.y * mult, (float) z).setColor(outer);
		buffer.addVertex(matrix, x + tip.x, y + tip.y, (float) z).setColor(inner);
		buffer.addVertex(matrix, x + left.x, y + left.y, (float) z).setColor(inner);
		buffer.addVertex(matrix, x + right.x, y + right.y, (float) z).setColor(inner);

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.disableCull();
		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.enableCull();
	}

	/**
	 * A part of a 256x256 texture region revealed clockwise like a 270 degree gauge (progress 0-1), starting at the
	 * bottom left corner. The original's GUIElements.drawSmoothTextureModalCircle.
	 */
	public static void drawSmoothTextureModalCircle(GuiGraphics graphics, net.minecraft.resources.ResourceLocation texture, int xDraw, int yDraw, float zDraw, int xStart, int yStart, int xDelta, int yDelta, double progress) {
		float uv = 1F / 256F;

		progress = Mth.clamp(progress, 0, 1);
		float angle = (float) (-progress * 270.0);
		double theta = Math.toRadians(angle - 135);
		int addons = 0;
		double xTarget = 0;
		double yTarget = 0;

		// how many fixed corners are passed before the moving point
		if(angle >= -180 && angle < -90) {
			addons = 1;
		} else if(angle >= -270 && angle < -180) {
			addons = 2;
		}

		// the moving point on the square's edge
		if(angle >= -90) {
			xTarget = -1;
			yTarget = -Math.tan(theta);
		} else if(angle > -180 && angle < -90 && angle != -135) {
			xTarget = Math.tan(Math.PI / 2 - theta);
			yTarget = 1;
		} else if(angle <= -180) {
			xTarget = 1;
			yTarget = Math.tan(theta);
		} else if(angle == -135) {
			xTarget = 0;
			yTarget = 1;
		}

		double xMid = (double) xDelta / 2;
		double yMid = (double) yDelta / 2;
		xTarget *= xMid;
		yTarget *= yMid;

		// a fan of triangles around the middle: bottom left, [top left], [top right], moving point
		List<double[]> points = new java.util.ArrayList<>();
		points.add(new double[] {0, yDelta});
		if(addons >= 1) points.add(new double[] {0, 0});
		if(addons == 2) points.add(new double[] {xDelta, 0});
		points.add(new double[] {xTarget + xMid, -yTarget + yMid});

		Matrix4f matrix = graphics.pose().last().pose();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
		for(int i = 0; i < points.size() - 1; i++) {
			double[] a = points.get(i);
			double[] b = points.get(i + 1);
			buffer.addVertex(matrix, (float) (xDraw + a[0]), (float) (yDraw + a[1]), zDraw).setUv((float) (xStart + a[0]) * uv, (float) (yStart + a[1]) * uv);
			buffer.addVertex(matrix, (float) (xDraw + xMid), (float) (yDraw + yMid), zDraw).setUv((float) (xStart + xMid) * uv, (float) (yStart + yMid) * uv);
			buffer.addVertex(matrix, (float) (xDraw + b[0]), (float) (yDraw + b[1]), zDraw).setUv((float) (xStart + b[0]) * uv, (float) (yStart + b[1]) * uv);
		}

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.disableCull();
		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.enableCull();
	}

	public static final int STANDARD_COLOR_BACKGROUND = -0xFEFFFF0;
	public static final int STANDARD_COLOR_LINE0 = 0x505000FF;
	public static final int STANDARD_COLOR_LINE1 = (STANDARD_COLOR_LINE0 & 0xFEFEFE) >> 1 | STANDARD_COLOR_LINE0 & -0xFEFEFE;
	public static final int RECIPE_COLOR_LINE0 = 0xFFFF8000;
	public static final int RECIPE_COLOR_LINE1 = 0xFFFFFF00;
	public static final int STANDARD_HEADER_OFFSET = 2;
	public static final int STANDARD_LINE_DIST = 10;

	/** Recipe tooltip: orange/yellow border, the first line (the recipe name) set apart */
	public static void drawHoveringTextRecipe(GuiGraphics graphics, List<String> lines, int x, int y, Font font, int guiWidth, int guiHeight) {
		drawHoveringText(graphics, lines, x, y, font, guiWidth, guiHeight, 6, STANDARD_LINE_DIST, STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_BACKGROUND, RECIPE_COLOR_LINE0, RECIPE_COLOR_LINE1);
	}

	/** The original's tooltip renderer with custom colors, header offset and line distance (colors are ARGB) */
	public static void drawHoveringText(GuiGraphics graphics, List<String> lines, int x, int y, Font font, int guiWidth, int guiHeight, int headerOffset, int lineDist, int colBG0, int colBG1, int colLine0, int colLine1) {

		if(lines.isEmpty()) return;

		int width = 0;
		for(String line : lines) width = Math.max(width, font.width(line));

		int boundX = x + 12;
		int boundY = y - 12;
		int height = 6 + headerOffset;

		if(lines.size() > 1) {
			height += 2 + (lines.size() - 1) * lineDist;
		}

		// if trying to leave bottom or right side, move inwards
		if(boundX + width + 4 > guiWidth) boundX -= 28 + width;
		if(boundY + height + 6 > guiHeight) boundY = guiHeight - height - 6;

		// afterwards, see if the tooltip exits the top or left and then fix that
		if(boundX < 4) boundX = 4;
		if(boundY < 4) boundY = 4;

		graphics.pose().pushPose();
		graphics.pose().translate(0, 0, 400);

		graphics.fillGradient(boundX - 3, boundY - 4, boundX + width + 3, boundY - 3, colBG0, colBG0);
		graphics.fillGradient(boundX - 3, boundY + height + 3, boundX + width + 3, boundY + height + 4, colBG1, colBG1);
		graphics.fillGradient(boundX - 3, boundY - 3, boundX + width + 3, boundY + height + 3, colBG0, colBG1);
		graphics.fillGradient(boundX - 4, boundY - 3, boundX - 3, boundY + height + 3, colBG0, colBG1);
		graphics.fillGradient(boundX + width + 3, boundY - 3, boundX + width + 4, boundY + height + 3, colBG0, colBG1);

		graphics.fillGradient(boundX - 3, boundY - 3 + 1, boundX - 3 + 1, boundY + height + 3 - 1, colLine0, colLine1);
		graphics.fillGradient(boundX + width + 2, boundY - 3 + 1, boundX + width + 3, boundY + height + 3 - 1, colLine0, colLine1);
		graphics.fillGradient(boundX - 3, boundY - 3, boundX + width + 3, boundY - 3 + 1, colLine0, colLine0);
		graphics.fillGradient(boundX - 3, boundY + height + 2, boundX + width + 3, boundY + height + 3, colLine1, colLine1);

		for(int i = 0; i < lines.size(); ++i) {
			graphics.drawString(font, lines.get(i), boundX, boundY, -1, true);

			if(i == 0) boundY += headerOffset;
			boundY += lineDist;
		}

		graphics.pose().popPose();
	}
}
