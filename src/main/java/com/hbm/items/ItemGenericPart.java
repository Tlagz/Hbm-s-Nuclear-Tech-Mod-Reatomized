package com.hbm.items;

/** Generic parts, one item per type (ModItems.part_generic), each with its own texture name */
public class ItemGenericPart {

	public static enum EnumPartType {
		PISTON_PNEUMATIC("piston_pneumatic"),
		PISTON_HYDRAULIC("piston_hydraulic"),
		PISTON_ELECTRIC("piston_electric"),
		LDE("low_density_element"),
		HDE("heavy_duty_element"),
		GLASS_POLARIZED("glass_polarized");

		public final String texName;

		private EnumPartType(String texName) {
			this.texName = texName;
		}
	}
}
