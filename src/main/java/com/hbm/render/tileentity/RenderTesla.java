package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityTesla;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Tesla coil with a lightning beam to every zapped target */
public class RenderTesla implements BlockEntityRenderer<TileEntityTesla> {

	public RenderTesla(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityTesla tesla, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0D, 0.5D);

		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(180));
		ResourceManager.tesla.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.tesla_tex)), light, overlay);
		pose.popPose();

		double sx = tesla.getBlockPos().getX() + 0.5D;
		double sy = tesla.getBlockPos().getY() + TileEntityTesla.offset;
		double sz = tesla.getBlockPos().getZ() + 0.5D;

		pose.translate(0.0D, TileEntityTesla.offset, 0.0D);
		for(double[] target : tesla.targets) {

			double length = Math.sqrt(Math.pow(target[0] - sx, 2) + Math.pow(target[1] - sy, 2) + Math.pow(target[2] - sz, 2));

			// the original rendered inside a 180 degree turn, hence the flipped x and z
			BeamPronter.prontBeam(pose, buffers, new Vec3(target[0] - sx, target[1] - sy, target[2] - sz), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x404040, 0x404040,
					(int) tesla.getLevel().getGameTime() % 1000 + 1, (int) (length * 5), 0.125F, 2, 0.03125F);
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityTesla tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -3, 0);
				pose.scale(6F, 6F, 6F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.tesla.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.tesla_tex)), light, overlay);
			}
		};
	}
}
