package com.hbm.items.machine;

/**
 * Chemical dyes and crayons, one item per color (ModItems.chemical_dye, ModItems.crayon), the overlay layer tinted with
 * the color. Both are registered as the matching dye (dyeRed -> c:dyes/red) like the original's ore dictionary entries.
 */
public class ItemChemicalDye {

	public static enum EnumChemDye {
		BLACK(1973019, "Black"),
		RED(11743532, "Red"),
		GREEN(3887386, "Green"),
		BROWN(5320730, "Brown"),
		BLUE(2437522, "Blue"),
		PURPLE(8073150, "Purple"),
		CYAN(2651799, "Cyan"),
		SILVER(11250603, "LightGray"),
		GRAY(4408131, "Gray"),
		PINK(14188952, "Pink"),
		LIME(4312372, "Lime"),
		YELLOW(14602026, "Yellow"),
		LIGHTBLUE(6719955, "LightBlue"),
		MAGENTA(12801229, "Magenta"),
		ORANGE(15435844, "Orange"),
		WHITE(15790320, "White");

		public int color;
		public String dictName;

		private EnumChemDye(int color, String name) {
			this.color = color;
			this.dictName = name;
		}
	}
}
