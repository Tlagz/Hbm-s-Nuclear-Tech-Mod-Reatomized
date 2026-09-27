package com.hbm.render.tileentity;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntitySolarMirror;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * Heliostat mirror, the original's RenderMirror ISBRH: the stand plus the mirror turned towards its target (flat
 * when it has none), drawn with the block's own texture.
 */
public class RenderSolarMirror implements BlockEntityRenderer<TileEntitySolarMirror> {

	public static final ResourceLocation texture = RefStrings.loc("textures/blocks/solar_mirror.png");

	public RenderSolarMirror(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntitySolarMirror mirror, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(texture));

		pose.pushPose();
		pose.translate(0.5D, 0D, 0.5D);
		ResourceManager.solar_mirror.renderPart("Base", pose, consumer, light, overlay);

		if(mirror.tY > mirror.getBlockPos().getY()) {
			int dx = mirror.tX - mirror.getBlockPos().getX();
			int dy = mirror.tY - mirror.getBlockPos().getY();
			int dz = mirror.tZ - mirror.getBlockPos().getZ();

			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
			double pitch = -Math.asin(dy / dist) + Math.PI / 2D;
			double yaw = -Math.atan2(dz, dx) - Math.PI / 2D;

			// the original rotated the vertices around X, then Y, around the pivot one block up
			pose.translate(0, 1, 0);
			pose.mulPose(Axis.YP.rotation((float) yaw));
			pose.mulPose(Axis.XP.rotation((float) -pitch));
			pose.translate(0, -1, 0);
		}

		ResourceManager.solar_mirror.renderPart("Mirror", pose, consumer, light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntitySolarMirror tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -3, 0);
				pose.scale(8F, 8F, 8F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(texture));
				ResourceManager.solar_mirror.renderPart("Base", pose, consumer, light, overlay);
				pose.translate(0, 1, 0);
				pose.mulPose(Axis.ZN.rotationDegrees(45));
				pose.translate(0, -1, 0);
				ResourceManager.solar_mirror.renderPart("Mirror", pose, consumer, light, overlay);
			}
		};
	}
}
