package com.hbm.blocks;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Blocks (or held items) that print information next to the crosshair while being looked at, e.g. a pipe's
 * fluid or a boiler's tanks. Drawn by the "look_overlay" GUI layer (ModEventHandlerClientMod), client only.
 */
public interface ILookOverlay {

	public void printHook(GuiGraphics graphics, Level world, BlockPos pos);

	/** Title plus lines, a line can start with "&[color&]" to set its color (the original's convention) */
	public static void printGeneric(GuiGraphics graphics, String title, int titleCol, int bgCol, List<String> text) {

		Minecraft mc = Minecraft.getInstance();

		int pX = graphics.guiWidth() / 2 + 8;
		int pZ = graphics.guiHeight() / 2;

		graphics.drawString(mc.font, title, pX + 1, pZ - 9, bgCol, false);
		graphics.drawString(mc.font, title, pX, pZ - 10, titleCol, false);

		try {
			for(String line : text) {

				int color = 0xFFFFFF;
				if(line.startsWith("&[")) {
					int end = line.lastIndexOf("&]");
					color = Integer.parseInt(line.substring(2, end));
					line = line.substring(end + 2);
				}

				graphics.drawString(mc.font, line, pX, pZ, color, true);
				pZ += 10;
			}
		} catch(Exception ex) {
			graphics.drawString(mc.font, ex.getClass().getSimpleName(), pX, pZ + 10, 0xff0000, true);
		}
	}
}
