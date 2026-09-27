package com.hbm.render.tileentity;

import java.awt.Color;

import com.hbm.blocks.BlockDummyable;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityFEL;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The laser and, while it's on, the beam out to where it hits */
public class RenderFEL implements BlockEntityRenderer<TileEntityFEL> {

	public RenderFEL(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityFEL fel, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0, 0.5D);

		switch(BlockDummyable.getMeta(fel.getBlockState()) - BlockDummyable.offset) {
		case 4: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		ResourceManager.fel.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.fel_tex)), light, overlay);

		int color = fel.mode.renderedBeamColor == 0 ? Color.HSBtoRGB(fel.getLevel().getGameTime() / 50.0F, 0.5F, 0.1F) & 16777215 : fel.mode.renderedBeamColor;
		int length = fel.distance - 3;

		pose.translate(0, 1.5, -1.5);

		if(fel.power > TileEntityFEL.powerReq * Math.pow(2, fel.mode.ordinal()) && fel.isOn && fel.mode != EnumWavelengths.NULL && length > 0) {
			BeamPronter.prontBeam(pose, buffers, new Vec3(0, 0, -length - 1), EnumWaveType.SPIRAL, EnumBeamType.SOLID, color, color, 0, 1, 0F, 2, 0.0625F);
			BeamPronter.prontBeam(pose, buffers, new Vec3(0, 0, -length - 1), EnumWaveType.RANDOM, EnumBeamType.SOLID, color, color, (int) (fel.getLevel().getGameTime() % 1000 / 2), (length / 2) + 1, 0.0625F, 2, 0.0625F);
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityFEL tile) {
		return tile.getRenderBoundingBox();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -1.5, 0);
				pose.scale(2.5F, 2.5F, 2.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				ResourceManager.fel.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.fel_tex)), light, overlay);
			}
		};
	}
}
