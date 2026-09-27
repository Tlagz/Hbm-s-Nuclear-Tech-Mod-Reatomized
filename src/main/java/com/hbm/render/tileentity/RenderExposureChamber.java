package com.hbm.render.tileentity;

import java.util.Random;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityMachineExposureChamber;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The chamber, the spinning magnets, the glowing core and the particle beams while running */
public class RenderExposureChamber implements BlockEntityRenderer<TileEntityMachineExposureChamber> {

	public RenderExposureChamber(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineExposureChamber chamber, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0, 0.5D);

		switch(BlockDummyable.getMeta(chamber.getBlockState()) - BlockDummyable.offset) {
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		}

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.exposure_chamber_tex));
		ResourceManager.exposure_chamber.renderPart("Chamber", pose, consumer, light, overlay);

		double rotation = chamber.prevRotation + (chamber.rotation - chamber.prevRotation) * interp;

		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees((float) rotation));
		ResourceManager.exposure_chamber.renderPart("Magnets", pose, consumer, light, overlay);
		pose.popPose();

		if(chamber.isOn) {
			long time = chamber.getLevel().getGameTime();

			pose.pushPose();
			pose.mulPose(Axis.YP.rotationDegrees((float) (rotation / 2D)));
			pose.translate(0, Math.sin((time % (Math.PI * 16D) + interp) * 0.125) * 0.0625, 0);
			ResourceManager.exposure_chamber.renderPart("Core", pose, consumer, LightTexture.FULL_BRIGHT, overlay);
			pose.popPose();

			int duration = 8;
			Random rand = new Random(time / duration);
			int chance = 2;
			int color = time % duration >= duration / 2 ? 0x80d0ff : 0xffffff;
			int start = (int) (System.currentTimeMillis() % 1000) / 50;
			rand.nextInt(chance); // RNG behaves weirdly in the first iteration

			if(rand.nextInt(chance) == 0) beam(pose, buffers, 0, 3.675, -7.5, new Vec3(0, 0, 5), EnumWaveType.RANDOM, color, start, 15);
			if(rand.nextInt(chance) == 0) beam(pose, buffers, 1.1875, 2.5, -7.5, new Vec3(0, 0, 5), EnumWaveType.RANDOM, color, start, 15);
			if(rand.nextInt(chance) == 0) beam(pose, buffers, -1.1875, 2.5, -7.5, new Vec3(0, 0, 5), EnumWaveType.RANDOM, color, start, 15);

			beam(pose, buffers, 0, 1.75, 0, new Vec3(0, 1.5, 0), EnumWaveType.RANDOM, 0x80d0ff, start, 10);
			beam(pose, buffers, 0, 1.75, 0, new Vec3(0, 1.5, 0), EnumWaveType.RANDOM, 0x8080ff, (int) (System.currentTimeMillis() + 5 % 1000) / 50, 10);

			int spiral = (int) (System.currentTimeMillis() % 360);
			beam(pose, buffers, 0, 2.5, 0, new Vec3(0, 0, -1), EnumWaveType.SPIRAL, 0xffff80, spiral, 15);
			beam(pose, buffers, 0, 2.5, 0, new Vec3(0, 0, -1), EnumWaveType.SPIRAL, 0xff8080, spiral + 180, 15);
		}

		pose.popPose();
	}

	private static void beam(PoseStack pose, MultiBufferSource buffers, double x, double y, double z, Vec3 skeleton, EnumWaveType wave, int color, int start, int segments) {
		pose.pushPose();
		pose.translate(x, y, z);
		BeamPronter.prontBeam(pose, buffers, skeleton, wave, EnumBeamType.LINE, color, 0xffffff, start, segments, 0.125F, 1, 0);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineExposureChamber tile) {
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
				pose.scale(3F, 3F, 3F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.translate(1.5, 0, 0);
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.scale(0.5F, 0.5F, 0.5F);
				ResourceManager.exposure_chamber.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.exposure_chamber_tex)), light, overlay);
			}
		};
	}
}
