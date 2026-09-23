package com.hbm.creativetabs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.level.ItemLike;

/**
 * The creative tabs of the original mod. Registration helpers in ModBlocks/ModItems add their
 * entries here, ModCreativeTabs builds the actual tabs out of these lists.
 */
public enum NtmTab {

	PARTS("tabParts"),				// ingots, nuggets, wires, machine parts
	CONTROL("tabControl"),			// items that belong in machines, fuels, etc
	TEMPLATE("tabTemplate"),		// templates, siren tracks
	BLOCKS("tabBlocks"),			// ore and mineral blocks
	MACHINE("tabMachine"),			// machines, structure parts
	NUKE("tabNuke"),				// bombs
	MISSILE("tabMissile"),			// missiles, satellites
	WEAPON("tabWeapon"),			// turrets, weapons, ammo
	CONSUMABLE("tabConsumable");	// drinks, kits, tools

	public final String legacyName;
	public final List<Supplier<? extends ItemLike>> entries = new ArrayList<>();

	NtmTab(String legacyName) {
		this.legacyName = legacyName;
	}

	public void add(Supplier<? extends ItemLike> entry) {
		entries.add(entry);
	}
}
