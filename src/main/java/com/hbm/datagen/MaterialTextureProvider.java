package com.hbm.datagen;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import com.google.common.hash.Hashing;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemAutogen.AutogenItems;

import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/**
 * Textures of the autogen items: the shape's grey texture remapped to the material's solid colors. The original
 * did this at runtime (RGBMutatorInterpolatedComponentRemap on a TextureAtlasSpriteMutatable), here the results
 * are written to textures/items/autogen and used like any other texture.
 */
public class MaterialTextureProvider implements DataProvider {

	/** The grey range of the base textures that gets mapped onto the material's light and dark color */
	public static final int SOURCE_LIGHT = 0xFFFFFF;
	public static final int SOURCE_DARK = 0x505050;

	private final PackOutput output;

	public MaterialTextureProvider(PackOutput output) {
		this.output = output;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		List<CompletableFuture<?>> futures = new ArrayList<>();
		Path root = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve("hbm/textures");

		for(AutogenItems set : ModItems.AUTOGEN) {
			BufferedImage base = read("/assets/hbm/textures/" + set.baseTexture + ".png");

			for(NTMMaterial mat : set.materials()) {
				if(!set.isGenerated(mat)) continue;
				byte[] png = png(remap(base, SOURCE_LIGHT, SOURCE_DARK, mat.solidColorLight, mat.solidColorDark));
				Path path = root.resolve(set.texture(mat) + ".png");
				futures.add(CompletableFuture.runAsync(() -> {
					try {
						cache.writeIfNeeded(path, png, Hashing.sha1().hashBytes(png));
					} catch(IOException ex) {
						throw new UncheckedIOException(ex);
					}
				}, Util.backgroundExecutor()));
			}
		}

		// dynamic slag in the colors of materials with two solid colors, see RenderSlag
		BufferedImage slag = read("/assets/hbm/textures/blocks/slag.png");
		for(NTMMaterial mat : com.hbm.inventory.material.Mats.orderedList) {
			if(!com.hbm.tileentity.machine.TileEntitySlag.hasOwnTexture(mat)) continue;
			byte[] png = png(remap(slag, SOURCE_LIGHT, SOURCE_DARK, mat.solidColorLight, mat.solidColorDark));
			Path path = root.resolve(com.hbm.tileentity.machine.TileEntitySlag.texturePath(mat) + ".png");
			futures.add(CompletableFuture.runAsync(() -> {
				try {
					cache.writeIfNeeded(path, png, Hashing.sha1().hashBytes(png));
				} catch(IOException ex) {
					throw new UncheckedIOException(ex);
				}
			}, Util.backgroundExecutor()));
		}

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	private static BufferedImage read(String resource) {
		try(InputStream in = MaterialTextureProvider.class.getResourceAsStream(resource)) {
			if(in == null) throw new IllegalStateException("Missing autogen base texture " + resource);
			return ImageIO.read(in);
		} catch(IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static byte[] png(BufferedImage image) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ImageIO.write(image, "png", bytes);
			return bytes.toByteArray();
		} catch(IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/** The original's RGBMutatorInterpolatedComponentRemap: each channel is mapped linearly from the source range to the target range */
	public static BufferedImage remap(BufferedImage image, int boundLighter, int boundDarker, int lighter, int darker) {
		BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				out.setRGB(x, y, shiftColor(boundLighter, boundDarker, lighter, darker, image.getRGB(x, y)));
			}
		}
		return out;
	}

	private static int shiftColor(int boundLighter, int boundDarker, int lighter, int darker, int pix) {

		int a = (pix & 0xff000000) >> 24;
		int r = (pix & 0xff0000) >> 16;
		int g = (pix & 0xff00) >> 8;
		int b = (pix & 0xff);

		int nR = (int) shiftComponent(comp(lighter, 16), comp(darker, 16), comp(boundLighter, 16), comp(boundDarker, 16), r);
		int nG = (int) shiftComponent(comp(lighter, 8), comp(darker, 8), comp(boundLighter, 8), comp(boundDarker, 8), g);
		int nB = (int) shiftComponent(comp(lighter, 0), comp(darker, 0), comp(boundLighter, 0), comp(boundDarker, 0), b);

		r = nR & 0xff;
		g = nG & 0xff;
		b = nB & 0xff;

		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static double shiftComponent(int lighter, int darker, int boundLighter, int boundDarker, int component) {
		double scaled = (component - (double) boundLighter) / ((double) boundDarker - boundLighter);
		return lighter + scaled * (darker - (double) lighter);
	}

	private static int comp(int color, int shift) {
		return (color >> shift) & 0xff;
	}

	@Override
	public String getName() {
		return "HBM material textures";
	}
}
