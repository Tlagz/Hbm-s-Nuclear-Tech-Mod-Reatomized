package com.hbm.items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.hbm.creativetabs.NtmTab;
import com.hbm.items.ItemEnums.*;
import com.hbm.items.ItemGenericPart.EnumPartType;
import com.hbm.items.machine.ItemCircuit.EnumCircuitType;
import com.hbm.items.machine.ItemBatteryPack.EnumBatteryPack;
import com.hbm.items.machine.ItemBreedingRod.BreedingRodType;
import com.hbm.items.machine.ItemDrive.EnumDriveType;
import com.hbm.items.machine.ItemPileRodMK2.EnumPileRod;
import com.hbm.items.machine.ItemBattery;
import com.hbm.items.machine.ItemBatteryCreative;
import com.hbm.items.machine.ItemCanister;
import com.hbm.items.machine.ItemFluidIDMulti;
import com.hbm.items.machine.ItemFluidTank;
import com.hbm.items.machine.ItemGasTank;
import com.hbm.items.machine.ItemInfiniteFluid;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemStamp;
import com.hbm.items.machine.ItemStamp.StampType;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.special.ItemAutogen;
import com.hbm.items.special.ItemAutogen.AutogenItems;
import com.hbm.items.tool.ItemDosimeter;
import com.hbm.items.tool.ItemGeigerCounter;
import com.hbm.lib.RefStrings;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class ModItems {

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RefStrings.MODID);

	/** Items with a plain flat model, item -> texture path (e.g. "items/ingot_uranium"), used by datagen */
	public static final Map<DeferredItem<?>, String> FLAT_MODELS = new LinkedHashMap<>();
	/** Items with several tinted layers (layer0, layer1...), used by datagen, tints come from the item color handler */
	public static final Map<DeferredItem<?>, String[]> LAYERED_MODELS = new LinkedHashMap<>();
	/** Items drawn by an NTM item renderer (3D models), datagen gives them a builtin/entity model */
	public static final java.util.Set<DeferredItem<?>> ITEM_RENDERED = new java.util.LinkedHashSet<>();

	/// HAND-PORTED ///
	public static final DeferredItem<ItemDosimeter> dosimeter = register("dosimeter", ItemDosimeter::new, new Item.Properties().stacksTo(1), NtmTab.CONSUMABLE);
	public static final DeferredItem<ItemGeigerCounter> geiger_counter = register("geiger_counter", ItemGeigerCounter::new, new Item.Properties().stacksTo(1), NtmTab.CONSUMABLE);

	public static final DeferredItem<ItemBatteryCreative> battery_creative = register("battery_creative", ItemBatteryCreative::new, new Item.Properties().stacksTo(1), NtmTab.CONTROL);
	public static final DeferredItem<ItemBattery> battery_potato = register("battery_potato", p -> new ItemBattery(p, 1000, 0, 100), new Item.Properties().stacksTo(1), NtmTab.CONTROL);
	public static final DeferredItem<ItemBattery> cube_power = register("cube_power", p -> new ItemBattery(p, 1000000000000000000L, 1000000000000000L, 1000000000000000L), new Item.Properties().stacksTo(1), NtmTab.CONTROL);

	/// GENERATED from the original's declarations by tools/gen_content.py, don't edit by hand ///
	// BEGIN GENERATED
	public static final DeferredItem<Item> ingot_uranium = simple("ingot_uranium", NtmTab.PARTS, "items/ingot_uranium", new Item.Properties());
	public static final DeferredItem<Item> ingot_u233 = simple("ingot_u233", NtmTab.PARTS, "items/ingot_u233", new Item.Properties());
	public static final DeferredItem<Item> ingot_u235 = simple("ingot_u235", NtmTab.PARTS, "items/ingot_u235", new Item.Properties());
	public static final DeferredItem<Item> ingot_u238 = simple("ingot_u238", NtmTab.PARTS, "items/ingot_u238", new Item.Properties());
	public static final DeferredItem<Item> ingot_th232 = simple("ingot_th232", NtmTab.PARTS, "items/ingot_th232", new Item.Properties());
	public static final DeferredItem<Item> ingot_plutonium = simple("ingot_plutonium", NtmTab.PARTS, "items/ingot_plutonium", new Item.Properties());
	public static final DeferredItem<Item> ingot_pu238 = simple("ingot_pu238", NtmTab.PARTS, "items/ingot_pu238", new Item.Properties());
	public static final DeferredItem<Item> ingot_pu239 = simple("ingot_pu239", NtmTab.PARTS, "items/ingot_pu239", new Item.Properties());
	public static final DeferredItem<Item> ingot_pu240 = simple("ingot_pu240", NtmTab.PARTS, "items/ingot_pu240", new Item.Properties());
	public static final DeferredItem<Item> ingot_pu241 = simple("ingot_pu241", NtmTab.PARTS, "items/ingot_pu241", new Item.Properties());
	public static final DeferredItem<Item> ingot_pu_mix = simple("ingot_pu_mix", NtmTab.PARTS, "items/ingot_pu_mix", new Item.Properties());
	public static final DeferredItem<Item> ingot_am241 = simple("ingot_am241", NtmTab.PARTS, "items/ingot_am241", new Item.Properties());
	public static final DeferredItem<Item> ingot_am242 = simple("ingot_am242", NtmTab.PARTS, "items/ingot_am242", new Item.Properties());
	public static final DeferredItem<Item> ingot_am_mix = simple("ingot_am_mix", NtmTab.PARTS, "items/ingot_am_mix", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_neptunium = lore("ingot_neptunium", "ingot_neptunium", NtmTab.PARTS, "items/ingot_neptunium", new Item.Properties());
	public static final DeferredItem<Item> ingot_polonium = simple("ingot_polonium", NtmTab.PARTS, "items/ingot_polonium", new Item.Properties());
	public static final DeferredItem<Item> ingot_technetium = simple("ingot_technetium", NtmTab.PARTS, "items/ingot_technetium", new Item.Properties());
	public static final DeferredItem<Item> ingot_co60 = simple("ingot_co60", NtmTab.PARTS, "items/ingot_co60", new Item.Properties());
	public static final DeferredItem<Item> ingot_sr90 = simple("ingot_sr90", NtmTab.PARTS, "items/ingot_sr90", new Item.Properties());
	public static final DeferredItem<Item> ingot_au198 = simple("ingot_au198", NtmTab.PARTS, "items/ingot_au198", new Item.Properties());
	public static final DeferredItem<Item> ingot_pb209 = simple("ingot_pb209", NtmTab.PARTS, "items/ingot_pb209", new Item.Properties());
	public static final DeferredItem<Item> ingot_ra226 = simple("ingot_ra226", NtmTab.PARTS, "items/ingot_ra226", new Item.Properties());
	public static final DeferredItem<Item> ingot_titanium = simple("ingot_titanium", NtmTab.PARTS, "items/ingot_titanium", new Item.Properties());
	public static final DeferredItem<Item> ingot_copper = simple("ingot_copper", NtmTab.PARTS, "items/ingot_copper", new Item.Properties());
	public static final DeferredItem<Item> ingot_red_copper = simple("ingot_red_copper", NtmTab.PARTS, "items/ingot_red_copper", new Item.Properties());
	public static final DeferredItem<Item> ingot_tungsten = simple("ingot_tungsten", NtmTab.PARTS, "items/ingot_tungsten", new Item.Properties());
	public static final DeferredItem<Item> ingot_tungsten_carbide = simple("ingot_tungsten_carbide", NtmTab.PARTS, "items/ingot_tungsten_carbide", new Item.Properties());
	public static final DeferredItem<Item> ingot_aluminium = simple("ingot_aluminium", NtmTab.PARTS, "items/ingot_aluminium", new Item.Properties());
	public static final DeferredItem<Item> ingot_steel = simple("ingot_steel", NtmTab.PARTS, "items/ingot_steel", new Item.Properties());
	public static final DeferredItem<Item> ingot_tcalloy = simple("ingot_tcalloy", NtmTab.PARTS, "items/ingot_tcalloy", new Item.Properties());
	public static final DeferredItem<Item> ingot_cdalloy = simple("ingot_cdalloy", NtmTab.PARTS, "items/ingot_cdalloy", new Item.Properties());
	public static final DeferredItem<Item> ingot_bismuth_bronze = simple("ingot_bismuth_bronze", NtmTab.PARTS, "items/ingot_bismuth_bronze", new Item.Properties());
	public static final DeferredItem<Item> ingot_arsenic_bronze = simple("ingot_arsenic_bronze", NtmTab.PARTS, "items/ingot_arsenic_bronze", new Item.Properties());
	public static final DeferredItem<Item> ingot_bscco = simple("ingot_bscco", NtmTab.PARTS, "items/ingot_bscco", new Item.Properties());
	public static final DeferredItem<Item> ingot_lead = simple("ingot_lead", NtmTab.PARTS, "items/ingot_lead", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_bismuth = lore("ingot_bismuth", "ingot_bismuth", NtmTab.PARTS, "items/ingot_bismuth", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_arsenic = lore("ingot_arsenic", "ingot_arsenic", NtmTab.PARTS, "items/ingot_arsenic", new Item.Properties());
	public static final DeferredItem<Item> ingot_calcium = simple("ingot_calcium", NtmTab.PARTS, "items/ingot_calcium", new Item.Properties());
	public static final DeferredItem<Item> ingot_cadmium = simple("ingot_cadmium", NtmTab.PARTS, "items/ingot_cadmium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_tantalium = lore("ingot_tantalium", "ingot_tantalium", NtmTab.PARTS, "items/ingot_tantalium", new Item.Properties());
	public static final DeferredItem<Item> ingot_silicon = simple("ingot_silicon", NtmTab.PARTS, "items/ingot_silicon", new Item.Properties());
	public static final DeferredItem<Item> ingot_niobium = simple("ingot_niobium", NtmTab.PARTS, "items/ingot_niobium", new Item.Properties());
	public static final DeferredItem<Item> ingot_beryllium = simple("ingot_beryllium", NtmTab.PARTS, "items/ingot_beryllium", new Item.Properties());
	public static final DeferredItem<Item> ingot_cobalt = simple("ingot_cobalt", NtmTab.PARTS, "items/ingot_cobalt", new Item.Properties());
	public static final DeferredItem<Item> ingot_boron = simple("ingot_boron", NtmTab.PARTS, "items/ingot_boron", new Item.Properties());
	public static final DeferredItem<Item> ingot_graphite = simple("ingot_graphite", NtmTab.PARTS, "items/ingot_graphite", new Item.Properties());
	public static final DeferredItem<Item> ingot_firebrick = simple("ingot_firebrick", NtmTab.PARTS, "items/ingot_firebrick", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_dura_steel = lore("ingot_dura_steel", "ingot_dura_steel", NtmTab.PARTS, "items/ingot_dura_steel", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_polymer = lore("ingot_polymer", "ingot_polymer", NtmTab.PARTS, "items/ingot_polymer", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_bakelite = lore("ingot_bakelite", "ingot_bakelite", NtmTab.PARTS, "items/ingot_bakelite", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_biorubber = lore("ingot_biorubber", "ingot_biorubber", NtmTab.PARTS, "items/ingot_biorubber", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_rubber = lore("ingot_rubber", "ingot_rubber", NtmTab.PARTS, "items/ingot_rubber", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_pc = lore("ingot_pc", "ingot_pc", NtmTab.PARTS, "items/ingot_pc", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_pvc = lore("ingot_pvc", "ingot_pvc", NtmTab.PARTS, "items/ingot_pvc", new Item.Properties());
	public static final DeferredItem<Item> ingot_mud = simple("ingot_mud", NtmTab.PARTS, "items/ingot_mud", new Item.Properties());
	public static final DeferredItem<Item> ingot_cft = simple("ingot_cft", NtmTab.PARTS, "items/ingot_cft", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_schrabidium = lore("ingot_schrabidium", "ingot_schrabidium", NtmTab.PARTS, "items/ingot_schrabidium", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<ItemCustomLore> ingot_schrabidate = lore("ingot_schrabidate", "ingot_schrabidate", NtmTab.PARTS, "items/ingot_schrabidate", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<Item> ingot_magnetized_tungsten = simple("ingot_magnetized_tungsten", NtmTab.PARTS, "items/ingot_magnetized_tungsten", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_combine_steel = lore("ingot_combine_steel", "ingot_combine_steel", NtmTab.PARTS, "items/ingot_combine_steel", new Item.Properties());
	public static final DeferredItem<Item> ingot_solinium = simple("ingot_solinium", NtmTab.PARTS, "items/ingot_solinium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_gh336 = lore("ingot_gh336", "ingot_gh336", NtmTab.PARTS, "items/ingot_gh336", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<Item> ingot_uranium_fuel = simple("ingot_uranium_fuel", NtmTab.PARTS, "items/ingot_uranium_fuel", new Item.Properties());
	public static final DeferredItem<Item> ingot_thorium_fuel = simple("ingot_thorium_fuel", NtmTab.PARTS, "items/ingot_thorium_fuel", new Item.Properties());
	public static final DeferredItem<Item> ingot_plutonium_fuel = simple("ingot_plutonium_fuel", NtmTab.PARTS, "items/ingot_plutonium_fuel", new Item.Properties());
	public static final DeferredItem<Item> ingot_neptunium_fuel = simple("ingot_neptunium_fuel", NtmTab.PARTS, "items/ingot_neptunium_fuel", new Item.Properties());
	public static final DeferredItem<Item> ingot_mox_fuel = simple("ingot_mox_fuel", NtmTab.PARTS, "items/ingot_mox_fuel", new Item.Properties());
	public static final DeferredItem<Item> ingot_americium_fuel = simple("ingot_americium_fuel", NtmTab.PARTS, "items/ingot_americium_fuel", new Item.Properties());
	public static final DeferredItem<Item> ingot_schrabidium_fuel = simple("ingot_schrabidium_fuel", NtmTab.PARTS, "items/ingot_schrabidium_fuel", new Item.Properties());
	public static final DeferredItem<Item> ingot_hes = simple("ingot_hes", NtmTab.PARTS, "items/ingot_hes", new Item.Properties());
	public static final DeferredItem<Item> ingot_les = simple("ingot_les", NtmTab.PARTS, "items/ingot_les", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_australium = lore("ingot_australium", "ingot_australium", NtmTab.PARTS, "items/ingot_australium", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> ingot_lanthanium = lore("ingot_lanthanium", "ingot_lanthanium", NtmTab.PARTS, "items/ingot_lanthanium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_actinium = lore("ingot_actinium", "ingot_actinium", NtmTab.PARTS, "items/ingot_actinium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_desh = lore("ingot_desh", "ingot_desh", NtmTab.PARTS, "items/ingot_desh", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_ferrouranium = lore("ingot_ferrouranium", "ingot_ferrouranium", NtmTab.PARTS, "items/ingot_ferrouranium", new Item.Properties());
	public static final DeferredItem<Item> ingot_gunmetal = simple("ingot_gunmetal", NtmTab.PARTS, "items/ingot_gunmetal", new Item.Properties());
	public static final DeferredItem<Item> ingot_weaponsteel = simple("ingot_weaponsteel", NtmTab.PARTS, "items/ingot_gunsteel", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_saturnite = lore("ingot_saturnite", "ingot_saturnite", NtmTab.PARTS, "items/ingot_saturnite", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<ItemCustomLore> ingot_euphemium = lore("ingot_euphemium", "ingot_euphemium", NtmTab.PARTS, "items/ingot_euphemium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> ingot_dineutronium = lore("ingot_dineutronium", "ingot_dineutronium", NtmTab.PARTS, "items/ingot_dineutronium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_electronium = lore("ingot_electronium", "ingot_electronium", NtmTab.PARTS, "items/ingot_electronium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_osmiridium = lore("ingot_osmiridium", "ingot_osmiridium", NtmTab.PARTS, "items/ingot_osmiridium", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<Item> ingot_phosphorus = simple("ingot_phosphorus", NtmTab.PARTS, "items/ingot_phosphorus", new Item.Properties());
	public static final DeferredItem<Item> lithium = simple("lithium", NtmTab.PARTS, "items/lithium", new Item.Properties());
	public static final DeferredItem<Item> ingot_zirconium = simple("ingot_zirconium", NtmTab.PARTS, "items/ingot_zirconium", new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumTarType> oil_tar = multi("oil_tar", "oil_tar", EnumTarType.class, true, true, NtmTab.PARTS, new Item.Properties());
	public static final DeferredItem<Item> solid_fuel = simple("solid_fuel", NtmTab.PARTS, "items/solid_fuel", new Item.Properties());
	public static final DeferredItem<Item> solid_fuel_presto = simple("solid_fuel_presto", NtmTab.PARTS, "items/solid_fuel_presto", new Item.Properties());
	public static final DeferredItem<Item> solid_fuel_presto_triplet = simple("solid_fuel_presto_triplet", NtmTab.PARTS, "items/solid_fuel_presto_triplet", new Item.Properties());
	public static final DeferredItem<Item> solid_fuel_bf = simple("solid_fuel_bf", NtmTab.PARTS, "items/solid_fuel_bf", new Item.Properties());
	public static final DeferredItem<Item> solid_fuel_presto_bf = simple("solid_fuel_presto_bf", NtmTab.PARTS, "items/solid_fuel_presto_bf", new Item.Properties());
	public static final DeferredItem<Item> solid_fuel_presto_triplet_bf = simple("solid_fuel_presto_triplet_bf", NtmTab.PARTS, "items/solid_fuel_presto_triplet_bf", new Item.Properties());
	public static final DeferredItem<Item> rocket_fuel = simple("rocket_fuel", NtmTab.PARTS, "items/rocket_fuel", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_fiberglass = lore("ingot_fiberglass", "ingot_fiberglass", NtmTab.PARTS, "items/ingot_fiberglass", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_asbestos = lore("ingot_asbestos", "ingot_asbestos", NtmTab.PARTS, "items/ingot_asbestos", new Item.Properties());
	public static final DeferredItem<Item> billet_uranium = simple("billet_uranium", NtmTab.PARTS, "items/billet_uranium", new Item.Properties());
	public static final DeferredItem<Item> billet_u233 = simple("billet_u233", NtmTab.PARTS, "items/billet_u233", new Item.Properties());
	public static final DeferredItem<Item> billet_u235 = simple("billet_u235", NtmTab.PARTS, "items/billet_u235", new Item.Properties());
	public static final DeferredItem<Item> billet_u238 = simple("billet_u238", NtmTab.PARTS, "items/billet_u238", new Item.Properties());
	public static final DeferredItem<Item> billet_uzh = simple("billet_uzh", NtmTab.PARTS, "items/billet_uzh", new Item.Properties());
	public static final DeferredItem<Item> billet_th232 = simple("billet_th232", NtmTab.PARTS, "items/billet_th232", new Item.Properties());
	public static final DeferredItem<Item> billet_plutonium = simple("billet_plutonium", NtmTab.PARTS, "items/billet_plutonium", new Item.Properties());
	public static final DeferredItem<Item> billet_pu238 = simple("billet_pu238", NtmTab.PARTS, "items/billet_pu238", new Item.Properties());
	public static final DeferredItem<Item> billet_pu239 = simple("billet_pu239", NtmTab.PARTS, "items/billet_pu239", new Item.Properties());
	public static final DeferredItem<Item> billet_pu240 = simple("billet_pu240", NtmTab.PARTS, "items/billet_pu240", new Item.Properties());
	public static final DeferredItem<Item> billet_pu241 = simple("billet_pu241", NtmTab.PARTS, "items/billet_pu241", new Item.Properties());
	public static final DeferredItem<Item> billet_pu_mix = simple("billet_pu_mix", NtmTab.PARTS, "items/billet_pu_mix", new Item.Properties());
	public static final DeferredItem<Item> billet_am241 = simple("billet_am241", NtmTab.PARTS, "items/billet_am241", new Item.Properties());
	public static final DeferredItem<Item> billet_am242 = simple("billet_am242", NtmTab.PARTS, "items/billet_am242", new Item.Properties());
	public static final DeferredItem<Item> billet_am_mix = simple("billet_am_mix", NtmTab.PARTS, "items/billet_am_mix", new Item.Properties());
	public static final DeferredItem<Item> billet_neptunium = simple("billet_neptunium", NtmTab.PARTS, "items/billet_neptunium", new Item.Properties());
	public static final DeferredItem<Item> billet_polonium = simple("billet_polonium", NtmTab.PARTS, "items/billet_polonium", new Item.Properties());
	public static final DeferredItem<Item> billet_technetium = simple("billet_technetium", NtmTab.PARTS, "items/billet_technetium", new Item.Properties());
	public static final DeferredItem<Item> billet_cobalt = simple("billet_cobalt", NtmTab.PARTS, "items/billet_cobalt", new Item.Properties());
	public static final DeferredItem<Item> billet_co60 = simple("billet_co60", NtmTab.PARTS, "items/billet_co60", new Item.Properties());
	public static final DeferredItem<Item> billet_sr90 = simple("billet_sr90", NtmTab.PARTS, "items/billet_sr90", new Item.Properties());
	public static final DeferredItem<Item> billet_au198 = simple("billet_au198", NtmTab.PARTS, "items/billet_au198", new Item.Properties());
	public static final DeferredItem<Item> billet_pb209 = simple("billet_pb209", NtmTab.PARTS, "items/billet_pb209", new Item.Properties());
	public static final DeferredItem<Item> billet_ra226 = simple("billet_ra226", NtmTab.PARTS, "items/billet_ra226", new Item.Properties());
	public static final DeferredItem<Item> billet_actinium = simple("billet_actinium", NtmTab.PARTS, "items/billet_actinium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> billet_schrabidium = lore("billet_schrabidium", "billet_schrabidium", NtmTab.PARTS, "items/billet_schrabidium", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<Item> billet_solinium = simple("billet_solinium", NtmTab.PARTS, "items/billet_solinium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> billet_gh336 = lore("billet_gh336", "billet_gh336", NtmTab.PARTS, "items/billet_gh336", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> billet_australium = lore("billet_australium", "billet_australium", NtmTab.PARTS, "items/billet_australium", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> billet_australium_lesser = lore("billet_australium_lesser", "billet_australium_lesser", NtmTab.PARTS, "items/billet_australium_lesser", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> billet_australium_greater = lore("billet_australium_greater", "billet_australium_greater", NtmTab.PARTS, "items/billet_australium_greater", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> billet_uranium_fuel = simple("billet_uranium_fuel", NtmTab.PARTS, "items/billet_uranium_fuel", new Item.Properties());
	public static final DeferredItem<Item> billet_thorium_fuel = simple("billet_thorium_fuel", NtmTab.PARTS, "items/billet_thorium_fuel", new Item.Properties());
	public static final DeferredItem<Item> billet_plutonium_fuel = simple("billet_plutonium_fuel", NtmTab.PARTS, "items/billet_plutonium_fuel", new Item.Properties());
	public static final DeferredItem<Item> billet_neptunium_fuel = simple("billet_neptunium_fuel", NtmTab.PARTS, "items/billet_neptunium_fuel", new Item.Properties());
	public static final DeferredItem<Item> billet_mox_fuel = simple("billet_mox_fuel", NtmTab.PARTS, "items/billet_mox_fuel", new Item.Properties());
	public static final DeferredItem<Item> billet_americium_fuel = simple("billet_americium_fuel", NtmTab.PARTS, "items/billet_americium_fuel", new Item.Properties());
	public static final DeferredItem<Item> billet_les = simple("billet_les", NtmTab.PARTS, "items/billet_les", new Item.Properties());
	public static final DeferredItem<Item> billet_schrabidium_fuel = simple("billet_schrabidium_fuel", NtmTab.PARTS, "items/billet_schrabidium_fuel", new Item.Properties());
	public static final DeferredItem<Item> billet_hes = simple("billet_hes", NtmTab.PARTS, "items/billet_hes", new Item.Properties());
	public static final DeferredItem<Item> billet_po210be = simple("billet_po210be", NtmTab.PARTS, "items/billet_po210be", new Item.Properties());
	public static final DeferredItem<Item> billet_ra226be = simple("billet_ra226be", NtmTab.PARTS, "items/billet_ra226be", new Item.Properties());
	public static final DeferredItem<Item> billet_pu238be = simple("billet_pu238be", NtmTab.PARTS, "items/billet_pu238be", new Item.Properties());
	public static final DeferredItem<Item> billet_beryllium = simple("billet_beryllium", NtmTab.PARTS, "items/billet_beryllium", new Item.Properties());
	public static final DeferredItem<Item> billet_bismuth = simple("billet_bismuth", NtmTab.PARTS, "items/billet_bismuth", new Item.Properties());
	public static final DeferredItem<Item> billet_silicon = simple("billet_silicon", NtmTab.PARTS, "items/billet_silicon", new Item.Properties());
	public static final DeferredItem<Item> billet_zirconium = simple("billet_zirconium", NtmTab.PARTS, "items/billet_zirconium", new Item.Properties());
	public static final DeferredItem<Item> billet_zfb_bismuth = simple("billet_zfb_bismuth", NtmTab.PARTS, "items/billet_zfb_bismuth", new Item.Properties());
	public static final DeferredItem<Item> billet_zfb_pu241 = simple("billet_zfb_pu241", NtmTab.PARTS, "items/billet_zfb_pu241", new Item.Properties());
	public static final DeferredItem<Item> billet_zfb_am_mix = simple("billet_zfb_am_mix", NtmTab.PARTS, "items/billet_zfb_am_mix", new Item.Properties());
	public static final DeferredItem<Item> billet_yharonite = simple("billet_yharonite", NtmTab.PARTS, "items/billet_yharonite", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> billet_balefire_gold = lore("billet_balefire_gold", "billet_balefire_gold", NtmTab.PARTS, "items/billet_balefire_gold", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> billet_flashlead = lore("billet_flashlead", "billet_flashlead", NtmTab.PARTS, "items/billet_flashlead", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> billet_nuclear_waste = simple("billet_nuclear_waste", NtmTab.PARTS, "items/billet_nuclear_waste", new Item.Properties());
	public static final DeferredItem<Item> cinnebar = simple("cinnebar", NtmTab.PARTS, "items/cinnebar", new Item.Properties());
	public static final DeferredItem<Item> nugget_mercury = simple("nugget_mercury_tiny", NtmTab.PARTS, "items/nugget_mercury_tiny", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ingot_mercury = lore("nugget_mercury", "nugget_mercury", NtmTab.PARTS, "items/nugget_mercury", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> bottle_mercury = lore("bottle_mercury", "bottle_mercury", NtmTab.PARTS, "items/bottle_mercury", new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumCokeType> coke = multi("coke", "coke", EnumCokeType.class, true, true, NtmTab.PARTS, new Item.Properties());
	public static final DeferredItem<Item> lignite = simple("lignite", NtmTab.PARTS, "items/lignite", new Item.Properties());
	public static final DeferredItem<Item> coal_infernal = simple("coal_infernal", NtmTab.PARTS, "items/coal_infernal", new Item.Properties());
	public static final DeferredItem<Item> coal_eternal = simple("coal_eternal", null, "items/coal_eternal", new Item.Properties().stacksTo(1));
	public static final ItemEnumMulti.Variants<EnumBriquetteType> briquette = multi("briquette", "briquette", EnumBriquetteType.class, true, true, NtmTab.PARTS, new Item.Properties());
	public static final DeferredItem<Item> sulfur = simple("sulfur", NtmTab.PARTS, "items/sulfur", new Item.Properties());
	public static final DeferredItem<Item> niter = simple("niter", NtmTab.PARTS, "items/salpeter", new Item.Properties());
	public static final DeferredItem<Item> nitra = simple("nitra", NtmTab.PARTS, "items/nitra", new Item.Properties());
	public static final DeferredItem<Item> nitra_small = simple("nitra_small", NtmTab.PARTS, "items/nitra_small", new Item.Properties());
	public static final DeferredItem<Item> fluorite = simple("fluorite", NtmTab.PARTS, "items/fluorite", new Item.Properties());
	public static final DeferredItem<Item> powder_coal = simple("powder_coal", NtmTab.PARTS, "items/powder_coal", new Item.Properties());
	public static final DeferredItem<Item> powder_coal_tiny = simple("powder_coal_tiny", NtmTab.PARTS, "items/powder_coal_tiny", new Item.Properties());
	public static final DeferredItem<Item> powder_iron = simple("powder_iron", NtmTab.PARTS, "items/powder_iron", new Item.Properties());
	public static final DeferredItem<Item> powder_gold = simple("powder_gold", NtmTab.PARTS, "items/powder_gold", new Item.Properties());
	public static final DeferredItem<Item> powder_lapis = simple("powder_lapis", NtmTab.PARTS, "items/powder_lapis", new Item.Properties());
	public static final DeferredItem<Item> powder_quartz = simple("powder_quartz", NtmTab.PARTS, "items/powder_quartz", new Item.Properties());
	public static final DeferredItem<Item> powder_diamond = simple("powder_diamond", NtmTab.PARTS, "items/powder_diamond", new Item.Properties());
	public static final DeferredItem<Item> powder_emerald = simple("powder_emerald", NtmTab.PARTS, "items/powder_emerald", new Item.Properties());
	public static final DeferredItem<Item> powder_uranium = simple("powder_uranium", NtmTab.PARTS, "items/powder_uranium", new Item.Properties());
	public static final DeferredItem<Item> powder_plutonium = simple("powder_plutonium", NtmTab.PARTS, "items/powder_plutonium", new Item.Properties());
	public static final DeferredItem<Item> powder_neptunium = simple("powder_neptunium", NtmTab.PARTS, "items/powder_neptunium", new Item.Properties());
	public static final DeferredItem<Item> powder_polonium = simple("powder_polonium", NtmTab.PARTS, "items/powder_polonium", new Item.Properties());
	public static final DeferredItem<Item> powder_co60 = simple("powder_co60", NtmTab.PARTS, "items/powder_co60", new Item.Properties());
	public static final DeferredItem<Item> powder_sr90 = simple("powder_sr90", NtmTab.PARTS, "items/powder_sr90", new Item.Properties());
	public static final DeferredItem<Item> powder_sr90_tiny = simple("powder_sr90_tiny", NtmTab.PARTS, "items/powder_sr90_tiny", new Item.Properties());
	public static final DeferredItem<Item> powder_i131 = simple("powder_i131", NtmTab.PARTS, "items/powder_i131", new Item.Properties());
	public static final DeferredItem<Item> powder_i131_tiny = simple("powder_i131_tiny", NtmTab.PARTS, "items/powder_i131_tiny", new Item.Properties());
	public static final DeferredItem<Item> powder_xe135 = simple("powder_xe135", NtmTab.PARTS, "items/powder_xe135", new Item.Properties());
	public static final DeferredItem<Item> powder_xe135_tiny = simple("powder_xe135_tiny", NtmTab.PARTS, "items/powder_xe135_tiny", new Item.Properties());
	public static final DeferredItem<Item> powder_cs137 = simple("powder_cs137", NtmTab.PARTS, "items/powder_cs137", new Item.Properties());
	public static final DeferredItem<Item> powder_cs137_tiny = simple("powder_cs137_tiny", NtmTab.PARTS, "items/powder_cs137_tiny", new Item.Properties());
	public static final DeferredItem<Item> powder_au198 = simple("powder_au198", NtmTab.PARTS, "items/powder_au198", new Item.Properties());
	public static final DeferredItem<Item> powder_ra226 = simple("powder_ra226", NtmTab.PARTS, "items/powder_ra226", new Item.Properties());
	public static final DeferredItem<Item> powder_at209 = simple("powder_at209", NtmTab.PARTS, "items/powder_at209", new Item.Properties());
	public static final DeferredItem<Item> powder_titanium = simple("powder_titanium", NtmTab.PARTS, "items/powder_titanium", new Item.Properties());
	public static final DeferredItem<Item> powder_copper = simple("powder_copper", NtmTab.PARTS, "items/powder_copper", new Item.Properties());
	public static final DeferredItem<Item> powder_red_copper = simple("powder_red_copper", NtmTab.PARTS, "items/powder_red_copper", new Item.Properties());
	public static final DeferredItem<Item> powder_tungsten = simple("powder_tungsten", NtmTab.PARTS, "items/powder_tungsten", new Item.Properties());
	public static final DeferredItem<Item> powder_aluminium = simple("powder_aluminium", NtmTab.PARTS, "items/powder_aluminium", new Item.Properties());
	public static final DeferredItem<Item> powder_steel = simple("powder_steel", NtmTab.PARTS, "items/powder_steel", new Item.Properties());
	public static final DeferredItem<Item> powder_steel_tiny = simple("powder_steel_tiny", NtmTab.PARTS, "items/powder_steel_tiny", new Item.Properties());
	public static final DeferredItem<Item> powder_tcalloy = simple("powder_tcalloy", NtmTab.PARTS, "items/powder_tcalloy", new Item.Properties());
	public static final DeferredItem<Item> powder_lead = simple("powder_lead", NtmTab.PARTS, "items/powder_lead", new Item.Properties());
	public static final DeferredItem<Item> powder_bismuth = simple("powder_bismuth", NtmTab.PARTS, "items/powder_bismuth", new Item.Properties());
	public static final DeferredItem<Item> powder_calcium = simple("powder_calcium", NtmTab.PARTS, "items/powder_calcium", new Item.Properties());
	public static final DeferredItem<Item> powder_cadmium = simple("powder_cadmium", NtmTab.PARTS, "items/powder_cadmium", new Item.Properties());
	public static final DeferredItem<Item> powder_coltan_ore = simple("powder_coltan_ore", NtmTab.PARTS, "items/powder_coltan_ore", new Item.Properties());
	public static final DeferredItem<Item> powder_coltan = simple("powder_coltan", NtmTab.PARTS, "items/powder_coltan", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_tantalium = lore("powder_tantalium", "powder_tantalium", NtmTab.PARTS, "items/powder_tantalium", new Item.Properties());
	public static final DeferredItem<Item> powder_tektite = simple("powder_tektite", NtmTab.PARTS, "items/powder_tektite", new Item.Properties());
	public static final DeferredItem<Item> powder_paleogenite = simple("powder_paleogenite", NtmTab.PARTS, "items/powder_paleogenite", new Item.Properties());
	public static final DeferredItem<Item> powder_paleogenite_tiny = simple("powder_paleogenite_tiny", NtmTab.PARTS, "items/powder_paleogenite_tiny", new Item.Properties());
	public static final DeferredItem<Item> powder_impure_osmiridium = simple("powder_impure_osmiridium", NtmTab.PARTS, "items/powder_impure_osmiridium", new Item.Properties());
	public static final DeferredItem<Item> powder_borax = simple("powder_borax", NtmTab.PARTS, "items/powder_borax", new Item.Properties());
	public static final DeferredItem<Item> powder_chlorocalcite = simple("powder_chlorocalcite", NtmTab.PARTS, "items/powder_chlorocalcite", new Item.Properties());
	public static final DeferredItem<Item> powder_molysite = simple("powder_molysite", NtmTab.PARTS, "items/powder_molysite", new Item.Properties());
	public static final DeferredItem<Item> powder_yellowcake = simple("powder_yellowcake", NtmTab.PARTS, "items/powder_yellowcake", new Item.Properties());
	public static final DeferredItem<Item> powder_beryllium = simple("powder_beryllium", NtmTab.PARTS, "items/powder_beryllium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_dura_steel = lore("powder_dura_steel", "powder_dura_steel", NtmTab.PARTS, "items/powder_dura_steel", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_polymer = lore("powder_polymer", "powder_polymer", NtmTab.PARTS, "items/powder_polymer", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_bakelite = lore("powder_bakelite", "powder_bakelite", NtmTab.PARTS, "items/powder_bakelite", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_schrabidium = lore("powder_schrabidium", "powder_schrabidium", NtmTab.PARTS, "items/powder_schrabidium", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<ItemCustomLore> powder_schrabidate = lore("powder_schrabidate", "powder_schrabidate", NtmTab.PARTS, "items/powder_schrabidate", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<Item> powder_magnetized_tungsten = simple("powder_magnetized_tungsten", NtmTab.PARTS, "items/powder_magnetized_tungsten", new Item.Properties());
	public static final DeferredItem<Item> powder_chlorophyte = simple("powder_chlorophyte", NtmTab.PARTS, "items/powder_chlorophyte", new Item.Properties());
	public static final DeferredItem<Item> powder_combine_steel = simple("powder_combine_steel", NtmTab.PARTS, "items/powder_combine_steel", new Item.Properties());
	public static final DeferredItem<Item> powder_lithium = simple("powder_lithium", NtmTab.PARTS, "items/powder_lithium", new Item.Properties());
	public static final DeferredItem<Item> powder_lithium_tiny = simple("powder_lithium_tiny", NtmTab.PARTS, "items/powder_lithium_tiny", new Item.Properties());
	public static final DeferredItem<Item> powder_zirconium = simple("powder_zirconium", NtmTab.PARTS, "items/powder_zirconium", new Item.Properties());
	public static final DeferredItem<Item> powder_sodium = simple("powder_sodium", NtmTab.PARTS, "items/powder_sodium", new Item.Properties());
	public static final DeferredItem<Item> powder_lignite = simple("powder_lignite", NtmTab.PARTS, "items/powder_lignite", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_iodine = lore("powder_iodine", "powder_iodine", NtmTab.PARTS, "items/powder_iodine", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> powder_thorium = lore("powder_thorium", "powder_thorium", NtmTab.PARTS, "items/powder_thorium", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> powder_neodymium = lore("powder_neodymium", "powder_neodymium", NtmTab.PARTS, "items/powder_neodymium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<Item> powder_neodymium_tiny = simple("powder_neodymium_tiny", NtmTab.PARTS, "items/powder_neodymium_tiny", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_astatine = lore("powder_astatine", "powder_astatine", NtmTab.PARTS, "items/powder_astatine", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> powder_caesium = lore("powder_caesium", "powder_caesium", NtmTab.PARTS, "items/powder_caesium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> powder_australium = lore("powder_australium", "powder_australium", NtmTab.PARTS, "items/powder_australium", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> powder_strontium = lore("powder_strontium", "powder_strontium", NtmTab.PARTS, "items/powder_strontium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> powder_cobalt = lore("powder_cobalt", "powder_cobalt", NtmTab.PARTS, "items/powder_cobalt", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<Item> powder_cobalt_tiny = simple("powder_cobalt_tiny", NtmTab.PARTS, "items/powder_cobalt_tiny", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_bromine = lore("powder_bromine", "powder_bromine", NtmTab.PARTS, "items/powder_bromine", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> powder_niobium = lore("powder_niobium", "powder_niobium", NtmTab.PARTS, "items/powder_niobium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<Item> powder_niobium_tiny = simple("powder_niobium_tiny", NtmTab.PARTS, "items/powder_niobium_tiny", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_tennessine = lore("powder_tennessine", "powder_tennessine", NtmTab.PARTS, "items/powder_tennessine", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> powder_cerium = lore("powder_cerium", "powder_cerium", NtmTab.PARTS, "items/powder_cerium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<Item> powder_cerium_tiny = simple("powder_cerium_tiny", NtmTab.PARTS, "items/powder_cerium_tiny", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_lanthanium = lore("powder_lanthanium", "powder_lanthanium", NtmTab.PARTS, "items/powder_lanthanium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<Item> powder_lanthanium_tiny = simple("powder_lanthanium_tiny", NtmTab.PARTS, "items/powder_lanthanium_tiny", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_actinium = lore("powder_actinium", "powder_actinium", NtmTab.PARTS, "items/powder_actinium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<Item> powder_actinium_tiny = simple("powder_actinium_tiny", NtmTab.PARTS, "items/powder_actinium_tiny", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_boron = lore("powder_boron", "powder_boron", NtmTab.PARTS, "items/powder_boron", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<Item> powder_boron_tiny = simple("powder_boron_tiny", NtmTab.PARTS, "items/powder_boron_tiny", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_asbestos = lore("powder_asbestos", "powder_asbestos", NtmTab.PARTS, "items/powder_asbestos", new Item.Properties());
	public static final DeferredItem<Item> powder_magic = simple("powder_magic", NtmTab.PARTS, "items/powder_magic", new Item.Properties());
	public static final DeferredItem<Item> powder_sawdust = simple("powder_sawdust", NtmTab.PARTS, "items/powder_sawdust", new Item.Properties());
	public static final DeferredItem<Item> powder_flux = simple("powder_flux", NtmTab.PARTS, "items/powder_flux", new Item.Properties());
	public static final DeferredItem<Item> powder_balefire = simple("powder_balefire", NtmTab.PARTS, "items/powder_balefire", new Item.Properties());
	public static final DeferredItem<Item> powder_semtex_mix = simple("powder_semtex_mix", NtmTab.PARTS, "items/powder_semtex_mix", new Item.Properties());
	public static final DeferredItem<Item> powder_desh_mix = simple("powder_desh_mix", NtmTab.PARTS, "items/powder_desh_mix", new Item.Properties());
	public static final DeferredItem<Item> powder_desh_ready = simple("powder_desh_ready", NtmTab.PARTS, "items/powder_desh_ready", new Item.Properties());
	public static final DeferredItem<Item> powder_desh = simple("powder_desh", NtmTab.PARTS, "items/powder_desh", new Item.Properties());
	public static final DeferredItem<Item> powder_nitan_mix = simple("powder_nitan_mix", NtmTab.PARTS, "items/powder_nitan_mix", new Item.Properties());
	public static final DeferredItem<Item> powder_spark_mix = simple("powder_spark_mix", NtmTab.PARTS, "items/powder_spark_mix", new Item.Properties());
	public static final DeferredItem<Item> powder_meteorite = simple("powder_meteorite", NtmTab.PARTS, "items/powder_meteorite", new Item.Properties());
	public static final DeferredItem<Item> powder_meteorite_tiny = simple("powder_meteorite_tiny", NtmTab.PARTS, "items/powder_meteorite_tiny", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_euphemium = lore("powder_euphemium", "powder_euphemium", NtmTab.PARTS, "items/powder_euphemium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> powder_dineutronium = lore("powder_dineutronium", "powder_dineutronium", NtmTab.PARTS, "items/powder_dineutronium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> dust = lore("dust", "dust", NtmTab.PARTS, "items/dust", new Item.Properties());
	public static final DeferredItem<Item> dust_tiny = simple("dust_tiny", NtmTab.PARTS, "items/dust_tiny", new Item.Properties());
	public static final DeferredItem<Item> fallout = simple("fallout", NtmTab.PARTS, "items/fallout", new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumAshType> powder_ash = multi("powder_ash", "powder_ash", EnumAshType.class, true, true, NtmTab.PARTS, new Item.Properties());
	public static final DeferredItem<Item> powder_limestone = simple("powder_limestone", NtmTab.PARTS, "items/powder_limestone", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_fire = lore("powder_fire", "powder_fire", NtmTab.PARTS, "items/powder_red_phosphorus", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_ice = lore("powder_ice", "powder_ice", NtmTab.PARTS, "items/powder_ice", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_poison = lore("powder_poison", "powder_poison", NtmTab.PARTS, "items/powder_poison", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_thermite = lore("powder_thermite", "powder_thermite", NtmTab.PARTS, "items/powder_thermite", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> powder_power = lore("powder_power", "powder_power", NtmTab.PARTS, "items/powder_energy_alt", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> cordite = simple("cordite", NtmTab.PARTS, "items/cordite", new Item.Properties());
	public static final DeferredItem<Item> ballistite = simple("ballistite", NtmTab.PARTS, "items/ballistite", new Item.Properties());
	public static final DeferredItem<Item> ball_dynamite = simple("ball_dynamite", NtmTab.PARTS, "items/ball_dynamite", new Item.Properties());
	public static final DeferredItem<Item> ball_tnt = simple("ball_tnt", NtmTab.PARTS, "items/ball_tnt", new Item.Properties());
	public static final DeferredItem<Item> ball_tatb = simple("ball_tatb", NtmTab.PARTS, "items/ball_tatb", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> ball_resin = lore("ball_resin", "ball_resin", NtmTab.PARTS, "items/ball_resin", new Item.Properties());
	public static final DeferredItem<Item> ball_fireclay = simple("ball_fireclay", NtmTab.PARTS, "items/ball_fireclay", new Item.Properties());
	public static final DeferredItem<Item> crystal_coal = simple("crystal_coal", NtmTab.PARTS, "items/crystal_coal", new Item.Properties());
	public static final DeferredItem<Item> crystal_iron = simple("crystal_iron", NtmTab.PARTS, "items/crystal_iron", new Item.Properties());
	public static final DeferredItem<Item> crystal_gold = simple("crystal_gold", NtmTab.PARTS, "items/crystal_gold", new Item.Properties());
	public static final DeferredItem<Item> crystal_redstone = simple("crystal_redstone", NtmTab.PARTS, "items/crystal_redstone", new Item.Properties());
	public static final DeferredItem<Item> crystal_lapis = simple("crystal_lapis", NtmTab.PARTS, "items/crystal_lapis", new Item.Properties());
	public static final DeferredItem<Item> crystal_diamond = simple("crystal_diamond", NtmTab.PARTS, "items/crystal_diamond", new Item.Properties());
	public static final DeferredItem<Item> crystal_uranium = simple("crystal_uranium", NtmTab.PARTS, "items/crystal_uranium", new Item.Properties());
	public static final DeferredItem<Item> crystal_thorium = simple("crystal_thorium", NtmTab.PARTS, "items/crystal_thorium", new Item.Properties());
	public static final DeferredItem<Item> crystal_plutonium = simple("crystal_plutonium", NtmTab.PARTS, "items/crystal_plutonium", new Item.Properties());
	public static final DeferredItem<Item> crystal_titanium = simple("crystal_titanium", NtmTab.PARTS, "items/crystal_titanium", new Item.Properties());
	public static final DeferredItem<Item> crystal_sulfur = simple("crystal_sulfur", NtmTab.PARTS, "items/crystal_sulfur", new Item.Properties());
	public static final DeferredItem<Item> crystal_niter = simple("crystal_niter", NtmTab.PARTS, "items/crystal_niter", new Item.Properties());
	public static final DeferredItem<Item> crystal_copper = simple("crystal_copper", NtmTab.PARTS, "items/crystal_copper", new Item.Properties());
	public static final DeferredItem<Item> crystal_tungsten = simple("crystal_tungsten", NtmTab.PARTS, "items/crystal_tungsten", new Item.Properties());
	public static final DeferredItem<Item> crystal_aluminium = simple("crystal_aluminium", NtmTab.PARTS, "items/crystal_aluminium", new Item.Properties());
	public static final DeferredItem<Item> crystal_fluorite = simple("crystal_fluorite", NtmTab.PARTS, "items/crystal_fluorite", new Item.Properties());
	public static final DeferredItem<Item> crystal_beryllium = simple("crystal_beryllium", NtmTab.PARTS, "items/crystal_beryllium", new Item.Properties());
	public static final DeferredItem<Item> crystal_lead = simple("crystal_lead", NtmTab.PARTS, "items/crystal_lead", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> crystal_schraranium = lore("crystal_schraranium", "crystal_schraranium", NtmTab.PARTS, "items/crystal_schraranium", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<ItemCustomLore> crystal_schrabidium = lore("crystal_schrabidium", "crystal_schrabidium", NtmTab.PARTS, "items/crystal_schrabidium", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<Item> crystal_rare = simple("crystal_rare", NtmTab.PARTS, "items/crystal_rare", new Item.Properties());
	public static final DeferredItem<Item> crystal_phosphorus = simple("crystal_phosphorus", NtmTab.PARTS, "items/crystal_phosphorus", new Item.Properties());
	public static final DeferredItem<Item> crystal_lithium = simple("crystal_lithium", NtmTab.PARTS, "items/crystal_lithium", new Item.Properties());
	public static final DeferredItem<Item> crystal_cobalt = simple("crystal_cobalt", NtmTab.PARTS, "items/crystal_cobalt", new Item.Properties());
	public static final DeferredItem<Item> crystal_starmetal = simple("crystal_starmetal", NtmTab.PARTS, "items/crystal_starmetal", new Item.Properties());
	public static final DeferredItem<Item> crystal_cinnebar = simple("crystal_cinnebar", NtmTab.PARTS, "items/crystal_cinnebar", new Item.Properties());
	public static final DeferredItem<Item> crystal_trixite = simple("crystal_trixite", NtmTab.PARTS, "items/crystal_trixite", new Item.Properties());
	public static final DeferredItem<Item> crystal_osmiridium = simple("crystal_osmiridium", NtmTab.PARTS, "items/crystal_osmiridium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> gem_sodalite = lore("gem_sodalite", "gem_sodalite", NtmTab.PARTS, "items/gem_sodalite", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> gem_tantalium = lore("gem_tantalium", "gem_tantalium", NtmTab.PARTS, "items/gem_tantalium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> gem_volcanic = lore("gem_volcanic", "gem_volcanic", NtmTab.PARTS, "items/gem_volcanic", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> gem_rad = lore("gem_rad", "gem_rad", NtmTab.PARTS, "items/gem_rad", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> fragment_neodymium = simple("fragment_neodymium", NtmTab.PARTS, "items/fragment_neodymium", new Item.Properties());
	public static final DeferredItem<Item> fragment_cobalt = simple("fragment_cobalt", NtmTab.PARTS, "items/fragment_cobalt", new Item.Properties());
	public static final DeferredItem<Item> fragment_niobium = simple("fragment_niobium", NtmTab.PARTS, "items/fragment_niobium", new Item.Properties());
	public static final DeferredItem<Item> fragment_cerium = simple("fragment_cerium", NtmTab.PARTS, "items/fragment_cerium", new Item.Properties());
	public static final DeferredItem<Item> fragment_lanthanium = simple("fragment_lanthanium", NtmTab.PARTS, "items/fragment_lanthanium", new Item.Properties());
	public static final DeferredItem<Item> fragment_actinium = simple("fragment_actinium", NtmTab.PARTS, "items/fragment_actinium", new Item.Properties());
	public static final DeferredItem<Item> fragment_boron = simple("fragment_boron", NtmTab.PARTS, "items/fragment_boron", new Item.Properties());
	public static final DeferredItem<Item> fragment_meteorite = simple("fragment_meteorite", NtmTab.PARTS, "items/fragment_meteorite", new Item.Properties());
	public static final DeferredItem<Item> fragment_coltan = simple("fragment_coltan", NtmTab.PARTS, "items/fragment_coltan", new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumChunkType> chunk_ore = multi("chunk_ore", "chunk_ore", EnumChunkType.class, true, true, NtmTab.PARTS, new Item.Properties());
	public static final DeferredItem<Item> biomass = simple("biomass", NtmTab.PARTS, "items/biomass", new Item.Properties());
	public static final DeferredItem<Item> biomass_compressed = simple("biomass_compressed", NtmTab.PARTS, "items/biomass_compressed", new Item.Properties());
	public static final DeferredItem<Item> nugget_uranium = simple("nugget_uranium", NtmTab.PARTS, "items/nugget_uranium", new Item.Properties());
	public static final DeferredItem<Item> nugget_u233 = simple("nugget_u233", NtmTab.PARTS, "items/nugget_u233", new Item.Properties());
	public static final DeferredItem<Item> nugget_u235 = simple("nugget_u235", NtmTab.PARTS, "items/nugget_u235", new Item.Properties());
	public static final DeferredItem<Item> nugget_u238 = simple("nugget_u238", NtmTab.PARTS, "items/nugget_u238", new Item.Properties());
	public static final DeferredItem<Item> nugget_th232 = simple("nugget_th232", NtmTab.PARTS, "items/nugget_th232", new Item.Properties());
	public static final DeferredItem<Item> nugget_plutonium = simple("nugget_plutonium", NtmTab.PARTS, "items/nugget_plutonium", new Item.Properties());
	public static final DeferredItem<Item> nugget_pu238 = simple("nugget_pu238", NtmTab.PARTS, "items/nugget_pu238", new Item.Properties());
	public static final DeferredItem<Item> nugget_pu239 = simple("nugget_pu239", NtmTab.PARTS, "items/nugget_pu239", new Item.Properties());
	public static final DeferredItem<Item> nugget_pu240 = simple("nugget_pu240", NtmTab.PARTS, "items/nugget_pu240", new Item.Properties());
	public static final DeferredItem<Item> nugget_pu241 = simple("nugget_pu241", NtmTab.PARTS, "items/nugget_pu241", new Item.Properties());
	public static final DeferredItem<Item> nugget_pu_mix = simple("nugget_pu_mix", NtmTab.PARTS, "items/nugget_pu_mix", new Item.Properties());
	public static final DeferredItem<Item> nugget_am241 = simple("nugget_am241", NtmTab.PARTS, "items/nugget_am241", new Item.Properties());
	public static final DeferredItem<Item> nugget_am242 = simple("nugget_am242", NtmTab.PARTS, "items/nugget_am242", new Item.Properties());
	public static final DeferredItem<Item> nugget_am_mix = simple("nugget_am_mix", NtmTab.PARTS, "items/nugget_am_mix", new Item.Properties());
	public static final DeferredItem<Item> nugget_neptunium = simple("nugget_neptunium", NtmTab.PARTS, "items/nugget_neptunium", new Item.Properties());
	public static final DeferredItem<Item> nugget_polonium = simple("nugget_polonium", NtmTab.PARTS, "items/nugget_polonium", new Item.Properties());
	public static final DeferredItem<Item> nugget_cobalt = simple("nugget_cobalt", NtmTab.PARTS, "items/nugget_cobalt", new Item.Properties());
	public static final DeferredItem<Item> nugget_co60 = simple("nugget_co60", NtmTab.PARTS, "items/nugget_co60", new Item.Properties());
	public static final DeferredItem<Item> nugget_sr90 = simple("nugget_sr90", NtmTab.PARTS, "items/nugget_sr90", new Item.Properties());
	public static final DeferredItem<Item> nugget_technetium = simple("nugget_technetium", NtmTab.PARTS, "items/nugget_technetium", new Item.Properties());
	public static final DeferredItem<Item> nugget_au198 = simple("nugget_au198", NtmTab.PARTS, "items/nugget_au198", new Item.Properties());
	public static final DeferredItem<Item> nugget_pb209 = simple("nugget_pb209", NtmTab.PARTS, "items/nugget_pb209", new Item.Properties());
	public static final DeferredItem<Item> nugget_ra226 = simple("nugget_ra226", NtmTab.PARTS, "items/nugget_ra226", new Item.Properties());
	public static final DeferredItem<Item> nugget_actinium = simple("nugget_actinium", NtmTab.PARTS, "items/nugget_actinium", new Item.Properties());
	public static final DeferredItem<Item> nugget_lead = simple("nugget_lead", NtmTab.PARTS, "items/nugget_lead", new Item.Properties());
	public static final DeferredItem<Item> nugget_bismuth = simple("nugget_bismuth", NtmTab.PARTS, "items/nugget_bismuth", new Item.Properties());
	public static final DeferredItem<Item> nugget_arsenic = simple("nugget_arsenic", NtmTab.PARTS, "items/nugget_arsenic", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> nugget_tantalium = lore("nugget_tantalium", "nugget_tantalium", NtmTab.PARTS, "items/nugget_tantalium", new Item.Properties());
	public static final DeferredItem<Item> nugget_silicon = simple("nugget_silicon", NtmTab.PARTS, "items/nugget_silicon", new Item.Properties());
	public static final DeferredItem<Item> nugget_niobium = simple("nugget_niobium", NtmTab.PARTS, "items/nugget_niobium", new Item.Properties());
	public static final DeferredItem<Item> nugget_beryllium = simple("nugget_beryllium", NtmTab.PARTS, "items/nugget_beryllium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> nugget_schrabidium = lore("nugget_schrabidium", "nugget_schrabidium", NtmTab.PARTS, "items/nugget_schrabidium", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<Item> nugget_solinium = simple("nugget_solinium", NtmTab.PARTS, "items/nugget_solinium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> nugget_gh336 = lore("nugget_gh336", "nugget_gh336", NtmTab.PARTS, "items/nugget_gh336", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<Item> nugget_uranium_fuel = simple("nugget_uranium_fuel", NtmTab.PARTS, "items/nugget_uranium_fuel", new Item.Properties());
	public static final DeferredItem<Item> nugget_thorium_fuel = simple("nugget_thorium_fuel", NtmTab.PARTS, "items/nugget_thorium_fuel", new Item.Properties());
	public static final DeferredItem<Item> nugget_plutonium_fuel = simple("nugget_plutonium_fuel", NtmTab.PARTS, "items/nugget_plutonium_fuel", new Item.Properties());
	public static final DeferredItem<Item> nugget_neptunium_fuel = simple("nugget_neptunium_fuel", NtmTab.PARTS, "items/nugget_neptunium_fuel", new Item.Properties());
	public static final DeferredItem<Item> nugget_mox_fuel = simple("nugget_mox_fuel", NtmTab.PARTS, "items/nugget_mox_fuel", new Item.Properties());
	public static final DeferredItem<Item> nugget_americium_fuel = simple("nugget_americium_fuel", NtmTab.PARTS, "items/nugget_americium_fuel", new Item.Properties());
	public static final DeferredItem<Item> nugget_schrabidium_fuel = simple("nugget_schrabidium_fuel", NtmTab.PARTS, "items/nugget_schrabidium_fuel", new Item.Properties());
	public static final DeferredItem<Item> nugget_hes = simple("nugget_hes", NtmTab.PARTS, "items/nugget_hes", new Item.Properties());
	public static final DeferredItem<Item> nugget_les = simple("nugget_les", NtmTab.PARTS, "items/nugget_les", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> nugget_zirconium = lore("nugget_zirconium", "nugget_zirconium", NtmTab.PARTS, "items/nugget_zirconium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> nugget_australium = lore("nugget_australium", "nugget_australium", NtmTab.PARTS, "items/nugget_australium", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> nugget_australium_lesser = lore("nugget_australium_lesser", "nugget_australium_lesser", NtmTab.PARTS, "items/nugget_australium_lesser", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> nugget_australium_greater = lore("nugget_australium_greater", "nugget_australium_greater", NtmTab.PARTS, "items/nugget_australium_greater", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> nugget_desh = lore("nugget_desh", "nugget_desh", NtmTab.PARTS, "items/nugget_desh", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> nugget_euphemium = lore("nugget_euphemium", "nugget_euphemium", NtmTab.PARTS, "items/nugget_euphemium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> nugget_dineutronium = lore("nugget_dineutronium", "nugget_dineutronium", NtmTab.PARTS, "items/nugget_dineutronium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> nugget_osmiridium = lore("nugget_osmiridium", "nugget_osmiridium", NtmTab.PARTS, "items/nugget_osmiridium", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<Item> plate_iron = simple("plate_iron", NtmTab.PARTS, "items/plate_iron", new Item.Properties());
	public static final DeferredItem<Item> plate_gold = simple("plate_gold", NtmTab.PARTS, "items/plate_gold", new Item.Properties());
	public static final DeferredItem<Item> plate_titanium = simple("plate_titanium", NtmTab.PARTS, "items/plate_titanium", new Item.Properties());
	public static final DeferredItem<Item> plate_aluminium = simple("plate_aluminium", NtmTab.PARTS, "items/plate_aluminium", new Item.Properties());
	public static final DeferredItem<Item> plate_steel = simple("plate_steel", NtmTab.PARTS, "items/plate_steel", new Item.Properties());
	public static final DeferredItem<Item> plate_lead = simple("plate_lead", NtmTab.PARTS, "items/plate_lead", new Item.Properties());
	public static final DeferredItem<Item> plate_copper = simple("plate_copper", NtmTab.PARTS, "items/plate_copper", new Item.Properties());
	public static final DeferredItem<Item> plate_dura_steel = simple("plate_dura_steel", NtmTab.PARTS, "items/plate_dura_steel", new Item.Properties());
	public static final DeferredItem<Item> neutron_reflector = simple("neutron_reflector", NtmTab.PARTS, "items/neutron_reflector", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> plate_schrabidium = lore("plate_schrabidium", "plate_schrabidium", NtmTab.PARTS, "items/plate_schrabidium", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<Item> plate_combine_steel = simple("plate_combine_steel", NtmTab.PARTS, "items/plate_combine_steel", new Item.Properties());
	public static final DeferredItem<Item> plate_mixed = simple("plate_mixed", NtmTab.PARTS, "items/plate_mixed", new Item.Properties());
	public static final DeferredItem<Item> plate_gunmetal = simple("plate_gunmetal", NtmTab.PARTS, "items/plate_gunmetal", new Item.Properties());
	public static final DeferredItem<Item> plate_weaponsteel = simple("plate_weaponsteel", NtmTab.PARTS, "items/plate_gunsteel", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> plate_saturnite = lore("plate_saturnite", "plate_saturnite", NtmTab.PARTS, "items/plate_saturnite", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<ItemCustomLore> plate_paa = lore("plate_paa", "plate_paa", NtmTab.PARTS, "items/plate_paa", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> plate_polymer = simple("plate_polymer", NtmTab.PARTS, "items/plate_polymer", new Item.Properties());
	public static final DeferredItem<Item> plate_kevlar = simple("plate_kevlar", NtmTab.PARTS, "items/plate_kevlar", new Item.Properties());
	public static final DeferredItem<Item> plate_dalekanium = simple("plate_dalekanium", NtmTab.PARTS, "items/plate_dalekanium", new Item.Properties());
	public static final DeferredItem<Item> plate_desh = simple("plate_desh", NtmTab.PARTS, "items/plate_desh", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> plate_bismuth = lore("plate_bismuth", "plate_bismuth", NtmTab.PARTS, "items/plate_bismuth", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> plate_euphemium = lore("plate_euphemium", "plate_euphemium", NtmTab.PARTS, "items/plate_euphemium", new Item.Properties().rarity(Rarity.EPIC));
	public static final DeferredItem<Item> plate_dineutronium = simple("plate_dineutronium", NtmTab.PARTS, "items/plate_dineutronium", new Item.Properties());
	public static final DeferredItem<Item> plate_armor_titanium = simple("plate_armor_titanium", NtmTab.PARTS, "items/plate_armor_titanium", new Item.Properties());
	public static final DeferredItem<Item> plate_armor_ajr = simple("plate_armor_ajr", NtmTab.PARTS, "items/plate_armor_ajr", new Item.Properties());
	public static final DeferredItem<Item> plate_armor_hev = simple("plate_armor_hev", NtmTab.PARTS, "items/plate_armor_hev", new Item.Properties());
	public static final DeferredItem<Item> plate_armor_lunar = simple("plate_armor_lunar", NtmTab.PARTS, "items/plate_armor_lunar", new Item.Properties());
	public static final DeferredItem<Item> plate_armor_fau = simple("plate_armor_fau", NtmTab.PARTS, "items/plate_armor_fau", new Item.Properties());
	public static final DeferredItem<Item> plate_armor_dnt = simple("plate_armor_dnt", NtmTab.PARTS, "items/plate_armor_dnt", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> bolt_spike = lore("bolt_spike", "bolt_spike", NtmTab.PARTS, "items/bolt_spike", new Item.Properties());
	public static final DeferredItem<Item> hazmat_cloth = simple("hazmat_cloth", NtmTab.PARTS, "items/hazmat_cloth", new Item.Properties());
	public static final DeferredItem<Item> hazmat_cloth_red = simple("hazmat_cloth_red", NtmTab.PARTS, "items/hazmat_cloth_red", new Item.Properties());
	public static final DeferredItem<Item> hazmat_cloth_grey = simple("hazmat_cloth_grey", NtmTab.PARTS, "items/hazmat_cloth_grey", new Item.Properties());
	public static final DeferredItem<Item> asbestos_cloth = simple("asbestos_cloth", NtmTab.PARTS, "items/asbestos_cloth", new Item.Properties());
	public static final DeferredItem<Item> rag_damp = simple("rag_damp", NtmTab.PARTS, "items/rag_damp", new Item.Properties());
	public static final DeferredItem<Item> rag_piss = simple("rag_piss", NtmTab.PARTS, "items/rag_piss", new Item.Properties());
	public static final DeferredItem<Item> filter_coal = simple("filter_coal", NtmTab.PARTS, "items/filter_coal", new Item.Properties());
	public static final DeferredItem<Item> coil_copper = simple("coil_copper", NtmTab.PARTS, "items/coil_copper", new Item.Properties());
	public static final DeferredItem<Item> coil_copper_torus = simple("coil_copper_torus", NtmTab.PARTS, "items/coil_copper_torus", new Item.Properties());
	public static final DeferredItem<Item> coil_gold = simple("coil_gold", NtmTab.PARTS, "items/coil_gold", new Item.Properties());
	public static final DeferredItem<Item> coil_gold_torus = simple("coil_gold_torus", NtmTab.PARTS, "items/coil_gold_torus", new Item.Properties());
	public static final DeferredItem<Item> coil_tungsten = simple("coil_tungsten", NtmTab.PARTS, "items/coil_tungsten", new Item.Properties());
	public static final DeferredItem<Item> coil_magnetized_tungsten = simple("coil_magnetized_tungsten", NtmTab.PARTS, "items/coil_magnetized_tungsten", new Item.Properties());
	public static final DeferredItem<Item> safety_fuse = simple("safety_fuse", NtmTab.PARTS, "items/safety_fuse", new Item.Properties());
	public static final DeferredItem<Item> tank_steel = simple("tank_steel", NtmTab.PARTS, "items/tank_steel", new Item.Properties());
	public static final DeferredItem<Item> motor = simple("motor", NtmTab.PARTS, "items/motor", new Item.Properties());
	public static final DeferredItem<Item> motor_desh = simple("motor_desh", NtmTab.PARTS, "items/motor_desh", new Item.Properties());
	public static final DeferredItem<Item> motor_bismuth = simple("motor_bismuth", NtmTab.PARTS, "items/motor_bismuth", new Item.Properties());
	public static final DeferredItem<Item> centrifuge_element = simple("centrifuge_element", NtmTab.PARTS, "items/centrifuge_element", new Item.Properties());
	public static final DeferredItem<Item> reactor_core = simple("reactor_core", NtmTab.PARTS, "items/reactor_core", new Item.Properties());
	public static final DeferredItem<Item> rtg_unit = simple("rtg_unit", NtmTab.PARTS, "items/rtg_unit", new Item.Properties());
	public static final DeferredItem<Item> pipes_steel = simple("pipes_steel", NtmTab.PARTS, "items/pipes_steel", new Item.Properties());
	public static final DeferredItem<Item> drill_titanium = simple("drill_titanium", NtmTab.PARTS, "items/drill_titanium", new Item.Properties());
	public static final DeferredItem<Item> photo_panel = simple("photo_panel", NtmTab.PARTS, "items/photo_panel", new Item.Properties());
	public static final DeferredItem<Item> ring_starmetal = simple("ring_starmetal", NtmTab.PARTS, "items/ring_starmetal", new Item.Properties());
	public static final DeferredItem<Item> deuterium_filter = simple("deuterium_filter", NtmTab.PARTS, "items/deuterium_filter", new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumSecretType> item_secret = multi("item_secret", "item_secret", EnumSecretType.class, true, true, null, new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumIngotMetal> ingot_metal = multi("ingot_metal", "ingot_metal", EnumIngotMetal.class, true, true, null, new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumLegendaryType> parts_legendary = multi("parts_legendary", "parts_legendary", EnumLegendaryType.class, false, true, NtmTab.PARTS, new Item.Properties());
	public static final DeferredItem<Item> sawblade = simple("sawblade", NtmTab.PARTS, "items/sawblade", new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumPlantType> plant_item = multi("plant_item", "plant_item", EnumPlantType.class, true, true, NtmTab.PARTS, new Item.Properties());
	public static final DeferredItem<ItemCustomLore> entanglement_kit = lore("entanglement_kit", "entanglement_kit", NtmTab.PARTS, "items/entanglement_kit", new Item.Properties());
	public static final DeferredItem<Item> fins_flat = simple("fins_flat", NtmTab.PARTS, "items/fins_flat", new Item.Properties());
	public static final DeferredItem<Item> fins_small_steel = simple("fins_small_steel", NtmTab.PARTS, "items/fins_small_steel", new Item.Properties());
	public static final DeferredItem<Item> fins_big_steel = simple("fins_big_steel", NtmTab.PARTS, "items/fins_big_steel", new Item.Properties());
	public static final DeferredItem<Item> fins_tri_steel = simple("fins_tri_steel", NtmTab.PARTS, "items/fins_tri_steel", new Item.Properties());
	public static final DeferredItem<Item> fins_quad_titanium = simple("fins_quad_titanium", NtmTab.PARTS, "items/fins_quad_titanium", new Item.Properties());
	public static final DeferredItem<Item> sphere_steel = simple("sphere_steel", NtmTab.PARTS, "items/sphere_steel", new Item.Properties());
	public static final DeferredItem<Item> pedestal_steel = simple("pedestal_steel", NtmTab.PARTS, "items/pedestal_steel", new Item.Properties());
	public static final DeferredItem<Item> dysfunctional_reactor = simple("dysfunctional_reactor", NtmTab.PARTS, "items/dysfunctional_reactor", new Item.Properties());
	public static final DeferredItem<Item> blade_titanium = simple("blade_titanium", NtmTab.PARTS, "items/blade_titanium", new Item.Properties());
	public static final DeferredItem<Item> blade_tungsten = simple("blade_tungsten", NtmTab.PARTS, "items/blade_tungsten", new Item.Properties());
	public static final DeferredItem<Item> turbine_titanium = simple("turbine_titanium", NtmTab.PARTS, "items/turbine_titanium", new Item.Properties());
	public static final DeferredItem<Item> turbine_tungsten = simple("turbine_tungsten", NtmTab.PARTS, "items/turbine_tungsten", new Item.Properties());
	public static final DeferredItem<Item> flywheel_beryllium = simple("flywheel_beryllium", NtmTab.PARTS, "items/flywheel_beryllium", new Item.Properties());
	public static final DeferredItem<Item> ducttape = simple("ducttape", NtmTab.PARTS, "items/ducttape", new Item.Properties());
	public static final DeferredItem<Item> catalyst_clay = simple("catalyst_clay", NtmTab.PARTS, "items/catalyst_clay", new Item.Properties());
	public static final DeferredItem<Item> missile_assembly = simple("missile_assembly", NtmTab.PARTS, "items/missile_assembly", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> warhead_generic_small = simple("warhead_generic_small", NtmTab.PARTS, "items/warhead_generic_small", new Item.Properties());
	public static final DeferredItem<Item> warhead_generic_medium = simple("warhead_generic_medium", NtmTab.PARTS, "items/warhead_generic_medium", new Item.Properties());
	public static final DeferredItem<Item> warhead_generic_large = simple("warhead_generic_large", NtmTab.PARTS, "items/warhead_generic_large", new Item.Properties());
	public static final DeferredItem<Item> warhead_incendiary_small = simple("warhead_incendiary_small", NtmTab.PARTS, "items/warhead_incendiary_small", new Item.Properties());
	public static final DeferredItem<Item> warhead_incendiary_medium = simple("warhead_incendiary_medium", NtmTab.PARTS, "items/warhead_incendiary_medium", new Item.Properties());
	public static final DeferredItem<Item> warhead_incendiary_large = simple("warhead_incendiary_large", NtmTab.PARTS, "items/warhead_incendiary_large", new Item.Properties());
	public static final DeferredItem<Item> warhead_cluster_small = simple("warhead_cluster_small", NtmTab.PARTS, "items/warhead_cluster_small", new Item.Properties());
	public static final DeferredItem<Item> warhead_cluster_medium = simple("warhead_cluster_medium", NtmTab.PARTS, "items/warhead_cluster_medium", new Item.Properties());
	public static final DeferredItem<Item> warhead_cluster_large = simple("warhead_cluster_large", NtmTab.PARTS, "items/warhead_cluster_large", new Item.Properties());
	public static final DeferredItem<Item> warhead_buster_small = simple("warhead_buster_small", NtmTab.PARTS, "items/warhead_buster_small", new Item.Properties());
	public static final DeferredItem<Item> warhead_buster_medium = simple("warhead_buster_medium", NtmTab.PARTS, "items/warhead_buster_medium", new Item.Properties());
	public static final DeferredItem<Item> warhead_buster_large = simple("warhead_buster_large", NtmTab.PARTS, "items/warhead_buster_large", new Item.Properties());
	public static final DeferredItem<Item> warhead_nuclear = simple("warhead_nuclear", NtmTab.PARTS, "items/warhead_nuclear", new Item.Properties());
	public static final DeferredItem<Item> warhead_mirv = simple("warhead_mirv", NtmTab.PARTS, "items/warhead_mirv", new Item.Properties());
	public static final DeferredItem<Item> warhead_volcano = simple("warhead_volcano", NtmTab.PARTS, "items/warhead_volcano", new Item.Properties());
	public static final DeferredItem<Item> fuel_tank_small = simple("fuel_tank_small", NtmTab.PARTS, "items/fuel_tank_small", new Item.Properties());
	public static final DeferredItem<Item> fuel_tank_medium = simple("fuel_tank_medium", NtmTab.PARTS, "items/fuel_tank_medium", new Item.Properties());
	public static final DeferredItem<Item> fuel_tank_large = simple("fuel_tank_large", NtmTab.PARTS, "items/fuel_tank_large", new Item.Properties());
	public static final DeferredItem<Item> thruster_small = simple("thruster_small", NtmTab.PARTS, "items/thruster_small", new Item.Properties());
	public static final DeferredItem<Item> thruster_medium = simple("thruster_medium", NtmTab.PARTS, "items/thruster_medium", new Item.Properties());
	public static final DeferredItem<Item> thruster_large = simple("thruster_large", NtmTab.PARTS, "items/thruster_large", new Item.Properties());
	public static final DeferredItem<Item> thruster_nuclear = simple("thruster_nuclear", NtmTab.PARTS, "items/thruster_nuclear", new Item.Properties());
	public static final DeferredItem<Item> seg_10 = simple("seg_10", NtmTab.PARTS, "items/seg_10", new Item.Properties());
	public static final DeferredItem<Item> seg_15 = simple("seg_15", NtmTab.PARTS, "items/seg_15", new Item.Properties());
	public static final DeferredItem<Item> seg_20 = simple("seg_20", NtmTab.PARTS, "items/seg_20", new Item.Properties());
	public static final DeferredItem<Item> combine_scrap = simple("combine_scrap", NtmTab.PARTS, "items/combine_scrap", new Item.Properties());
	public static final DeferredItem<Item> shimmer_head = simple("shimmer_head", NtmTab.PARTS, "items/shimmer_head_original", new Item.Properties());
	public static final DeferredItem<Item> shimmer_axe_head = simple("shimmer_axe_head", NtmTab.PARTS, "items/shimmer_axe_head", new Item.Properties());
	public static final DeferredItem<Item> shimmer_handle = simple("shimmer_handle", NtmTab.PARTS, "items/shimmer_handle", new Item.Properties());
	public static final DeferredItem<Item> crt_display = simple("crt_display", NtmTab.PARTS, "items/crt_display", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> circuit_star = lore("circuit_star", "circuit_star", null, "items/circuit_star", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final ItemEnumMulti.Variants<EnumCasingType> casing = multi("casing", "casing", EnumCasingType.class, true, true, NtmTab.PARTS, new Item.Properties());
	public static final DeferredItem<Item> assembly_nuke = simple("assembly_nuke", NtmTab.PARTS, "items/assembly_nuke", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> flame_pony = lore("flame_pony", "flame_pony", NtmTab.PARTS, "items/flame_pony", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> flame_conspiracy = lore("flame_conspiracy", "flame_conspiracy", NtmTab.PARTS, "items/flame_conspiracy", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> flame_politics = lore("flame_politics", "flame_politics", NtmTab.PARTS, "items/flame_politics", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> flame_opinion = lore("flame_opinion", "flame_opinion", NtmTab.PARTS, "items/flame_opinion", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> pellet_cluster = lore("pellet_cluster", "pellet_cluster", NtmTab.PARTS, "items/pellet_cluster", new Item.Properties());
	public static final DeferredItem<Item> pellet_buckshot = simple("pellet_buckshot", NtmTab.PARTS, "items/pellets_lead", new Item.Properties());
	public static final DeferredItem<Item> pellet_charged = simple("pellet_charged", NtmTab.PARTS, "items/pellets_charged", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> pellet_gas = lore("pellet_gas", "pellet_gas", NtmTab.PARTS, "items/pellet_gas", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> magnetron = lore("magnetron", "magnetron", NtmTab.PARTS, "items/magnetron_alt", new Item.Properties());
	public static final DeferredItem<Item> piston_selenium = simple("piston_selenium", NtmTab.CONTROL, "items/piston_selenium", new Item.Properties());
	public static final DeferredItem<Item> cell_empty = simple("cell_empty", NtmTab.CONTROL, "items/cell_empty", new Item.Properties());
	public static final DeferredItem<Item> cell_uf6 = simple("cell_uf6", NtmTab.CONTROL, "items/cell_uf6", new Item.Properties());
	public static final DeferredItem<Item> cell_puf6 = simple("cell_puf6", NtmTab.CONTROL, "items/cell_puf6", new Item.Properties());
	public static final DeferredItem<Item> cell_deuterium = simple("cell_deuterium", NtmTab.CONTROL, "items/cell_deuterium", new Item.Properties());
	public static final DeferredItem<Item> cell_tritium = simple("cell_tritium", NtmTab.CONTROL, "items/cell_tritium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> cell_sas3 = lore("cell_sas3", "cell_sas3", NtmTab.CONTROL, "items/cell_sas3", new Item.Properties().rarity(Rarity.RARE));
	public static final DeferredItem<Item> cell_balefire = simple("cell_balefire", NtmTab.CONTROL, "items/cell_balefire", new Item.Properties());
	public static final DeferredItem<Item> demon_core_closed = simple("demon_core_closed", NtmTab.NUKE, "items/demon_core_closed", new Item.Properties());
	public static final DeferredItem<Item> particle_empty = simple("particle_empty", NtmTab.CONTROL, "items/particle_empty", new Item.Properties());
	public static final DeferredItem<Item> particle_hydrogen = simple("particle_hydrogen", NtmTab.CONTROL, "items/particle_hydrogen", new Item.Properties());
	public static final DeferredItem<Item> particle_copper = simple("particle_copper", NtmTab.CONTROL, "items/particle_copper", new Item.Properties());
	public static final DeferredItem<Item> particle_lead = simple("particle_lead", NtmTab.CONTROL, "items/particle_lead", new Item.Properties());
	public static final DeferredItem<Item> particle_amat = simple("particle_amat", NtmTab.CONTROL, "items/particle_amat", new Item.Properties());
	public static final DeferredItem<Item> particle_aschrab = simple("particle_aschrab", NtmTab.CONTROL, "items/particle_aschrab", new Item.Properties());
	public static final DeferredItem<Item> particle_higgs = simple("particle_higgs", NtmTab.CONTROL, "items/particle_higgs", new Item.Properties());
	public static final DeferredItem<Item> particle_muon = simple("particle_muon", NtmTab.CONTROL, "items/particle_muon", new Item.Properties());
	public static final DeferredItem<Item> particle_tachyon = simple("particle_tachyon", NtmTab.CONTROL, "items/particle_tachyon", new Item.Properties());
	public static final DeferredItem<Item> particle_strange = simple("particle_strange", NtmTab.CONTROL, "items/particle_strange", new Item.Properties());
	public static final DeferredItem<Item> particle_dark = simple("particle_dark", NtmTab.CONTROL, "items/particle_dark", new Item.Properties());
	public static final DeferredItem<Item> particle_sparkticle = simple("particle_sparkticle", NtmTab.CONTROL, "items/particle_sparkticle", new Item.Properties());
	public static final DeferredItem<Item> particle_lutece = simple("particle_lutece", NtmTab.CONTROL, "items/particle_lutece", new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumFuelAdditive> fuel_additive = multi("fuel_additive", "fuel_additive", EnumFuelAdditive.class, true, true, NtmTab.CONTROL, new Item.Properties());
	public static final DeferredItem<ItemCustomLore> canister_empty = lore("canister_empty", "canister_empty", NtmTab.CONTROL, "items/canister_empty", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> canister_napalm = lore("canister_napalm", "canister_napalm", NtmTab.CONTROL, "items/canister_napalm", new Item.Properties());
	public static final DeferredItem<Item> gas_empty = simple("gas_empty", NtmTab.CONTROL, "items/gas_empty", new Item.Properties());
	public static final DeferredItem<Item> fluid_tank_empty = simple("fluid_tank_empty", NtmTab.CONTROL, "items/fluid_tank", new Item.Properties());
	public static final DeferredItem<Item> fluid_tank_lead_empty = simple("fluid_tank_lead_empty", NtmTab.CONTROL, "items/fluid_tank_lead", new Item.Properties());
	public static final DeferredItem<Item> fluid_barrel_empty = simple("fluid_barrel_empty", NtmTab.CONTROL, "items/fluid_barrel", new Item.Properties());
	public static final DeferredItem<Item> fluid_pack_empty = simple("fluid_pack_empty", NtmTab.CONTROL, "items/fluid_pack", new Item.Properties());
	public static final DeferredItem<Item> battery_spark = simple("battery_spark", NtmTab.NUKE, "items/battery_spark", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> battery_trixite = simple("battery_trixite", NtmTab.NUKE, "items/battery_trixite", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> mold_base = simple("mold_base", NtmTab.CONTROL, "items/mold_base", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> upgrade_template = lore("upgrade_template", "upgrade_template", NtmTab.PARTS, "items/upgrade_template", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> fuse = lore("fuse", "fuse", NtmTab.CONTROL, "items/fuse", new Item.Properties());
	public static final DeferredItem<Item> part_lithium = simple("part_lithium", NtmTab.CONTROL, "items/part_lithium", new Item.Properties());
	public static final DeferredItem<Item> part_beryllium = simple("part_beryllium", NtmTab.CONTROL, "items/part_beryllium", new Item.Properties());
	public static final DeferredItem<Item> part_carbon = simple("part_carbon", NtmTab.CONTROL, "items/part_carbon", new Item.Properties());
	public static final DeferredItem<Item> part_copper = simple("part_copper", NtmTab.CONTROL, "items/part_copper", new Item.Properties());
	public static final DeferredItem<Item> part_plutonium = simple("part_plutonium", NtmTab.CONTROL, "items/part_plutonium", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> rune_blank = lore("rune_blank", "rune_blank", NtmTab.PARTS, "items/rune_blank", new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final DeferredItem<ItemCustomLore> rune_isa = lore("rune_isa", "rune_isa", NtmTab.PARTS, "items/rune_isa", new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final DeferredItem<ItemCustomLore> rune_dagaz = lore("rune_dagaz", "rune_dagaz", NtmTab.PARTS, "items/rune_dagaz", new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final DeferredItem<ItemCustomLore> rune_hagalaz = lore("rune_hagalaz", "rune_hagalaz", NtmTab.PARTS, "items/rune_hagalaz", new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final DeferredItem<ItemCustomLore> rune_jera = lore("rune_jera", "rune_jera", NtmTab.PARTS, "items/rune_jera", new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final DeferredItem<ItemCustomLore> rune_thurisaz = lore("rune_thurisaz", "rune_thurisaz", NtmTab.PARTS, "items/rune_thurisaz", new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final DeferredItem<Item> ams_catalyst_blank = simple("ams_catalyst_blank", NtmTab.CONTROL, "items/ams_catalyst_blank", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> thermo_element = simple("thermo_element", NtmTab.CONTROL, "items/thermo_element", new Item.Properties());
	public static final DeferredItem<Item> catalytic_converter = simple("catalytic_converter", NtmTab.CONTROL, "items/catalytic_converter", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> rod_empty = simple("rod_empty", NtmTab.CONTROL, "items/rod_empty", new Item.Properties());
	public static final DeferredItem<Item> rod_dual_empty = simple("rod_dual_empty", NtmTab.CONTROL, "items/rod_dual_empty", new Item.Properties());
	public static final DeferredItem<Item> rod_quad_empty = simple("rod_quad_empty", NtmTab.CONTROL, "items/rod_quad_empty", new Item.Properties());
	public static final DeferredItem<Item> rod_zirnox_empty = simple("rod_zirnox_empty", NtmTab.CONTROL, "items/rod_zirnox_empty", new Item.Properties().stacksTo(64));
	public static final DeferredItem<Item> rod_zirnox_tritium = simple("rod_zirnox_tritium", NtmTab.CONTROL, "items/rod_zirnox_tritium", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> rod_zirnox_natural_uranium_fuel_depleted = simple("rod_zirnox_natural_uranium_fuel_depleted", NtmTab.CONTROL, "items/rod_zirnox_uranium_fuel_depleted", new Item.Properties());
	public static final DeferredItem<Item> rod_zirnox_uranium_fuel_depleted = simple("rod_zirnox_uranium_fuel_depleted", NtmTab.CONTROL, "items/rod_zirnox_uranium_fuel_depleted", new Item.Properties());
	public static final DeferredItem<Item> rod_zirnox_thorium_fuel_depleted = simple("rod_zirnox_thorium_fuel_depleted", NtmTab.CONTROL, "items/rod_zirnox_thorium_fuel_depleted", new Item.Properties());
	public static final DeferredItem<Item> rod_zirnox_mox_fuel_depleted = simple("rod_zirnox_mox_fuel_depleted", NtmTab.CONTROL, "items/rod_zirnox_mox_fuel_depleted", new Item.Properties());
	public static final DeferredItem<Item> rod_zirnox_plutonium_fuel_depleted = simple("rod_zirnox_plutonium_fuel_depleted", NtmTab.CONTROL, "items/rod_zirnox_plutonium_fuel_depleted", new Item.Properties());
	public static final DeferredItem<Item> rod_zirnox_u233_fuel_depleted = simple("rod_zirnox_u233_fuel_depleted", NtmTab.CONTROL, "items/rod_zirnox_u233_fuel_depleted", new Item.Properties());
	public static final DeferredItem<Item> rod_zirnox_u235_fuel_depleted = simple("rod_zirnox_u235_fuel_depleted", NtmTab.CONTROL, "items/rod_zirnox_u235_fuel_depleted", new Item.Properties());
	public static final DeferredItem<Item> rod_zirnox_les_fuel_depleted = simple("rod_zirnox_les_fuel_depleted", NtmTab.CONTROL, "items/rod_zirnox_les_fuel_depleted", new Item.Properties());
	public static final DeferredItem<Item> rod_zirnox_zfb_mox_depleted = simple("rod_zirnox_zfb_mox_depleted", NtmTab.CONTROL, "items/rod_zirnox_zfb_mox_depleted", new Item.Properties());
	public static final DeferredItem<Item> rbmk_fuel_empty = simple("rbmk_fuel_empty", NtmTab.CONTROL, "items/rbmk_fuel_empty", new Item.Properties());
	public static final DeferredItem<Item> icf_pellet_empty = simple("icf_pellet_empty", NtmTab.CONTROL, "items/icf_pellet_empty", new Item.Properties());
	public static final DeferredItem<Item> icf_pellet_depleted = simple("icf_pellet_depleted", NtmTab.CONTROL, "items/icf_pellet_depleted", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> debris_graphite = simple("debris_graphite", NtmTab.CONTROL, "items/debris_graphite", new Item.Properties());
	public static final DeferredItem<Item> debris_metal = simple("debris_metal", NtmTab.CONTROL, "items/debris_metal", new Item.Properties());
	public static final DeferredItem<Item> debris_fuel = simple("debris_fuel", NtmTab.CONTROL, "items/debris_fuel", new Item.Properties());
	public static final DeferredItem<Item> debris_concrete = simple("debris_concrete", NtmTab.CONTROL, "items/debris_concrete", new Item.Properties());
	public static final DeferredItem<Item> debris_exchanger = simple("debris_exchanger", NtmTab.CONTROL, "items/debris_exchanger", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> undefined = lore("undefined", "undefined", NtmTab.PARTS, "items/undefined", new Item.Properties());
	public static final DeferredItem<Item> scrap = simple("scrap", NtmTab.PARTS, "items/scrap", new Item.Properties());
	public static final DeferredItem<Item> scrap_oil = simple("scrap_oil", NtmTab.PARTS, "items/scrap_oil", new Item.Properties());
	public static final DeferredItem<Item> scrap_nuclear = simple("scrap_nuclear", NtmTab.PARTS, "items/scrap_nuclear", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> key_red = lore("key_red", "key_red", null, "items/key_red", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> key_red_cracked = lore("key_red_cracked", "key_red_cracked", null, "items/key_red_cracked", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> mech_key = lore("mech_key", "mech_key", null, "items/mech_key", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> pin = lore("pin", "pin", NtmTab.CONSUMABLE, "items/pin", new Item.Properties().stacksTo(8));
	public static final DeferredItem<Item> launch_code_piece = simple("launch_code_piece", NtmTab.PARTS, "items/launch_code_piece", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> launch_code = simple("launch_code", NtmTab.PARTS, "items/launch_code", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> launch_key = simple("launch_key", NtmTab.PARTS, "items/launch_key", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> missile_soyuz_lander = lore("missile_soyuz_lander", "missile_soyuz_lander", NtmTab.MISSILE, "items/soyuz_lander", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> ammo_dgk = lore("ammo_dgk", "ammo_dgk", NtmTab.WEAPON, "items/ammo_dgk", new Item.Properties());
	public static final DeferredItem<Item> stick_tnt = simple("stick_tnt", NtmTab.WEAPON, "items/stick_tnt", new Item.Properties());
	public static final DeferredItem<Item> stick_semtex = simple("stick_semtex", NtmTab.WEAPON, "items/stick_semtex", new Item.Properties());
	public static final DeferredItem<Item> stick_c4 = simple("stick_c4", NtmTab.WEAPON, "items/stick_c4", new Item.Properties());
	public static final DeferredItem<Item> disperser_canister_empty = simple("disperser_canister_empty", NtmTab.WEAPON, "items/disperser_canister", new Item.Properties());
	public static final DeferredItem<Item> glyphid_gland_empty = simple("glyphid_gland_empty", NtmTab.WEAPON, "items/glyphid_gland", new Item.Properties());
	public static final DeferredItem<Item> syringe_empty = simple("syringe_empty", NtmTab.CONSUMABLE, "items/syringe_empty", new Item.Properties());
	public static final DeferredItem<Item> syringe_metal_empty = simple("syringe_metal_empty", NtmTab.CONSUMABLE, "items/syringe_metal_empty", new Item.Properties());
	public static final DeferredItem<Item> egg_glyphid = simple("egg_glyphid", NtmTab.CONSUMABLE, "items/egg_glyphid", new Item.Properties());
	public static final DeferredItem<Item> can_empty = simple("can_empty", NtmTab.CONSUMABLE, "items/can_empty", new Item.Properties());
	public static final DeferredItem<Item> bottle_empty = simple("bottle_empty", null, "items/bottle_empty", new Item.Properties());
	public static final DeferredItem<Item> bottle2_empty = simple("bottle2_empty", null, "items/bottle2_empty", new Item.Properties());
	public static final DeferredItem<Item> cap_nuka = simple("cap_nuka", NtmTab.CONSUMABLE, "items/cap_nuka", new Item.Properties());
	public static final DeferredItem<Item> cap_quantum = simple("cap_quantum", NtmTab.CONSUMABLE, "items/cap_quantum", new Item.Properties());
	public static final DeferredItem<Item> cap_sparkle = simple("cap_sparkle", NtmTab.CONSUMABLE, "items/cap_sparkle", new Item.Properties());
	public static final DeferredItem<Item> cap_rad = simple("cap_rad", NtmTab.CONSUMABLE, "items/cap_rad", new Item.Properties());
	public static final DeferredItem<Item> cap_korl = simple("cap_korl", NtmTab.CONSUMABLE, "items/cap_korl", new Item.Properties());
	public static final DeferredItem<Item> cap_fritz = simple("cap_fritz", NtmTab.CONSUMABLE, "items/cap_fritz", new Item.Properties());
	public static final DeferredItem<Item> ring_pull = simple("ring_pull", NtmTab.CONSUMABLE, "items/ring_pull", new Item.Properties());
	public static final DeferredItem<Item> can_key = simple("can_key", NtmTab.CONSUMABLE, "items/can_key", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> coin_creeper = lore("coin_creeper", "coin_creeper", NtmTab.CONSUMABLE, "items/coin_creeper", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> coin_maskman = lore("coin_maskman", "coin_maskman", NtmTab.CONSUMABLE, "items/coin_maskman", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> coin_worm = lore("coin_worm", "coin_worm", NtmTab.CONSUMABLE, "items/coin_worm", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> coin_ufo = lore("coin_ufo", "coin_ufo", NtmTab.CONSUMABLE, "items/coin_ufo", new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> coin_token = simple("coin_token", NtmTab.CONSUMABLE, "items/coin_token", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> early_explosive_lenses = lore("early_explosive_lenses", "early_explosive_lenses", NtmTab.NUKE, "items/gadget_explosive8", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> explosive_lenses = lore("explosive_lenses", "explosive_lenses", NtmTab.NUKE, "items/man_explosive8", new Item.Properties());
	public static final DeferredItem<Item> gadget_wireing = simple("gadget_wireing", NtmTab.NUKE, "items/gadget_wireing", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> gadget_core = lore("gadget_core", "gadget_core", NtmTab.NUKE, "items/gadget_core", new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> boy_shielding = simple("boy_shielding", NtmTab.NUKE, "items/boy_shielding", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> boy_target = lore("boy_target", "boy_target", NtmTab.NUKE, "items/boy_target", new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<ItemCustomLore> boy_bullet = lore("boy_bullet", "boy_bullet", NtmTab.NUKE, "items/boy_bullet", new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> boy_propellant = simple("boy_propellant", NtmTab.NUKE, "items/boy_propellant", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> boy_igniter = simple("boy_igniter", NtmTab.NUKE, "items/boy_igniter", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> man_igniter = simple("man_igniter", NtmTab.NUKE, "items/man_igniter", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> man_core = lore("man_core", "man_core", NtmTab.NUKE, "items/man_core", new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> mike_core = simple("mike_core", NtmTab.NUKE, "items/mike_core", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> mike_deut = simple("mike_deut", NtmTab.NUKE, "items/mike_deut", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> mike_cooling_unit = simple("mike_cooling_unit", NtmTab.NUKE, "items/mike_cooling_unit", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> tsar_core = simple("tsar_core", NtmTab.NUKE, "items/tsar_core", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> egg_balefire_shard = simple("egg_balefire_shard", NtmTab.NUKE, "items/egg_balefire_shard", new Item.Properties().stacksTo(16));
	public static final DeferredItem<Item> egg_balefire = simple("egg_balefire", NtmTab.NUKE, "items/egg_balefire", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> custom_tnt = lore("custom_tnt", "custom_tnt", NtmTab.NUKE, "items/custom_tnt", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> custom_nuke = lore("custom_nuke", "custom_nuke", NtmTab.NUKE, "items/custom_nuke", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> custom_hydro = lore("custom_hydro", "custom_hydro", NtmTab.NUKE, "items/custom_hydro", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> custom_amat = lore("custom_amat", "custom_amat", NtmTab.NUKE, "items/custom_amat", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> custom_dirty = lore("custom_dirty", "custom_dirty", NtmTab.NUKE, "items/custom_dirty", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> custom_schrab = lore("custom_schrab", "custom_schrab", NtmTab.NUKE, "items/custom_schrab", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> custom_fall = lore("custom_fall", "custom_fall", NtmTab.NUKE, "items/custom_fall", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> igniter = lore("igniter", "igniter", NtmTab.NUKE, "items/trigger", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> reacher = simple("reacher", NtmTab.CONTROL, "items/reacher", new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> watch = lore("watch", "watch", null, "items/watch", new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
	public static final DeferredItem<ItemCustomLore> crystal_horn = lore("crystal_horn", "crystal_horn", NtmTab.PARTS, "items/crystal_horn", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> crystal_charred = lore("crystal_charred", "crystal_charred", NtmTab.PARTS, "items/crystal_charred", new Item.Properties());
	public static final DeferredItem<ItemCustomLore> book_secret = lore("book_secret", "book_secret", null, "items/book_secret", new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumPages> page_of_ = multi("page_of_", "page_of_", EnumPages.class, true, false, null, new Item.Properties().stacksTo(1));
	public static final DeferredItem<ItemCustomLore> burnt_bark = lore("burnt_bark", "burnt_bark", null, "items/burnt_bark", new Item.Properties());
	public static final DeferredItem<Item> holotape_damaged = simple("holotape_damaged", null, "items/holotape_damaged", new Item.Properties());
	public static final DeferredItem<Item> chlorine1 = simple("chlorine1", null, "items/chlorine1", new Item.Properties());
	public static final DeferredItem<Item> chlorine2 = simple("chlorine2", null, "items/chlorine2", new Item.Properties());
	public static final DeferredItem<Item> chlorine3 = simple("chlorine3", null, "items/chlorine3", new Item.Properties());
	public static final DeferredItem<Item> chlorine4 = simple("chlorine4", null, "items/chlorine4", new Item.Properties());
	public static final DeferredItem<Item> chlorine5 = simple("chlorine5", null, "items/chlorine5", new Item.Properties());
	public static final DeferredItem<Item> chlorine6 = simple("chlorine6", null, "items/chlorine6", new Item.Properties());
	public static final DeferredItem<Item> chlorine7 = simple("chlorine7", null, "items/chlorine7", new Item.Properties());
	public static final DeferredItem<Item> chlorine8 = simple("chlorine8", null, "items/chlorine8", new Item.Properties());
	public static final DeferredItem<Item> pc1 = simple("pc1", null, "items/pc1", new Item.Properties());
	public static final DeferredItem<Item> pc2 = simple("pc2", null, "items/pc2", new Item.Properties());
	public static final DeferredItem<Item> pc3 = simple("pc3", null, "items/pc3", new Item.Properties());
	public static final DeferredItem<Item> pc4 = simple("pc4", null, "items/pc4", new Item.Properties());
	public static final DeferredItem<Item> pc5 = simple("pc5", null, "items/pc5", new Item.Properties());
	public static final DeferredItem<Item> pc6 = simple("pc6", null, "items/pc6", new Item.Properties());
	public static final DeferredItem<Item> pc7 = simple("pc7", null, "items/pc7", new Item.Properties());
	public static final DeferredItem<Item> pc8 = simple("pc8", null, "items/pc8", new Item.Properties());
	public static final DeferredItem<Item> cloud1 = simple("cloud1", null, "items/cloud1", new Item.Properties());
	public static final DeferredItem<Item> cloud2 = simple("cloud2", null, "items/cloud2", new Item.Properties());
	public static final DeferredItem<Item> cloud3 = simple("cloud3", null, "items/cloud3", new Item.Properties());
	public static final DeferredItem<Item> cloud4 = simple("cloud4", null, "items/cloud4", new Item.Properties());
	public static final DeferredItem<Item> cloud5 = simple("cloud5", null, "items/cloud5", new Item.Properties());
	public static final DeferredItem<Item> cloud6 = simple("cloud6", null, "items/cloud6", new Item.Properties());
	public static final DeferredItem<Item> cloud7 = simple("cloud7", null, "items/cloud7", new Item.Properties());
	public static final DeferredItem<Item> cloud8 = simple("cloud8", null, "items/cloud8", new Item.Properties());
	public static final DeferredItem<Item> orange1 = simple("orange1", null, "items/orange1", new Item.Properties());
	public static final DeferredItem<Item> orange2 = simple("orange2", null, "items/orange2", new Item.Properties());
	public static final DeferredItem<Item> orange3 = simple("orange3", null, "items/orange3", new Item.Properties());
	public static final DeferredItem<Item> orange4 = simple("orange4", null, "items/orange4", new Item.Properties());
	public static final DeferredItem<Item> orange5 = simple("orange5", null, "items/orange5", new Item.Properties());
	public static final DeferredItem<Item> orange6 = simple("orange6", null, "items/orange6", new Item.Properties());
	public static final DeferredItem<Item> orange7 = simple("orange7", null, "items/orange7", new Item.Properties());
	public static final DeferredItem<Item> orange8 = simple("orange8", null, "items/orange8", new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumAchievementType> achievement_icon = multi("achievement_icon", "achievement_icon", EnumAchievementType.class, true, true, null, new Item.Properties());
	public static final DeferredItem<Item> template_folder = simple("template_folder", null, "items/template_folder", new Item.Properties());
	public static final DeferredItem<Item> nothing = simple("nothing", null, "items/nothing", new Item.Properties());
	// END GENERATED

	/// PRESS STAMPS ///
	public static final DeferredItem<ItemStamp> stamp_stone_flat = stamp("stamp_stone_flat", 32, StampType.FLAT, "items/stamp_stone_flat");
	public static final DeferredItem<ItemStamp> stamp_stone_plate = stamp("stamp_stone_plate", 32, StampType.PLATE, "items/stamp_stone_plate");
	public static final DeferredItem<ItemStamp> stamp_stone_wire = stamp("stamp_stone_wire", 32, StampType.WIRE, "items/stamp_stone_wire");
	public static final DeferredItem<ItemStamp> stamp_stone_circuit = stamp("stamp_stone_circuit", 32, StampType.CIRCUIT, "items/stamp_stone_circuit");
	public static final DeferredItem<ItemStamp> stamp_iron_flat = stamp("stamp_iron_flat", 64, StampType.FLAT, "items/stamp_iron_flat");
	public static final DeferredItem<ItemStamp> stamp_iron_plate = stamp("stamp_iron_plate", 64, StampType.PLATE, "items/stamp_iron_plate");
	public static final DeferredItem<ItemStamp> stamp_iron_wire = stamp("stamp_iron_wire", 64, StampType.WIRE, "items/stamp_iron_wire");
	public static final DeferredItem<ItemStamp> stamp_iron_circuit = stamp("stamp_iron_circuit", 64, StampType.CIRCUIT, "items/stamp_iron_circuit");
	public static final DeferredItem<ItemStamp> stamp_steel_flat = stamp("stamp_steel_flat", 192, StampType.FLAT, "items/stamp_steel_flat");
	public static final DeferredItem<ItemStamp> stamp_steel_plate = stamp("stamp_steel_plate", 192, StampType.PLATE, "items/stamp_steel_plate");
	public static final DeferredItem<ItemStamp> stamp_steel_wire = stamp("stamp_steel_wire", 192, StampType.WIRE, "items/stamp_steel_wire");
	public static final DeferredItem<ItemStamp> stamp_steel_circuit = stamp("stamp_steel_circuit", 192, StampType.CIRCUIT, "items/stamp_steel_circuit");
	public static final DeferredItem<ItemStamp> stamp_titanium_flat = stamp("stamp_titanium_flat", 256, StampType.FLAT, "items/stamp_titanium_flat");
	public static final DeferredItem<ItemStamp> stamp_titanium_plate = stamp("stamp_titanium_plate", 256, StampType.PLATE, "items/stamp_titanium_plate");
	public static final DeferredItem<ItemStamp> stamp_titanium_wire = stamp("stamp_titanium_wire", 256, StampType.WIRE, "items/stamp_titanium_wire");
	public static final DeferredItem<ItemStamp> stamp_titanium_circuit = stamp("stamp_titanium_circuit", 256, StampType.CIRCUIT, "items/stamp_titanium_circuit");
	public static final DeferredItem<ItemStamp> stamp_obsidian_flat = stamp("stamp_obsidian_flat", 512, StampType.FLAT, "items/stamp_obsidian_flat");
	public static final DeferredItem<ItemStamp> stamp_obsidian_plate = stamp("stamp_obsidian_plate", 512, StampType.PLATE, "items/stamp_obsidian_plate");
	public static final DeferredItem<ItemStamp> stamp_obsidian_wire = stamp("stamp_obsidian_wire", 512, StampType.WIRE, "items/stamp_obsidian_wire");
	public static final DeferredItem<ItemStamp> stamp_obsidian_circuit = stamp("stamp_obsidian_circuit", 512, StampType.CIRCUIT, "items/stamp_obsidian_circuit");
	public static final DeferredItem<ItemStamp> stamp_desh_flat = stamp("stamp_desh_flat", 0, StampType.FLAT, "items/stamp_desh_flat");
	public static final DeferredItem<ItemStamp> stamp_desh_plate = stamp("stamp_desh_plate", 0, StampType.PLATE, "items/stamp_desh_plate");
	public static final DeferredItem<ItemStamp> stamp_desh_wire = stamp("stamp_desh_wire", 0, StampType.WIRE, "items/stamp_desh_wire");
	public static final DeferredItem<ItemStamp> stamp_desh_circuit = stamp("stamp_desh_circuit", 0, StampType.CIRCUIT, "items/stamp_desh_circuit");
	public static final DeferredItem<ItemStamp> stamp_357 = stamp("stamp_357", 1000, StampType.C357, "items/stamp_357");
	public static final DeferredItem<ItemStamp> stamp_44 = stamp("stamp_44", 1000, StampType.C44, "items/stamp_44");
	public static final DeferredItem<ItemStamp> stamp_9 = stamp("stamp_9", 1000, StampType.C9, "items/stamp_9");
	public static final DeferredItem<ItemStamp> stamp_50 = stamp("stamp_50", 1000, StampType.C50, "items/stamp_50");
	public static final DeferredItem<ItemStamp> stamp_desh_357 = stamp("stamp_desh_357", 0, StampType.C357, "items/stamp_357_desh");
	public static final DeferredItem<ItemStamp> stamp_desh_44 = stamp("stamp_desh_44", 0, StampType.C44, "items/stamp_44_desh");
	public static final DeferredItem<ItemStamp> stamp_desh_9 = stamp("stamp_desh_9", 0, StampType.C9, "items/stamp_9_desh");
	public static final DeferredItem<ItemStamp> stamp_desh_50 = stamp("stamp_desh_50", 0, StampType.C50, "items/stamp_50_desh");

	/// MACHINE UPGRADES (TODO ejector/stack/muffler upgrades, ItemMachineUpgrade subclasses) ///
	public static final DeferredItem<ItemMachineUpgrade> upgrade_speed_1 = upgrade("upgrade_speed_1", UpgradeType.SPEED, 1, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_speed_2 = upgrade("upgrade_speed_2", UpgradeType.SPEED, 2, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_speed_3 = upgrade("upgrade_speed_3", UpgradeType.SPEED, 3, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_effect_1 = upgrade("upgrade_effect_1", UpgradeType.EFFECT, 1, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_effect_2 = upgrade("upgrade_effect_2", UpgradeType.EFFECT, 2, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_effect_3 = upgrade("upgrade_effect_3", UpgradeType.EFFECT, 3, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_power_1 = upgrade("upgrade_power_1", UpgradeType.POWER, 1, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_power_2 = upgrade("upgrade_power_2", UpgradeType.POWER, 2, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_power_3 = upgrade("upgrade_power_3", UpgradeType.POWER, 3, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_fortune_1 = upgrade("upgrade_fortune_1", UpgradeType.FORTUNE, 1, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_fortune_2 = upgrade("upgrade_fortune_2", UpgradeType.FORTUNE, 2, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_fortune_3 = upgrade("upgrade_fortune_3", UpgradeType.FORTUNE, 3, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_afterburn_1 = upgrade("upgrade_afterburn_1", UpgradeType.AFTERBURN, 1, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_afterburn_2 = upgrade("upgrade_afterburn_2", UpgradeType.AFTERBURN, 2, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_afterburn_3 = upgrade("upgrade_afterburn_3", UpgradeType.AFTERBURN, 3, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_overdrive_1 = upgrade("upgrade_overdrive_1", UpgradeType.OVERDRIVE, 1, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_overdrive_2 = upgrade("upgrade_overdrive_2", UpgradeType.OVERDRIVE, 2, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_overdrive_3 = upgrade("upgrade_overdrive_3", UpgradeType.OVERDRIVE, 3, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_radius = upgrade("upgrade_radius", UpgradeType.SPECIAL, 0, 16);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_health = upgrade("upgrade_health", UpgradeType.SPECIAL, 0, 16);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_smelter = upgrade("upgrade_smelter", UpgradeType.SPECIAL, 0, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_shredder = upgrade("upgrade_shredder", UpgradeType.SPECIAL, 0, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_centrifuge = upgrade("upgrade_centrifuge", UpgradeType.SPECIAL, 0, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_crystallizer = upgrade("upgrade_crystallizer", UpgradeType.SPECIAL, 0, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_nullifier = upgrade("upgrade_nullifier", UpgradeType.SPECIAL, 0, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_screm = upgrade("upgrade_screm", UpgradeType.SPECIAL, 0, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_gc_speed = upgrade("upgrade_gc_speed", UpgradeType.SPECIAL, 0, 1);
	public static final DeferredItem<ItemMachineUpgrade> upgrade_5g = upgrade("upgrade_5g", UpgradeType.SPECIAL, 0, 1);

	/// FLUID CONTAINERS (the fluid is the FLUID_TYPE component, the original's damage value) ///
	public static final DeferredItem<ItemCanister> canister_full = fluidItem("canister_full", p -> new ItemCanister(p.craftRemainder(canister_empty.get())), canister_empty, NtmTab.CONTROL, "items/canister_empty", "items/canister_overlay");
	public static final DeferredItem<ItemGasTank> gas_full = fluidItem("gas_full", p -> new ItemGasTank(p.craftRemainder(gas_empty.get())), gas_empty, NtmTab.CONTROL, "items/gas_empty", "items/gas_bottle", "items/gas_label");
	public static final DeferredItem<ItemFluidTank> fluid_tank_full = fluidItem("fluid_tank_full", p -> new ItemFluidTank(p.craftRemainder(fluid_tank_empty.get())), fluid_tank_empty, NtmTab.CONTROL, "items/fluid_tank", "items/fluid_tank_overlay");
	public static final DeferredItem<ItemFluidTank> fluid_tank_lead_full = fluidItem("fluid_tank_lead_full", p -> new ItemFluidTank(p.craftRemainder(fluid_tank_lead_empty.get())), fluid_tank_lead_empty, NtmTab.CONTROL, "items/fluid_tank_lead", "items/fluid_tank_lead_overlay");
	public static final DeferredItem<ItemFluidTank> fluid_barrel_full = fluidItem("fluid_barrel_full", p -> new ItemFluidTank(p.craftRemainder(fluid_barrel_empty.get())), fluid_barrel_empty, NtmTab.CONTROL, "items/fluid_barrel", "items/fluid_barrel_overlay");
	public static final DeferredItem<ItemInfiniteFluid> fluid_barrel_infinite = fluidItem("fluid_barrel_infinite", p -> new ItemInfiniteFluid(p.stacksTo(1), null, 1_000_000_000), fluid_barrel_full, NtmTab.CONTROL, "items/fluid_barrel_infinite");
	public static final DeferredItem<ItemInfiniteFluid> inf_water = fluidItem("inf_water", p -> new ItemInfiniteFluid(p.stacksTo(1), Fluids.WATER, 50), fluid_barrel_infinite, NtmTab.CONTROL, "items/inf_water");
	public static final DeferredItem<ItemFluidIDMulti> fluid_identifier_multi = fluidItem("fluid_identifier_multi", p -> new ItemFluidIDMulti(p.stacksTo(1)), null, NtmTab.TEMPLATE, "items/fluid_identifier_multi", "items/fluid_identifier_overlay");

	/// PARTS (TODO item_expensive's "Expensive mode item" tooltip) ///
	public static final ItemEnumMulti.Variants<EnumCircuitType> circuit = multi("circuit", "circuit", EnumCircuitType.class, true, true, NtmTab.PARTS, new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumPartType> part_generic = multi("part_generic", "part_generic", EnumPartType.class, true, part -> "items/" + part.texName, NtmTab.PARTS, new Item.Properties());
	public static final ItemEnumMulti.Variants<EnumExpensiveType> item_expensive = multi("item_expensive", "item_expensive", EnumExpensiveType.class, true, true, NtmTab.PARTS, new Item.Properties());

	/// MATERIAL AUTOGEN: one item per material that has the shape, textures overridden like the original's aot() ///
	public static final List<AutogenItems> AUTOGEN = new ArrayList<>();
	public static final AutogenItems bolt = autogen("bolt", "boltntm", MaterialShapes.BOLT, Map.of());
	public static final AutogenItems bedrock_ore_fragment = autogen("bedrock_ore_fragment", "bedrock_ore_fragment", MaterialShapes.FRAGMENT, Map.of(Mats.MAT_BISMUTH, "bedrock_ore_fragment_bismuth"));
	public static final AutogenItems shell = autogen("shell", "shellntm", MaterialShapes.SHELL, Map.of());
	public static final AutogenItems pipe = autogen("pipe", "pipentm", MaterialShapes.PIPE, Map.of());
	public static final AutogenItems ingot_raw = autogen("ingot_raw", "ingot_raw", MaterialShapes.INGOT, Map.of());
	public static final AutogenItems plate_cast = autogen("plate_cast", "plate_cast", MaterialShapes.CASTPLATE, Map.of(Mats.MAT_BISMUTH, "plate_cast_bismuth"));
	public static final AutogenItems plate_welded = autogen("plate_welded", "plate_welded", MaterialShapes.WELDEDPLATE, Map.of());
	public static final AutogenItems wire_fine = autogen("wire_fine", "wire_fine", MaterialShapes.WIRE, Map.of(
			Mats.MAT_ALUMINIUM, "wire_aluminium", Mats.MAT_COPPER, "wire_copper",
			Mats.MAT_MINGRADE, "wire_red_copper", Mats.MAT_GOLD, "wire_gold",
			Mats.MAT_TUNGSTEN, "wire_tungsten", Mats.MAT_CARBON, "wire_carbon",
			Mats.MAT_SCHRABIDIUM, "wire_schrabidium", Mats.MAT_MAGTUNG, "wire_magnetized_tungsten"));
	public static final AutogenItems wire_dense = autogen("wire_dense", "wire_dense", MaterialShapes.DENSEWIRE, Map.of());
	public static final AutogenItems part_barrel_light = autogen("part_barrel_light", "part_barrel_light", MaterialShapes.LIGHTBARREL, Map.of());
	public static final AutogenItems part_barrel_heavy = autogen("part_barrel_heavy", "part_barrel_heavy", MaterialShapes.HEAVYBARREL, Map.of());
	public static final AutogenItems part_receiver_light = autogen("part_receiver_light", "part_receiver_light", MaterialShapes.LIGHTRECEIVER, Map.of());
	public static final AutogenItems part_receiver_heavy = autogen("part_receiver_heavy", "part_receiver_heavy", MaterialShapes.HEAVYRECEIVER, Map.of());
	public static final AutogenItems part_mechanism = autogen("part_mechanism", "part_mechanism", MaterialShapes.MECHANISM, Map.of());
	public static final AutogenItems part_stock = autogen("part_stock", "part_stock", MaterialShapes.STOCK, Map.of());
	public static final AutogenItems part_grip = autogen("part_grip", "part_grip", MaterialShapes.GRIP, Map.of());

	/// ENUM ITEMS WITH THEIR OWN CLASSES ///
	/** Battery socket packs, drawn with the socket's model (RenderBatterySocket.itemRenderer) */
	public static final ItemEnumMulti.Variants<EnumBatteryPack> battery_pack = multi("battery_pack", "battery_pack", EnumBatteryPack.class, true,
			value -> null, NtmTab.CONTROL, new Item.Properties(), (p, descriptionId, value) -> new com.hbm.items.machine.ItemBatteryPack(p, descriptionId, value));
	public static final ItemEnumMulti.Variants<EnumPileRod> pile_rod = multi("pile_rod", "pile_rod", EnumPileRod.class, true, true, NtmTab.CONTROL, new Item.Properties());
	public static final ItemEnumMulti.Variants<BreedingRodType> rod = multi("rod", "rod", BreedingRodType.class, true,
			value -> "items/rod." + value.name().toLowerCase(java.util.Locale.US), NtmTab.CONTROL, new Item.Properties(), (p, descriptionId, value) -> new ItemEnumMulti(p.craftRemainder(rod_empty.get()), descriptionId));
	public static final ItemEnumMulti.Variants<BreedingRodType> rod_dual = multi("rod_dual", "rod_dual", BreedingRodType.class, true,
			value -> "items/rod_dual." + value.name().toLowerCase(java.util.Locale.US), NtmTab.CONTROL, new Item.Properties(), (p, descriptionId, value) -> new ItemEnumMulti(p.craftRemainder(rod_dual_empty.get()), descriptionId));
	public static final ItemEnumMulti.Variants<BreedingRodType> rod_quad = multi("rod_quad", "rod_quad", BreedingRodType.class, true,
			value -> "items/rod_quad." + value.name().toLowerCase(java.util.Locale.US), NtmTab.CONTROL, new Item.Properties(), (p, descriptionId, value) -> new ItemEnumMulti(p.craftRemainder(rod_quad_empty.get()), descriptionId));
	public static final ItemEnumMulti.Variants<EnumDriveType> drive = multi("drive", "drive", EnumDriveType.class, true, true, NtmTab.PARTS, new Item.Properties());

	/** Shredder blades, desh blades don't wear */
	public static final DeferredItem<com.hbm.items.machine.ItemBlades> blades_steel = register("blades_steel", com.hbm.items.machine.ItemBlades::new, new Item.Properties().durability(400), NtmTab.CONTROL);
	public static final DeferredItem<com.hbm.items.machine.ItemBlades> blades_titanium = register("blades_titanium", com.hbm.items.machine.ItemBlades::new, new Item.Properties().durability(500), NtmTab.CONTROL);
	public static final DeferredItem<com.hbm.items.machine.ItemBlades> blades_desh = register("blades_desh", com.hbm.items.machine.ItemBlades::new, new Item.Properties().stacksTo(1), NtmTab.CONTROL);

	/** Fluid stand-in for recipe displays, tinted with the fluid color, not in a creative tab like the original */
	public static final DeferredItem<com.hbm.items.machine.ItemFluidIcon> fluid_icon = register("fluid_icon", com.hbm.items.machine.ItemFluidIcon::new, new Item.Properties(), null);

	/** Unlocks pooled recipes of generic recipe machines, the model picks the texture by the pool (item property "hbm:pool") */
	public static final DeferredItem<com.hbm.items.machine.ItemBlueprints> blueprints = register("blueprints", com.hbm.items.machine.ItemBlueprints::new, new Item.Properties(), NtmTab.TEMPLATE);

	static {
		FLAT_MODELS.put(dosimeter, "items/dosimeter");
		FLAT_MODELS.put(geiger_counter, "items/geiger_counter");
		FLAT_MODELS.put(battery_creative, "items/battery_creative_new");
		FLAT_MODELS.put(battery_potato, "items/battery_potato");
		FLAT_MODELS.put(cube_power, "items/cube_power");
		FLAT_MODELS.put(fluid_icon, "items/fluid_icon");
		FLAT_MODELS.put(blades_steel, "items/blades_steel");
		FLAT_MODELS.put(blades_titanium, "items/blades_titanium");
		FLAT_MODELS.put(blades_desh, "items/blades_desh");
	}

	/** Press stamp with a flat model, in the control tab like the original */
	private static DeferredItem<ItemStamp> stamp(String name, int dura, StampType type, String texture) {
		DeferredItem<ItemStamp> item = register(name, p -> new ItemStamp(p, dura, type), new Item.Properties(), NtmTab.CONTROL);
		FLAT_MODELS.put(item, texture);
		return item;
	}

	/** Machine upgrade with a flat model, in the control tab like the original */
	private static DeferredItem<ItemMachineUpgrade> upgrade(String name, UpgradeType type, int tier, int stackSize) {
		DeferredItem<ItemMachineUpgrade> item = register(name, p -> new ItemMachineUpgrade(p.stacksTo(stackSize), type, tier), new Item.Properties(), NtmTab.CONTROL);
		FLAT_MODELS.put(item, "items/" + name);
		return item;
	}

	/** Hand-ported item placed in the tab after "after" (or appended), with a flat or layered model */
	private static <T extends Item> DeferredItem<T> fluidItem(String name, Function<Item.Properties, T> factory, DeferredItem<?> after, NtmTab tab, String... layers) {
		DeferredItem<T> item = register(name, factory, new Item.Properties(), null);
		if(after != null) tab.addAfter(item, after); else tab.add(item);
		if(layers.length == 1) FLAT_MODELS.put(item, layers[0]);
		else LAYERED_MODELS.put(item, layers);
		return item;
	}

	public static DeferredItem<Item> register(String name, NtmTab tab) {
		return register(name, Item::new, new Item.Properties(), tab);
	}

	public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> factory, Item.Properties props, NtmTab tab) {
		DeferredItem<T> item = ITEMS.registerItem(name, factory, props);
		if(tab != null) tab.add(item);
		return item;
	}

	/** Plain item with a flat model (new Item() in the original) */
	private static DeferredItem<Item> simple(String name, NtmTab tab, String texture, Item.Properties props) {
		DeferredItem<Item> item = register(name, Item::new, props, tab);
		FLAT_MODELS.put(item, texture);
		return item;
	}

	/**
	 * ItemEnumMulti: one item per enum value, named [name]_[value]. Textures are items/[name].[value] if
	 * the original had one texture per value (multiTexture), items/[name] otherwise.
	 */
	private static <E extends Enum<E>> ItemEnumMulti.Variants<E> multi(String name, String originalName, Class<E> theEnum, boolean multiName, boolean multiTexture, NtmTab tab, Item.Properties props) {
		return multi(name, originalName, theEnum, multiName, value -> "items/" + name + (multiTexture ? "." + value.name().toLowerCase(java.util.Locale.US) : ""), tab, props);
	}

	/** ItemEnumMulti with custom texture names per variant (the original's overridden registerIcons) */
	private static <E extends Enum<E>> ItemEnumMulti.Variants<E> multi(String name, String originalName, Class<E> theEnum, boolean multiName, Function<E, String> texture, NtmTab tab, Item.Properties props) {
		return multi(name, originalName, theEnum, multiName, texture, tab, props, (p, descriptionId, value) -> new ItemEnumMulti(p, descriptionId));
	}

	/** Creates the item of one variant (for ItemEnumMulti subclasses of the original, e.g. batteries or rods with a container item) */
	@FunctionalInterface
	public interface VariantFactory<E> {
		Item create(Item.Properties properties, String descriptionId, E value);
	}

	/** Variants made by a custom factory; a null texture means the item has its own renderer (ITEM_RENDERED) */
	private static <E extends Enum<E>> ItemEnumMulti.Variants<E> multi(String name, String originalName, Class<E> theEnum, boolean multiName, Function<E, String> texture, NtmTab tab, Item.Properties props, VariantFactory<E> factory) {
		ItemEnumMulti.Variants<E> variants = new ItemEnumMulti.Variants<>(name, theEnum);
		E[] order = theEnum.getEnumConstants();
		if(order[0] instanceof com.hbm.interfaces.IOrderedEnum ordered) {
			@SuppressWarnings("unchecked") E[] custom = (E[]) ordered.getOrder();
			order = custom;
		}
		for(E value : order) {
			String lower = value.name().toLowerCase(java.util.Locale.US);
			String descriptionId = "item.hbm." + originalName.toLowerCase() + (multiName ? "." + lower : "");
			DeferredItem<Item> item = register(ItemEnumMulti.Variants.variantName(name, value), p -> factory.create(p, descriptionId, value), props, tab);
			variants.put(value, item);
			String tex = texture.apply(value);
			if(tex != null) FLAT_MODELS.put(item, tex);
			else ITEM_RENDERED.add(item);
		}
		return variants;
	}

	/** ItemAutogen for every material with the shape in its autogen, in material order like the original's sub items */
	private static AutogenItems autogen(String name, String unlocalizedName, MaterialShapes shape, Map<NTMMaterial, String> textureOverrides) {
		AutogenItems set = new AutogenItems(name, shape, "items/" + name, textureOverrides);
		String descriptionId = "item.hbm." + unlocalizedName;
		for(NTMMaterial mat : Mats.orderedList) {
			if(!mat.autogen.contains(shape)) continue;
			DeferredItem<ItemAutogen> item = register(AutogenItems.itemName(name, mat), p -> new ItemAutogen(p, shape, mat, descriptionId), new Item.Properties(), NtmTab.PARTS);
			set.put(mat, item);
			FLAT_MODELS.put(item, set.texture(mat));
		}
		AUTOGEN.add(set);
		return set;
	}

	/** ItemCustomLore, tooltip from the "item.[original name].desc" translation */
	private static DeferredItem<ItemCustomLore> lore(String name, String originalName, NtmTab tab, String texture, Item.Properties props) {
		DeferredItem<ItemCustomLore> item = register(name, p -> new ItemCustomLore(p, originalName), props, tab);
		FLAT_MODELS.put(item, texture);
		return item;
	}
}
