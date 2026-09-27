package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** FEL laser crystal, decides the wavelength of the free electron laser */
public class ItemFELCrystal extends Item {

	public final EnumWavelengths wavelength;

	public ItemFELCrystal(Properties properties, EnumWavelengths wavelength) {
		super(properties.stacksTo(1));
		this.wavelength = wavelength;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		if(stack.is(ModItems.laser_crystal_digamma.get())) {
			list.add(Component.literal("THERADIANCEOFATHOUSANDSUNS").withStyle(ChatFormatting.OBFUSCATED));
		} else {
			list.add(Component.literal(I18nUtil.resolveKey("item." + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this).getPath() + ".desc")));
		}
		list.add(Component.literal(wavelength.textColor + I18nUtil.resolveKey(wavelength.name) + " - " + wavelength.textColor + I18nUtil.resolveKey(wavelength.wavelengthRange)));
	}

	public static enum EnumWavelengths {
		NULL("la creatura", "6 dollar", 0x010101, 0x010101, ChatFormatting.WHITE), //why do you exist?

		IR("wavelengths.name.ir", "wavelengths.waveRange.ir", 0xBB1010, 0xCC4040, ChatFormatting.RED),
		VISIBLE("wavelengths.name.visible", "wavelengths.waveRange.visible", 0, 0, ChatFormatting.GREEN),
		UV("wavelengths.name.uv", "wavelengths.waveRange.uv", 0x0A1FC4, 0x00EFFF, ChatFormatting.AQUA),
		GAMMA("wavelengths.name.gamma", "wavelengths.waveRange.gamma", 0x150560, 0xEF00FF, ChatFormatting.LIGHT_PURPLE),
		DRX("wavelengths.name.drx", "wavelengths.waveRange.drx", 0xFF0000, 0xFF0000, ChatFormatting.DARK_RED);

		public final String name;
		public final String wavelengthRange;
		public final int renderedBeamColor;
		public final int guiColor;
		public final ChatFormatting textColor;

		private EnumWavelengths(String name, String wavelength, int color, int guiColor, ChatFormatting textColor) {
			this.name = name;
			this.wavelengthRange = wavelength;
			this.renderedBeamColor = color;
			this.guiColor = guiColor;
			this.textColor = textColor;
		}
	}
}
