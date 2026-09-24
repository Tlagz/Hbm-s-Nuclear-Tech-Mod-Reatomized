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
	public static final ResourceLocation wood_burner_tex = RefStrings.loc("textures/models/machines/wood_burner.png");
}
