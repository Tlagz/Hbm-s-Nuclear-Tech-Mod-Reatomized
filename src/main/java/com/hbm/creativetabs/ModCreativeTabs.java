package com.hbm.creativetabs;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import com.hbm.lib.RefStrings;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

	public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RefStrings.MODID);
	public static final Map<NtmTab, DeferredHolder<CreativeModeTab, CreativeModeTab>> BY_TAB = new EnumMap<>(NtmTab.class);

	static {
		// the original's tab icons, looked up by name so they show up as soon as the item is ported
		tab(NtmTab.PARTS, "ingot_uranium");
		tab(NtmTab.CONTROL, "pellet_rtg");
		tab(NtmTab.TEMPLATE, "blueprints");
		tab(NtmTab.BLOCKS, "ore_uranium");
		tab(NtmTab.MACHINE, "pwr_controller");
		tab(NtmTab.NUKE, "nuke_man");
		tab(NtmTab.MISSILE, "missile_nuclear");
		tab(NtmTab.WEAPON, "gun_greasegun");
		tab(NtmTab.CONSUMABLE, "bottle_nuka");
	}

	/** Original fallback for icons that don't exist (yet) */
	private static void tab(NtmTab tab, String icon) {
		tab(tab, () -> BuiltInRegistries.ITEM.getOptional(RefStrings.loc(icon)).orElse(Items.IRON_PICKAXE));
	}

	private static void tab(NtmTab tab, Supplier<ItemLike> icon) {
		BY_TAB.put(tab, TABS.register(tab.name().toLowerCase(), () -> CreativeModeTab.builder()
				.title(Component.translatable("itemGroup." + tab.legacyName))
				.icon(() -> new ItemStack(icon.get()))
				.displayItems((params, output) -> tab.entries.forEach(e -> output.accept(e.get())))
				.build()));
	}
}
