package com.hbm.inventory.gui.element;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

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
}
