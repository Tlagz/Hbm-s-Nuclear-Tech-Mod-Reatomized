package com.hbm.items;

import java.util.List;

import com.hbm.util.i18n.I18nUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Item with a description from the lang file ("item.[name].desc", lines separated by $).
 * The original name is kept for the lang key since registry names are lowercased.
 *
 * TODO polaroid P11 alternate descriptions, the "undefined" item's scrambling name
 */
public class ItemCustomLore extends Item {

	private final String descKey;

	public ItemCustomLore(Properties properties, String originalName) {
		super(properties);
		this.descKey = "item." + originalName + ".desc";
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		String loc = I18nUtil.resolveKey(descKey);

		if(!descKey.equals(loc)) {
			for(String s : loc.split("\\$")) {
				list.add(Component.literal(s));
			}
		}
	}
}
