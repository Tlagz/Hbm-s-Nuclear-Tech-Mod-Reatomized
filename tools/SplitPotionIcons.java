import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Cuts the 1.7.10 potion icon sheet (textures/gui/potions.png, 18x18 icons starting at y=198, same layout as
 * vanilla's inventory.png) into the per-effect textures 1.20+ expects in textures/mob_effect.
 *
 * Usage (from the project root): java tools/SplitPotionIcons.java
 */
public class SplitPotionIcons {

	public static void main(String[] args) throws Exception {
		File assets = new File("src/main/resources/assets/hbm/textures");
		BufferedImage sheet = ImageIO.read(new File(assets, "gui/potions.png"));
		File out = new File(assets, "mob_effect");
		out.mkdirs();

		// name -> icon index x, y (from HbmPotion.init in the original)
		Map<String, int[]> icons = Map.ofEntries(
				Map.entry("taint", new int[] {0, 0}),
				Map.entry("radiation", new int[] {1, 0}),
				Map.entry("mutation", new int[] {2, 0}),
				Map.entry("bang", new int[] {3, 0}),
				Map.entry("radx", new int[] {5, 0}),
				Map.entry("lead", new int[] {6, 0}),
				Map.entry("radaway", new int[] {7, 0}),
				Map.entry("phosphorus", new int[] {1, 1}),
				Map.entry("stability", new int[] {2, 1}),
				Map.entry("potionsickness", new int[] {3, 1}),
				Map.entry("death", new int[] {4, 1}));

		for(Map.Entry<String, int[]> e : icons.entrySet()) {
			BufferedImage icon = sheet.getSubimage(e.getValue()[0] * 18, 198 + e.getValue()[1] * 18, 18, 18);
			ImageIO.write(icon, "png", new File(out, e.getKey() + ".png"));
		}
		System.out.println("wrote " + icons.size() + " icons to " + out);
	}
}
