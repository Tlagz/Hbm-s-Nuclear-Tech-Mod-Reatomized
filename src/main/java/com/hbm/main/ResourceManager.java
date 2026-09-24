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
	public static final HFRWavefrontObject refinery = new HFRWavefrontObject(RefStrings.loc("models/refinery.obj"));
	public static final HFRWavefrontObject refinery_exploded = new HFRWavefrontObject(RefStrings.loc("models/refinery_exploded.obj"));
	public static final ResourceLocation refinery_tex = RefStrings.loc("textures/models/refinery.png");
	public static final HFRWavefrontObject derrick = new HFRWavefrontObject(RefStrings.loc("models/machines/derrick.obj"));
	public static final ResourceLocation derrick_tex = RefStrings.loc("textures/models/machines/derrick.png");
	public static final HFRWavefrontObject dieselgen = new HFRWavefrontObject(RefStrings.loc("models/machines/dieselgen.obj"));
	public static final ResourceLocation dieselgen_tex = RefStrings.loc("textures/models/machines/dieselgen.png");
	public static final ResourceLocation wood_burner_tex = RefStrings.loc("textures/models/machines/wood_burner.png");
}
