package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.machine.TileEntityDeuteriumExtractor;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Deuterium extractor, a plain block without GUI, the look overlay shows power and tanks */
public class MachineDeuteriumExtractor extends BlockMachineTile implements ILookOverlay {

	public MachineDeuteriumExtractor(Properties properties) {
		super(properties, ModTileEntities.DEUTERIUM_EXTRACTOR);
	}

	/** The overlay of both the extractor and the tower */
	public static void printExtractor(GuiGraphics graphics, TileEntityDeuteriumExtractor extractor, String title) {
		List<String> text = new ArrayList<>();
		text.add((extractor.power < extractor.getMaxPower() / 20 ? ChatFormatting.RED : ChatFormatting.GREEN) + "Power: " + BobMathUtil.getShortNumber(extractor.power) + "HE");

		for(int i = 0; i < extractor.tanks.length; i++)
			text.add((i < 1 ? (ChatFormatting.GREEN + "-> ") : (ChatFormatting.RED + "<- ")) + ChatFormatting.RESET + extractor.tanks[i].getTankType().getLocalizedName() + ": " + extractor.tanks[i].getFill() + "/" + extractor.tanks[i].getMaxFill() + "mB");

		ILookOverlay.printGeneric(graphics, title, 0xffff00, 0x404000, text);
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		if(!(world.getBlockEntity(pos) instanceof TileEntityDeuteriumExtractor extractor)) return;
		printExtractor(graphics, extractor, I18nUtil.resolveKey(getDescriptionId()));
	}
}
