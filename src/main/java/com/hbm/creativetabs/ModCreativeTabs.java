package com.hbm.creativetabs;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

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
		// placeholder icons (iron pickaxe, same fallback as the original) get replaced as content is ported
		tab(NtmTab.PARTS, () -> ModItems.ingot_uranium.get());
		tab(NtmTab.CONTROL, () -> Items.IRON_PICKAXE);
		tab(NtmTab.TEMPLATE, () -> Items.IRON_PICKAXE);
		tab(NtmTab.BLOCKS, () -> ModBlocks.ore_uranium.get());
		tab(NtmTab.MACHINE, () -> Items.IRON_PICKAXE);
		tab(NtmTab.NUKE, () -> Items.IRON_PICKAXE);
		tab(NtmTab.MISSILE, () -> Items.IRON_PICKAXE);
		tab(NtmTab.WEAPON, () -> Items.IRON_PICKAXE);
		tab(NtmTab.CONSUMABLE, () -> ModItems.geiger_counter.get());
	}

	private static void tab(NtmTab tab, Supplier<ItemLike> icon) {
		BY_TAB.put(tab, TABS.register(tab.name().toLowerCase(), () -> CreativeModeTab.builder()
				.title(Component.translatable("itemGroup." + tab.legacyName))
				.icon(() -> new ItemStack(icon.get()))
				.displayItems((params, output) -> tab.entries.forEach(e -> output.accept(e.get())))
				.build()));
	}
}
