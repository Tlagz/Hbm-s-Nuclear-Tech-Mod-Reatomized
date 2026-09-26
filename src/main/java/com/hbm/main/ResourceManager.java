package com.hbm.main;

import com.hbm.lib.RefStrings;
import com.hbm.render.loader.HFRWavefrontObject;

import net.minecraft.resources.ResourceLocation;

/**
 * Client side models and textures, same field names as the original's ResourceManager.
 * Paths are lowercased (see tools/asset_rename_map.txt).
 */
public class ResourceManager {

	//// MODELS ////
	public static final HFRWavefrontObject wood_burner = new HFRWavefrontObject(RefStrings.loc("models/machines/wood_burner.obj"));

	//// TEXTURES ////
	public static final HFRWavefrontObject heater_firebox = new HFRWavefrontObject(RefStrings.loc("models/machines/firebox.obj"));
	public static final ResourceLocation heater_firebox_tex = RefStrings.loc("textures/models/machines/firebox.png");
	public static final HFRWavefrontObject boiler = new HFRWavefrontObject(RefStrings.loc("models/machines/boiler.obj"));
	public static final HFRWavefrontObject boiler_burst = new HFRWavefrontObject(RefStrings.loc("models/machines/boiler_burst.obj"));
	public static final ResourceLocation boiler_tex = RefStrings.loc("textures/models/machines/boiler.png");
	public static final HFRWavefrontObject press_body = new HFRWavefrontObject(RefStrings.loc("models/press_body.obj"));
	public static final HFRWavefrontObject press_head = new HFRWavefrontObject(RefStrings.loc("models/press_head.obj"));
	public static final ResourceLocation press_body_tex = RefStrings.loc("textures/models/press_body.png");
	public static final ResourceLocation press_head_tex = RefStrings.loc("textures/models/press_head.png");
	public static final HFRWavefrontObject refinery = new HFRWavefrontObject(RefStrings.loc("models/refinery.obj"));
	public static final HFRWavefrontObject refinery_exploded = new HFRWavefrontObject(RefStrings.loc("models/refinery_exploded.obj"));
	public static final ResourceLocation refinery_tex = RefStrings.loc("textures/models/refinery.png");
	public static final HFRWavefrontObject derrick = new HFRWavefrontObject(RefStrings.loc("models/machines/derrick.obj"));
	public static final ResourceLocation derrick_tex = RefStrings.loc("textures/models/machines/derrick.png");
	public static final HFRWavefrontObject dieselgen = new HFRWavefrontObject(RefStrings.loc("models/machines/dieselgen.obj"));
	public static final ResourceLocation dieselgen_tex = RefStrings.loc("textures/models/machines/dieselgen.png");
	public static final ResourceLocation wood_burner_tex = RefStrings.loc("textures/models/machines/wood_burner.png");
	public static final HFRWavefrontObject assembly_machine = new HFRWavefrontObject(RefStrings.loc("models/machines/assembly_machine.obj"));
	public static final ResourceLocation assembly_machine_tex = RefStrings.loc("textures/models/machines/assembly_machine.png");
	public static final HFRWavefrontObject chemical_plant = new HFRWavefrontObject(RefStrings.loc("models/machines/chemical_plant.obj"));
	public static final ResourceLocation chemical_plant_tex = RefStrings.loc("textures/models/machines/chemical_plant.png");
	public static final HFRWavefrontObject arc_welder = new HFRWavefrontObject(RefStrings.loc("models/machines/arc_welder.obj")).noSmooth();
	public static final ResourceLocation arc_welder_tex = RefStrings.loc("textures/models/machines/arc_welder.png");
	public static final HFRWavefrontObject stirling = new HFRWavefrontObject(RefStrings.loc("models/machines/stirling.obj"));
	public static final ResourceLocation stirling_tex = RefStrings.loc("textures/models/machines/stirling.png");
	public static final ResourceLocation stirling_steel_tex = RefStrings.loc("textures/models/machines/stirling_steel.png");
	public static final ResourceLocation stirling_creative_tex = RefStrings.loc("textures/models/machines/stirling_creative.png");
	public static final HFRWavefrontObject heater_oven = new HFRWavefrontObject(RefStrings.loc("models/machines/heating_oven.obj")).noSmooth();
	public static final ResourceLocation heater_oven_tex = RefStrings.loc("textures/models/machines/heating_oven.png");
	public static final HFRWavefrontObject crucible_heat = new HFRWavefrontObject(RefStrings.loc("models/machines/crucible.obj"));
	public static final ResourceLocation crucible_tex = RefStrings.loc("textures/models/machines/crucible_heat.png");
	public static final HFRWavefrontObject arc_furnace = new HFRWavefrontObject(RefStrings.loc("models/machines/arc_furnace.obj"));
	public static final ResourceLocation arc_furnace_tex = RefStrings.loc("textures/models/machines/arc_furnace.png");
	public static final HFRWavefrontObject blast_furnace = new HFRWavefrontObject(RefStrings.loc("models/machines/blast_furnace.obj")).noSmooth();
	public static final ResourceLocation blast_furnace_tex = RefStrings.loc("textures/models/machines/blast_furnace.png");
	public static final HFRWavefrontObject battery_socket = new HFRWavefrontObject(RefStrings.loc("models/machines/battery.obj"));
	public static final ResourceLocation battery_socket_tex = RefStrings.loc("textures/models/machines/battery_socket.png");
	public static final ResourceLocation chemical_plant_fluid_tex = RefStrings.loc("textures/models/machines/chemical_plant_fluid.png");
}
