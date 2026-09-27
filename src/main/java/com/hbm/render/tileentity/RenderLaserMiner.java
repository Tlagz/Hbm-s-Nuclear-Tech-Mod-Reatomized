package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityMachineMiningLaser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Mining laser: the emitter turns and tilts towards the block being mined, three red spiral beams while it's cutting */
public class RenderLaserMiner implements BlockEntityRenderer<TileEntityMachineMiningLaser> {

	public RenderLaserMiner(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineMiningLaser laser, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, -1, 0.5);

		double tx = (laser.targetX - laser.lastTargetX) * interp + laser.lastTargetX;
		double ty = (laser.targetY - laser.lastTargetY) * interp + laser.lastTargetY;
		double tz = (laser.targetZ - laser.lastTargetZ) * interp + laser.lastTargetZ;
		double vx = tx - laser.getBlockPos().getX();
		double vy = ty - laser.getBlockPos().getY() + 3;
		double vz = tz - laser.getBlockPos().getZ();

		Vec3 nVec = new Vec3(vx, vy, vz).normalize().scale(1.5D);
		Vec3 vec = new Vec3(vx - nVec.x, vy - nVec.y, vz - nVec.z);

		double yaw = Math.toDegrees(Math.atan2(vec.x, vec.z));
		double sqrt = Mth.sqrt((float) (vec.x * vec.x + vec.z * vec.z));
		double pitch = Math.toDegrees(Math.atan2(vec.y, sqrt));

		ResourceManager.mining_laser.renderPart("Base", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.mining_laser_base_tex)), light, overlay);

		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees((float) yaw));
		ResourceManager.mining_laser.renderPart("Pivot", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.mining_laser_pivot_tex)), light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees((float) yaw));
		pose.translate(0, -1, 0);
		pose.mulPose(Axis.XN.rotationDegrees((float) (pitch + 90)));
		pose.translate(0, 1, 0);
		ResourceManager.mining_laser.renderPart("Laser", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.mining_laser_laser_tex)), light, overlay);
		pose.popPose();

		if(laser.beam) {
			double length = vec.length();
			pose.translate(nVec.x, nVec.y - 1, nVec.z);
			int range = (int) Math.ceil(length * 0.5);
			int start = (int) laser.getLevel().getGameTime() * -25 % 360;
			for(int i = 0; i < 3; i++) {
				BeamPronter.prontBeam(pose, buffers, vec, EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xa00000, 0xa00000, start + i * 120, range * 2, 0.075F, 3, 0.025F);
			}
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineMiningLaser tile) {
		return AABB.INFINITE;
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -1, 0);
				pose.scale(4F, 4F, 4F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.translate(0, -1, 0);
				ResourceManager.mining_laser.renderPart("Base", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.mining_laser_base_tex)), light, overlay);
				ResourceManager.mining_laser.renderPart("Pivot", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.mining_laser_pivot_tex)), light, overlay);
				ResourceManager.mining_laser.renderPart("Laser", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.mining_laser_laser_tex)), light, overlay);
			}
		};
	}
}
