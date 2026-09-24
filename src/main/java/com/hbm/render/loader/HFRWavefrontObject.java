package com.hbm.render.loader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.hbm.main.MainRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

/**
 * OBJ model for tile entity / item renderers, same API as the original (renderAll, renderPart, renderOnly,
 * renderAllExcept) but drawing into a VertexConsumer with the current PoseStack instead of the Tessellator.
 * Faces are emitted as quads (triangles repeat their last vertex), so use a QUADS based RenderType
 * like RenderType.entitySolid/entityCutout with the texture the original bound before rendering.
 *
 * Models are parsed lazily on first use and dropped on resource reload.
 */
public class HFRWavefrontObject {

	/** All created models, for clearing their cache on resource reloads */
	private static final Set<HFRWavefrontObject> ALL = Collections.newSetFromMap(new WeakHashMap<>());

	public final ResourceLocation resource;
	private boolean smoothing = true;

	/** Group name -> faces, each face as 4 vertices * (x, y, z, u, v, nx, ny, nz) */
	private Map<String, float[][]> groups;

	public HFRWavefrontObject(ResourceLocation resource) {
		this.resource = resource;
		ALL.add(this);
	}

	/** Flat shading (face normals), like the original's noSmooth() */
	public HFRWavefrontObject noSmooth() {
		this.smoothing = false;
		return this;
	}

	public static void clearCaches() {
		for(HFRWavefrontObject obj : ALL) obj.groups = null;
	}

	public List<String> getPartNames() {
		return new ArrayList<>(getGroups().keySet());
	}

	/// RENDERING ///

	public void renderAll(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
		for(float[][] faces : getGroups().values()) render(pose, consumer, faces, light, overlay, 1F, 1F, 1F, 1F);
	}

	public void renderAll(PoseStack pose, VertexConsumer consumer, int light) {
		renderAll(pose, consumer, light, OverlayTexture.NO_OVERLAY);
	}

	public void renderPart(String partName, PoseStack pose, VertexConsumer consumer, int light, int overlay) {
		float[][] faces = getGroups().get(partName);
		if(faces != null) render(pose, consumer, faces, light, overlay, 1F, 1F, 1F, 1F);
	}

	public void renderPart(String partName, PoseStack pose, VertexConsumer consumer, int light) {
		renderPart(partName, pose, consumer, light, OverlayTexture.NO_OVERLAY);
	}

	/** Tinted version, the original used GL11.glColor before rendering */
	public void renderPart(String partName, PoseStack pose, VertexConsumer consumer, int light, int overlay, float r, float g, float b, float a) {
		float[][] faces = getGroups().get(partName);
		if(faces != null) render(pose, consumer, faces, light, overlay, r, g, b, a);
	}

	/** Tinted with shifted texture coordinates, the original's GL_TEXTURE matrix translation (scrolling textures) */
	public void renderPartShifted(String partName, PoseStack pose, VertexConsumer consumer, int light, int overlay, float r, float g, float b, float a, float du, float dv) {
		float[][] faces = getGroups().get(partName);
		if(faces != null) render(pose, consumer, faces, light, overlay, r, g, b, a, du, dv);
	}

	public void renderOnly(PoseStack pose, VertexConsumer consumer, int light, String... groupNames) {
		for(String name : groupNames) renderPart(name, pose, consumer, light);
	}

	public void renderAllExcept(PoseStack pose, VertexConsumer consumer, int light, String... excludedGroupNames) {
		List<String> excluded = Arrays.asList(excludedGroupNames);
		for(Map.Entry<String, float[][]> group : getGroups().entrySet()) {
			if(!excluded.contains(group.getKey())) render(pose, consumer, group.getValue(), light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		}
	}

	private static void render(PoseStack pose, VertexConsumer consumer, float[][] faces, int light, int overlay, float r, float g, float b, float a) {
		render(pose, consumer, faces, light, overlay, r, g, b, a, 0F, 0F);
	}

	private static void render(PoseStack pose, VertexConsumer consumer, float[][] faces, int light, int overlay, float r, float g, float b, float a, float du, float dv) {
		PoseStack.Pose last = pose.last();
		Matrix4f matrix = last.pose();
		Matrix3f normalMatrix = last.normal();
		Vector3f normal = new Vector3f();

		for(float[] face : faces) {
			for(int i = 0; i < 4; i++) {
				int o = i * 8;
				normal.set(face[o + 5], face[o + 6], face[o + 7]).mul(normalMatrix);
				consumer.addVertex(matrix, face[o], face[o + 1], face[o + 2])
						.setColor(r, g, b, a)
						.setUv(face[o + 3] + du, face[o + 4] + dv)
						.setOverlay(overlay)
						.setLight(light)
						.setNormal(normal.x, normal.y, normal.z);
			}
		}
	}

	/// PARSING ///

	private Map<String, float[][]> getGroups() {
		if(groups == null) groups = load();
		return groups;
	}

	private Map<String, float[][]> load() {
		Map<String, List<float[]>> parsed = new LinkedHashMap<>();
		List<float[]> positions = new ArrayList<>();
		List<float[]> uvs = new ArrayList<>();
		List<float[]> normals = new ArrayList<>();
		String current = "Default";

		Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(resource);
		if(res.isEmpty()) {
			MainRegistry.logger.error("Missing OBJ model " + resource);
			return new LinkedHashMap<>();
		}

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(res.get().open(), StandardCharsets.UTF_8))) {
			String line;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.isEmpty() || line.startsWith("#")) continue;
				String[] parts = line.split("\\s+");

				switch(parts[0]) {
				case "v" -> positions.add(new float[] { f(parts[1]), f(parts[2]), f(parts[3]) });
				case "vt" -> uvs.add(new float[] { f(parts[1]), 1F - f(parts[2]) }); // the original flips v
				case "vn" -> normals.add(new float[] { f(parts[1]), f(parts[2]), f(parts[3]) });
				case "g", "o" -> current = parts.length > 1 ? parts[1] : "Default";
				case "f" -> {
					int count = parts.length - 1;
					if(count < 3 || count > 4) continue;
					float[] face = new float[32];
					float[][] verts = new float[count][];
					for(int i = 0; i < count; i++) {
						String[] idx = parts[i + 1].split("/");
						float[] p = positions.get(Integer.parseInt(idx[0]) - 1);
						float[] uv = idx.length > 1 && !idx[1].isEmpty() ? uvs.get(Integer.parseInt(idx[1]) - 1) : new float[] { 0, 0 };
						float[] n = idx.length > 2 && !idx[2].isEmpty() ? normals.get(Integer.parseInt(idx[2]) - 1) : null;
						verts[i] = new float[] { p[0], p[1], p[2], uv[0], uv[1], n != null ? n[0] : 0, n != null ? n[1] : 0, n != null ? n[2] : 0, n != null ? 1 : 0 };
					}

					float[] faceNormal = faceNormal(verts);
					for(int i = 0; i < 4; i++) {
						float[] v = verts[Math.min(i, count - 1)];
						boolean useVertexNormal = smoothing && v[8] > 0;
						System.arraycopy(v, 0, face, i * 8, 5);
						face[i * 8 + 5] = useVertexNormal ? v[5] : faceNormal[0];
						face[i * 8 + 6] = useVertexNormal ? v[6] : faceNormal[1];
						face[i * 8 + 7] = useVertexNormal ? v[7] : faceNormal[2];
					}
					parsed.computeIfAbsent(current, k -> new ArrayList<>()).add(face);
				}
				default -> { }
				}
			}
		} catch(Exception ex) {
			MainRegistry.logger.error("Could not load OBJ model " + resource, ex);
		}

		Map<String, float[][]> out = new LinkedHashMap<>();
		parsed.forEach((name, faces) -> out.put(name, faces.toArray(new float[0][])));
		return out;
	}

	private static float f(String s) {
		return Float.parseFloat(s);
	}

	private static float[] faceNormal(float[][] v) {
		Vector3f a = new Vector3f(v[1][0] - v[0][0], v[1][1] - v[0][1], v[1][2] - v[0][2]);
		Vector3f b = new Vector3f(v[2][0] - v[0][0], v[2][1] - v[0][1], v[2][2] - v[0][2]);
		a.cross(b);
		if(a.lengthSquared() > 0) a.normalize();
		return new float[] { a.x, a.y, a.z };
	}
}
