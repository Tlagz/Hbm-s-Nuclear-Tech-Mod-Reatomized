package com.hbm.lib;

import net.minecraft.resources.ResourceLocation;

public class RefStrings {

	public static final String MODID = "hbm";
	public static final String NAME = "HBM's Nuclear Tech - NeoForge Port";

	public static ResourceLocation loc(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}
