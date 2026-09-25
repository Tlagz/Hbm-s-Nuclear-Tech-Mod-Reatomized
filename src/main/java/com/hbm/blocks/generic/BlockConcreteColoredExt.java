package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Colored concrete, no mob spawns like the other concrete; the machine stripe variant has the plain machine texture on top */
public class BlockConcreteColoredExt extends BlockEnumMulti {

	public BlockConcreteColoredExt(Properties properties, String descriptionId, EnumConcreteType type) {
		super(properties.isValidSpawn((state, level, pos, entity) -> false), descriptionId);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal(I18nUtil.resolveKey("tile.nospawn")).withStyle(ChatFormatting.RED));
	}

	public enum EnumConcreteType {
		MACHINE,
		MACHINE_STRIPE,
		INDIGO,
		PURPLE,
		PINK,
		HAZARD,
		SAND,
		BRONZE
	}
}
