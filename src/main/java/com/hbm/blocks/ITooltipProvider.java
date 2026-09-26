package com.hbm.blocks;

import java.util.List;

import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

/** Blocks with the "Hold LSHIFT" description tooltip, text from {@code tile.<name>.desc} */
public interface ITooltipProvider {

	public default void addStandardInfo(List<Component> list) {
		this.addStandardInfo("tile." + BuiltInRegistries.BLOCK.getKey((Block) this).getPath() + ".desc", list);
	}

	public default void addStandardInfo(String name, List<Component> list) {
		addStandardInfoStatic(name, list);
	}

	public static void addStandardInfoStatic(String name, List<Component> list) {
		if(Screen.hasShiftDown()) {
			for(String s : I18nUtil.resolveKeyArray(name)) list.add(Component.literal(s).withStyle(ChatFormatting.YELLOW));
		} else {
			list.add(Component.literal("Hold <").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
					.append(Component.literal("LSHIFT").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
					.append(Component.literal("> to display more info").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
		}
	}
}
