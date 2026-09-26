package com.hbm.hazard;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.OreDictManager;
import com.hbm.hazard.type.*;
import com.hbm.items.ModItems;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Hazard values and assignments. The isotope/shape constants are unchanged from the original.
 *
 * Item assignments are generated, see registerItems().
 */
@SuppressWarnings("unused")
public class HazardRegistry {

	//CO60		             5a		β−	030.00Rad/s	Spicy
	//SR90		            29a		β−	015.00Rad/s Spicy
	//TC99		       211,000a		β−	002.75Rad/s	Spicy
	//I181		           192h		β−	150.00Rad/s	2 much spice :(
	//XE135		             9h		β−	aaaaaaaaaaaaaaaa
	//CS137		            30a		β−	020.00Rad/s	Spicy
	//AU198		            64h		β−	500.00Rad/s	2 much spice :(
	//PB209		             3h		β−	10,000.00Rad/s mama mia my face is melting off
	//AT209		             5h		β+	like 7.5k or sth idk bruv
	//PO210		           138d		α	075.00Rad/s	Spicy
	//RA226		         1,600a		α	007.50Rad/s
	//AC227		            22a		β−	030.00Rad/s Spicy
	//TH232		14,000,000,000a		α	000.10Rad/s
	//U233		       160,000a		α	005.00Rad/s
	//U235		   700,000,000a		α	001.00Rad/s
	//U238		 4,500,000,000a		α	000.25Rad/s
	//NP237		     2,100,000a		α	002.50Rad/s
	//PU238		            88a		α	010.00Rad/s	Spicy
	//PU239		        24,000a		α	005.00Rad/s
	//PU240		         6,600a		α	007.50Rad/s
	//PU241		            14a		β−	025.00Rad/s	Spicy
	//AM241		           432a		α	008.50Rad/s
	//AM242		           141a		β−	009.50Rad/s

	//simplified groups for ReC compat
	public static final float gen_S = 10_000F;
	public static final float gen_H = 2_000F;
	public static final float gen_10D = 100F;
	public static final float gen_100D = 80F;
	public static final float gen_1Y = 50F;
	public static final float gen_10Y = 30F;
	public static final float gen_100Y = 10F;
	public static final float gen_1K = 7.5F;
	public static final float gen_10K = 6.25F;
	public static final float gen_100K = 5F;
	public static final float gen_1M = 2.5F;
	public static final float gen_10M = 1.5F;
	public static final float gen_100M = 1F;
	public static final float gen_1B = 0.5F;
	public static final float gen_10B = 0.1F;

	public static final float co60 = 30.0F;
	public static final float sr90 = 15.0F;
	public static final float tc99 = 2.75F;
	public static final float i131 = 150.0F;
	public static final float xe135 = 1250.0F;
	public static final float cs137 = 20.0F;
	public static final float au198 = 500.0F;
	public static final float pb209 = 10000.0F;
	public static final float at209 = 7500.0F;
	public static final float po210 = 75.0F;
	public static final float ra226 = 7.5F;
	public static final float ac227 = 30.0F;
	public static final float th232 = 0.1F;
	public static final float thf = 1.75F;
	public static final float u = 0.35F;
	public static final float u233 = 5.0F;
	public static final float u235 = 1.0F;
	public static final float u238 = 0.25F;
	public static final float uf = 0.5F;
	public static final float uzh = 0.125F;
	public static final float np237 = 2.5F;
	public static final float npf = 1.5F;
	public static final float pu = 7.5F;
	public static final float purg = 6.25F;
	public static final float pu238 = 10.0F;
	public static final float pu239 = 5.0F;
	public static final float pu240 = 7.5F;
	public static final float pu241 = 25.0F;
	public static final float puf = 4.25F;
	public static final float am241 = 8.5F;
	public static final float am242 = 9.5F;
	public static final float amrg = 9.0F;
	public static final float amf = 4.75F;
	public static final float mox = 2.5F;
	public static final float sa326 = 15.0F;
	public static final float sa327 = 17.5F;
	public static final float saf = 5.85F;
	public static final float sas3 = 5F;
	public static final float gh336 = 5.0F;
	public static final float mud = 1.0F;
	public static final float radsource_mult = 3.0F;
	public static final float pobe = po210 * radsource_mult;
	public static final float rabe = ra226 * radsource_mult;
	public static final float pube = pu238 * radsource_mult;
	public static final float zfb_bi = u235 * 0.35F;
	public static final float zfb_pu241 = pu241 * 0.5F;
	public static final float zfb_am_mix = amrg * 0.5F;
	public static final float bf = 300_000.0F;
	public static final float bfb = 500_000.0F;

	public static final float sr = sa326 * 0.1F;
	public static final float sb = sa326 * 0.1F;
	public static final float trx = 25.0F;
	public static final float trn = 0.1F;
	public static final float wst = 15.0F;
	public static final float wstv = 7.5F;
	public static final float yc = u;
	public static final float fo = 10F;

	public static final float nugget = 0.1F;
	public static final float ingot = 1.0F;
	public static final float gem = 1.0F;
	public static final float plate = ingot;
	public static final float plateCast = plate * 3;
	public static final float powder_mult = 3.0F;
	public static final float powder = ingot * powder_mult;
	public static final float powder_tiny = nugget * powder_mult;
	public static final float ore = ingot;
	public static final float block = 10.0F;
	public static final float crystal = block;
	public static final float billet = 0.5F;
	public static final float rtg = billet * 3;
	public static final float rod = 0.5F;
	public static final float rod_dual = rod * 2;
	public static final float rod_quad = rod * 4;
	public static final float rod_rbmk = rod * 8;

	public static final HazardTypeBase RADIATION = new HazardTypeRadiation();
	public static final HazardTypeBase DIGAMMA = new HazardTypeDigamma();
	public static final HazardTypeBase HOT = new HazardTypeHot();
	public static final HazardTypeBase BLINDING = new HazardTypeBlinding();
	public static final HazardTypeBase ASBESTOS = new HazardTypeAsbestos();
	public static final HazardTypeBase COAL = new HazardTypeCoal();
	public static final HazardTypeBase HYDROACTIVE = new HazardTypeHydroactive();
	public static final HazardTypeBase EXPLOSIVE = new HazardTypeExplosive();

	/**
	 * Item specific hazards. Material hazards (ingots, nuggets, ores...) are registered on their tags by the
	 * OreDictManager, which has to run first.
	 *
	 * TODO fuel rods (RBMK, PWR, ZIRNOX, RTG pellets...) with their hazard modifiers, metadata based items
	 */
	public static void registerItems() {

		/// GENERATED by tools/gen_hazards.py from the original's registerItems(), don't edit by hand ///
		// BEGIN GENERATED
		HazardSystem.register(Items.GUNPOWDER, makeData(EXPLOSIVE, 1F));
		HazardSystem.register(Blocks.TNT, makeData(EXPLOSIVE, 4F));
		HazardSystem.register(Items.PUMPKIN_PIE, makeData(EXPLOSIVE, 1F));
		HazardSystem.register(ModItems.ball_dynamite, makeData(EXPLOSIVE, 2F));
		HazardSystem.register(ModItems.stick_tnt, makeData(EXPLOSIVE, 1.5F));
		HazardSystem.register(ModItems.stick_semtex, makeData(EXPLOSIVE, 2.5F));
		HazardSystem.register(ModItems.stick_c4, makeData(EXPLOSIVE, 2.5F));
		HazardSystem.register(ModItems.cordite, makeData(EXPLOSIVE, 2F));
		HazardSystem.register(ModItems.ballistite, makeData(EXPLOSIVE, 1F));
		HazardSystem.register(OreDictManager.tag("dustCoal"), makeData(COAL, powder));
		HazardSystem.register(OreDictManager.tag("dustTinyCoal"), makeData(COAL, powder_tiny));
		HazardSystem.register(OreDictManager.tag("dustLignite"), makeData(COAL, powder));
		HazardSystem.register(OreDictManager.tag("dustTinyLignite"), makeData(COAL, powder_tiny));
		HazardSystem.register(ModItems.demon_core_closed, makeData(RADIATION, 100_000F));
		HazardSystem.register(ModItems.cell_tritium, makeData(RADIATION, 0.001F));
		HazardSystem.register(ModItems.cell_sas3, makeData().addEntry(RADIATION, sas3).addEntry(BLINDING, 60F));
		HazardSystem.register(ModItems.cell_balefire, makeData(RADIATION, 50F));
		HazardSystem.register(ModItems.powder_balefire, makeData(RADIATION, 500F));
		HazardSystem.register(ModItems.egg_balefire_shard, makeData(RADIATION, bf * nugget));
		HazardSystem.register(ModItems.egg_balefire, makeData(RADIATION, bf * ingot));
		HazardSystem.register(ModItems.solid_fuel_bf, makeData(RADIATION, 1000));
		HazardSystem.register(ModItems.solid_fuel_presto_bf, makeData(RADIATION, 2000));
		HazardSystem.register(ModItems.solid_fuel_presto_triplet_bf, makeData(RADIATION, 6000));
		HazardSystem.register(ModItems.scrap_nuclear, makeData(RADIATION, 1F));
		HazardSystem.register(ModBlocks.block_trinitite, makeData(RADIATION, trn * block));
		HazardSystem.register(ModItems.nuclear_waste, makeData(RADIATION, wst * ingot));
		HazardSystem.register(ModItems.billet_nuclear_waste, makeData(RADIATION, wst * billet));
		HazardSystem.register(ModItems.nuclear_waste_tiny, makeData(RADIATION, wst * nugget));
		HazardSystem.register(ModBlocks.ancient_scrap, makeData(RADIATION, 150F));
		HazardSystem.register(ModBlocks.block_corium, makeData(RADIATION, 150F));
		HazardSystem.register(ModBlocks.block_corium_cobble, makeData(RADIATION, 150F));
		HazardSystem.register(ModItems.rod_zirnox_natural_uranium_fuel_depleted, makeData(RADIATION, wst * rod_dual * 11.5F));
		HazardSystem.register(ModItems.rod_zirnox_uranium_fuel_depleted, makeData(RADIATION, wst * rod_dual * 10F));
		HazardSystem.register(ModItems.rod_zirnox_thorium_fuel_depleted, makeData(RADIATION, wst * rod_dual * 7.5F));
		HazardSystem.register(ModItems.rod_zirnox_mox_fuel_depleted, makeData(RADIATION, wst * rod_dual * 10F));
		HazardSystem.register(ModItems.rod_zirnox_plutonium_fuel_depleted, makeData(RADIATION, wst * rod_dual * 12.5F));
		HazardSystem.register(ModItems.rod_zirnox_u233_fuel_depleted, makeData(RADIATION, wst * rod_dual * 10F));
		HazardSystem.register(ModItems.rod_zirnox_u235_fuel_depleted, makeData(RADIATION, wst * rod_dual * 11F));
		HazardSystem.register(ModItems.rod_zirnox_les_fuel_depleted, makeData().addEntry(RADIATION, wst * rod_dual * 15F).addEntry(BLINDING, 20F));
		HazardSystem.register(ModItems.rod_zirnox_tritium, makeData(RADIATION, 0.001F * rod_dual));
		HazardSystem.register(ModItems.rod_zirnox_zfb_mox_depleted, makeData(RADIATION, wst * rod_dual * 5F));
		HazardSystem.register(ModItems.debris_graphite, makeData().addEntry(RADIATION, 70F).addEntry(HOT, 5F));
		HazardSystem.register(ModItems.debris_metal, makeData(RADIATION, 5F));
		HazardSystem.register(ModItems.debris_fuel, makeData().addEntry(RADIATION, 500F).addEntry(HOT, 5F));
		HazardSystem.register(ModItems.debris_concrete, makeData(RADIATION, 30F));
		HazardSystem.register(ModItems.debris_exchanger, makeData(RADIATION, 25F));
		HazardSystem.register(ModItems.nugget_uranium_fuel, makeData(RADIATION, uf * nugget));
		HazardSystem.register(ModItems.billet_uranium_fuel, makeData(RADIATION, uf * billet));
		HazardSystem.register(ModItems.ingot_uranium_fuel, makeData(RADIATION, uf * ingot));
		HazardSystem.register(ModBlocks.block_uranium_fuel, makeData(RADIATION, uf * block));
		HazardSystem.register(ModItems.billet_uzh, makeData(RADIATION, uzh * billet));
		HazardSystem.register(ModItems.nugget_plutonium_fuel, makeData(RADIATION, puf * nugget));
		HazardSystem.register(ModItems.billet_plutonium_fuel, makeData(RADIATION, puf * billet));
		HazardSystem.register(ModItems.ingot_plutonium_fuel, makeData(RADIATION, puf * ingot));
		HazardSystem.register(ModBlocks.block_plutonium_fuel, makeData(RADIATION, puf * block));
		HazardSystem.register(ModItems.nugget_thorium_fuel, makeData(RADIATION, thf * nugget));
		HazardSystem.register(ModItems.billet_thorium_fuel, makeData(RADIATION, thf * billet));
		HazardSystem.register(ModItems.ingot_thorium_fuel, makeData(RADIATION, thf * ingot));
		HazardSystem.register(ModBlocks.block_thorium_fuel, makeData(RADIATION, thf * block));
		HazardSystem.register(ModItems.nugget_neptunium_fuel, makeData(RADIATION, npf * nugget));
		HazardSystem.register(ModItems.billet_neptunium_fuel, makeData(RADIATION, npf * billet));
		HazardSystem.register(ModItems.ingot_neptunium_fuel, makeData(RADIATION, npf * ingot));
		HazardSystem.register(ModItems.nugget_mox_fuel, makeData(RADIATION, mox * nugget));
		HazardSystem.register(ModItems.billet_mox_fuel, makeData(RADIATION, mox * billet));
		HazardSystem.register(ModItems.ingot_mox_fuel, makeData(RADIATION, mox * ingot));
		HazardSystem.register(ModBlocks.block_mox_fuel, makeData(RADIATION, mox * block));
		HazardSystem.register(ModItems.nugget_americium_fuel, makeData(RADIATION, amf * nugget));
		HazardSystem.register(ModItems.billet_americium_fuel, makeData(RADIATION, amf * billet));
		HazardSystem.register(ModItems.ingot_americium_fuel, makeData(RADIATION, amf * ingot));
		HazardSystem.register(ModItems.nugget_schrabidium_fuel, makeData().addEntry(RADIATION, saf * nugget).addEntry(BLINDING, 5F * nugget));
		HazardSystem.register(ModItems.billet_schrabidium_fuel, makeData().addEntry(RADIATION, saf * billet).addEntry(BLINDING, 5F * billet));
		HazardSystem.register(ModItems.ingot_schrabidium_fuel, makeData().addEntry(RADIATION, saf * ingot).addEntry(BLINDING, 5F * ingot));
		HazardSystem.register(ModBlocks.block_schrabidium_fuel, makeData().addEntry(RADIATION, saf * block).addEntry(BLINDING, 5F * block));
		HazardSystem.register(ModItems.nugget_hes, makeData(RADIATION, saf * nugget));
		HazardSystem.register(ModItems.billet_hes, makeData(RADIATION, saf * billet));
		HazardSystem.register(ModItems.ingot_hes, makeData(RADIATION, saf * ingot));
		HazardSystem.register(ModItems.nugget_les, makeData(RADIATION, saf * nugget));
		HazardSystem.register(ModItems.billet_les, makeData(RADIATION, saf * billet));
		HazardSystem.register(ModItems.ingot_les, makeData(RADIATION, saf * ingot));
		HazardSystem.register(ModItems.billet_balefire_gold, makeData(RADIATION, au198 * billet));
		HazardSystem.register(ModItems.billet_flashlead, makeData().addEntry(RADIATION, pb209 * 1.25F * billet).addEntry(HOT, 7F));
		HazardSystem.register(ModItems.billet_po210be, makeData(RADIATION, pobe * billet));
		HazardSystem.register(ModItems.billet_ra226be, makeData(RADIATION, rabe * billet));
		HazardSystem.register(ModItems.billet_pu238be, makeData(RADIATION, pube * billet));
		HazardSystem.register(ModItems.powder_yellowcake, makeData(RADIATION, yc * powder));
		HazardSystem.register(ModItems.fallout, makeData(RADIATION, fo * powder));
		HazardSystem.register(ModItems.powder_caesium, makeData().addEntry(HYDROACTIVE, 1F).addEntry(HOT, 3F));
		HazardSystem.register(ModBlocks.brick_asbestos, makeData(ASBESTOS, 1F));
		HazardSystem.register(ModBlocks.tile_lab_broken, makeData(ASBESTOS, 1F));
		HazardSystem.register(ModItems.powder_coltan_ore, makeData(ASBESTOS, 3F));
		HazardSystem.register(ModItems.crystal_uranium, makeData(RADIATION, u * crystal));
		HazardSystem.register(ModItems.crystal_thorium, makeData(RADIATION, th232 * crystal));
		HazardSystem.register(ModItems.crystal_plutonium, makeData(RADIATION, pu * crystal));
		HazardSystem.register(ModItems.crystal_schraranium, makeData(RADIATION, sr * crystal));
		HazardSystem.register(ModItems.crystal_schrabidium, makeData(RADIATION, sa326 * crystal));
		HazardSystem.register(ModItems.crystal_phosphorus, makeData(HOT, 2F * crystal));
		HazardSystem.register(ModItems.crystal_lithium, makeData(HYDROACTIVE, 1F * crystal));
		HazardSystem.register(ModItems.crystal_trixite, makeData(RADIATION, trx * crystal));
		HazardSystem.register(ModItems.boy_propellant, makeData(EXPLOSIVE, 2F));
		HazardSystem.register(ModItems.gadget_core, makeData(RADIATION, pu239 * nugget * 10));
		HazardSystem.register(ModItems.boy_target, makeData(RADIATION, u235 * ingot * 2));
		HazardSystem.register(ModItems.boy_bullet, makeData(RADIATION, u235 * ingot));
		HazardSystem.register(ModItems.man_core, makeData(RADIATION, pu239 * nugget * 10));
		HazardSystem.register(ModItems.mike_core, makeData(RADIATION, u238 * nugget * 10));
		HazardSystem.register(ModItems.tsar_core, makeData(RADIATION, pu239 * nugget * 15));
		HazardSystem.register(ModItems.holotape_damaged, makeData(DIGAMMA, 1_000F));
		// END GENERATED
	}

	public static HazardData makeData() { return new HazardData(); }
	public static HazardData makeData(HazardTypeBase hazard) { return new HazardData().addEntry(hazard); }
	public static HazardData makeData(HazardTypeBase hazard, float level) { return new HazardData().addEntry(hazard, level); }
	public static HazardData makeData(HazardTypeBase hazard, float level, boolean override) { return new HazardData().addEntry(hazard, level, override); }
}
