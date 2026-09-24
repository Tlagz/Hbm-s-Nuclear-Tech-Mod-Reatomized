package com.hbm.module;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Burn time and heat multipliers per fuel category, used by burners and fireboxes. The categories came
 * from ore dict names in the original ("Coke", "Coal", "Lignite", "log", "Wood"), now from tag paths.
 *
 * TODO machine JSON config (readIfPresent/writeConfig of IConfigurableMachine)
 */
public class ModuleBurnTime {

	private static final int modLog = 0;
	private static final int modWood = 1;
	private static final int modCoal = 2;
	private static final int modLignite = 3;
	private static final int modCoke = 4;
	private static final int modSolid = 5;
	private static final int modRocket = 6;
	private static final int modBalefire = 7;

	private double[] modTime = new double[8];
	private double[] modHeat = new double[8];

	public ModuleBurnTime() {
		for(int i = 0; i < modTime.length; i++) {
			modTime[i] = 1.0D;
			modHeat[i] = 1.0D;
		}
	}

	public ModuleBurnTime setLogTimeMod(double mod) { this.modTime[modLog] = mod; return this; }
	public ModuleBurnTime setWoodTimeMod(double mod) { this.modTime[modWood] = mod; return this; }
	public ModuleBurnTime setCoalTimeMod(double mod) { this.modTime[modCoal] = mod; return this; }
	public ModuleBurnTime setLigniteTimeMod(double mod) { this.modTime[modLignite] = mod; return this; }
	public ModuleBurnTime setCokeTimeMod(double mod) { this.modTime[modCoke] = mod; return this; }
	public ModuleBurnTime setSolidTimeMod(double mod) { this.modTime[modSolid] = mod; return this; }
	public ModuleBurnTime setRocketTimeMod(double mod) { this.modTime[modRocket] = mod; return this; }
	public ModuleBurnTime setBalefireTimeMod(double mod) { this.modTime[modBalefire] = mod; return this; }

	public ModuleBurnTime setLogHeatMod(double mod) { this.modHeat[modLog] = mod; return this; }
	public ModuleBurnTime setWoodHeatMod(double mod) { this.modHeat[modWood] = mod; return this; }
	public ModuleBurnTime setCoalHeatMod(double mod) { this.modHeat[modCoal] = mod; return this; }
	public ModuleBurnTime setLigniteHeatMod(double mod) { this.modHeat[modLignite] = mod; return this; }
	public ModuleBurnTime setCokeHeatMod(double mod) { this.modHeat[modCoke] = mod; return this; }
	public ModuleBurnTime setSolidHeatMod(double mod) { this.modHeat[modSolid] = mod; return this; }
	public ModuleBurnTime setRocketHeatMod(double mod) { this.modHeat[modRocket] = mod; return this; }
	public ModuleBurnTime setBalefireHeatMod(double mod) { this.modHeat[modBalefire] = mod; return this; }

	/** Furnace burn time (vanilla + NTM fuel data map) times the category multiplier */
	public int getBurnTime(ItemStack stack, double def) {
		int fuel = stack.isEmpty() ? 0 : stack.getBurnTime(RecipeType.SMELTING);
		if(fuel <= 0) return 0;
		return (int) (fuel * getMod(stack, modTime, def));
	}

	public int getBurnTime(ItemStack stack) {
		return getBurnTime(stack, 1D);
	}

	public int getBurnHeat(int base, ItemStack stack, double def) {
		if(base <= 0) return 0;
		return (int) (base * getMod(stack, modHeat));
	}

	public int getBurnHeat(int base, ItemStack stack) {
		return getBurnHeat(base, stack, 1D);
	}

	public double getMod(ItemStack stack, double[] mod, double def) {
		if(stack.isEmpty()) return 0;

		Item item = stack.getItem();
		if(item == ModItems.solid_fuel.get() || item == ModItems.solid_fuel_presto.get() || item == ModItems.solid_fuel_presto_triplet.get()) return mod[modSolid];
		if(item == ModItems.solid_fuel_bf.get() || item == ModItems.solid_fuel_presto_bf.get() || item == ModItems.solid_fuel_presto_triplet_bf.get()) return mod[modBalefire];
		if(item == ModItems.rocket_fuel.get()) return mod[modRocket];

		return switch(getCategory(stack)) {
			case "coke" -> mod[modCoke];
			case "coal" -> mod[modCoal];
			case "lignite" -> mod[modLignite];
			case "log" -> mod[modLog];
			case "wood" -> mod[modWood];
			default -> def;
		};
	}

	/**
	 * The fuel category from the item's tags, in the same order as the original checked ore dict names.
	 * @return coke, coal, lignite, log, wood or other
	 */
	public static String getCategory(ItemStack stack) {
		if(stack.is(ItemTags.LOGS)) return "log";
		List<String> paths = stack.getTags().map(TagKey::location).map(l -> l.getPath()).toList();
		for(String path : paths) {
			if(path.contains("coke")) return "coke";
			if(path.contains("coal")) return "coal";
			if(path.contains("lignite")) return "lignite";
		}
		if(stack.is(ItemTags.PLANKS) || stack.is(ItemTags.WOODEN_SLABS) || stack.is(ItemTags.WOODEN_STAIRS)) return "wood";
		for(String path : paths) {
			if(path.contains("wood")) return "wood";
			if(path.contains("sapling")) return "sapling";
		}
		return "other";
	}

	public double getMod(ItemStack stack, double[] mod) {
		return getMod(stack, mod, 1D);
	}

	public List<String> getDesc() {
		List<String> desc = new ArrayList<>();
		desc.addAll(getTimeDesc());
		desc.addAll(getHeatDesc());
		return desc;
	}

	public List<String> getTimeDesc() {
		List<String> list = new ArrayList<>();

		list.add(ChatFormatting.GOLD + "Burn time bonuses:");

		addIf(list, "Logs", modTime[modLog]);
		addIf(list, "Wood", modTime[modWood]);
		addIf(list, "Coal", modTime[modCoal]);
		addIf(list, "Lignite", modTime[modLignite]);
		addIf(list, "Coke", modTime[modCoke]);
		addIf(list, "Solid Fuel", modTime[modSolid]);
		addIf(list, "Rocket Fuel", modTime[modRocket]);
		addIf(list, "Balefire", modTime[modBalefire]);

		if(list.size() == 1)
			list.clear();

		return list;
	}

	public List<String> getHeatDesc() {
		List<String> list = new ArrayList<>();

		list.add(ChatFormatting.RED + "Burn heat bonuses:");

		addIf(list, "Logs", modHeat[modLog]);
		addIf(list, "Wood", modHeat[modWood]);
		addIf(list, "Coal", modHeat[modCoal]);
		addIf(list, "Lignite", modHeat[modLignite]);
		addIf(list, "Coke", modHeat[modCoke]);
		addIf(list, "Solid Fuel", modHeat[modSolid]);
		addIf(list, "Rocket Fuel", modHeat[modRocket]);
		addIf(list, "Balefire", modHeat[modBalefire]);

		if(list.size() == 1)
			list.clear();

		return list;
	}

	private void addIf(List<String> list, String type, double mod) {
		if(mod != 1.0D) list.add(ChatFormatting.YELLOW + "- " + type + ": " + getPercent(mod));
	}

	private String getPercent(double mod) {
		mod -= 1D;
		String num = ((int) (mod * 100)) + "%";
		if(mod < 0) num = ChatFormatting.RED + num;
		else num = ChatFormatting.GREEN + "+" + num;
		return num;
	}
}
