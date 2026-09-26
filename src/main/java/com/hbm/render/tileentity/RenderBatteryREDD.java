package com.hbm.render.tileentity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.storage.TileEntityBatteryREDD;
import com.hbm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/**
 * REDD: the wheel spins with the stored energy, its lights glow, yellow light trails follow the lights and the
 * plasma ring scrolls.
 *
 * TODO the plasma sparkle layer and the zaps (BeamPronter)
 */
public class RenderBatteryREDD implements BlockEntityRenderer<TileEntityBatteryREDD> {

	public RenderBatteryREDD(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityBatteryREDD redd, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(redd.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		}

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.battery_redd_tex));
		ResourceManager.battery_redd.renderPart("Base", pose, buffer, light, overlay);

		float speed = redd.getSpeed();

		pose.pushPose();
		pose.translate(0, 5.5, 0);
		pose.mulPose(Axis.XP.rotationDegrees(redd.prevRotation + (redd.rotation - redd.prevRotation) * interp));
		pose.translate(0, -5.5, 0);

		ResourceManager.battery_redd.renderPart("Wheel", pose, buffer, light, overlay);
		ResourceManager.battery_redd.renderPart("Lights", pose, buffer, LightTexture.FULL_BRIGHT, overlay);

		pose.pushPose();
		pose.translate(0, 5.5, 0);
		renderTrails(pose, buffers.getBuffer(RenderType.lightning()), speed);
		pose.popPose();

		renderPlasma(redd, pose, buffers, overlay);

		pose.popPose();
		pose.popPose();
	}

	/** Additive yellow trails behind the 8 lights on both sides of the wheel, longer the faster it spins */
	private void renderTrails(PoseStack pose, VertexConsumer consumer, float speed) {
		double span = speed * 0.75;
		if(span <= 0) return;

		Matrix4f matrix = pose.last().pose();
		double len = 4.25D;
		double width = 0.125D;
		float[] alphas = {0.75F, 0.5F, 0.25F, 0F};

		for(int j = -1; j <= 1; j += 2) {
			float xOffset = 0.8125F * j;

			for(int i = 0; i < 8; i++) {
				Vector3f vec = new Vector3f(0, 1, 0).rotateX((float) Math.toRadians(i * 45D));

				for(int step = 0; step < 3; step++) {
					Vector3f next = new Vector3f(vec).rotateX((float) Math.toRadians(span));
					float a0 = alphas[step], a1 = alphas[step + 1];
					if(step == 0) a1 = 0.5F;
					if(step == 1) { a0 = 0.5F; a1 = 0.25F; }
					if(step == 2) { a0 = 0.25F; a1 = 0F; }

					consumer.addVertex(matrix, xOffset, (float) (vec.y * (len - width)), (float) (vec.z * (len - width))).setColor(1F, 1F, 0F, a0);
					consumer.addVertex(matrix, xOffset, (float) (vec.y * (len + width)), (float) (vec.z * (len + width))).setColor(1F, 1F, 0F, a0);
					consumer.addVertex(matrix, xOffset, (float) (next.y * (len + width)), (float) (next.z * (len + width))).setColor(1F, 1F, 0F, a1);
					consumer.addVertex(matrix, xOffset, (float) (next.y * (len - width)), (float) (next.z * (len - width))).setColor(1F, 1F, 0F, a1);

					vec = next;
				}
			}
		}
	}

	/** The plasma ring, a scrolling pink glow that fades in with the speed */
	private void renderPlasma(TileEntityBatteryREDD redd, PoseStack pose, MultiBufferSource buffers, int overlay) {
		float alphaMult = redd.getSpeed() / 15F;
		if(alphaMult <= 0) return;

		long time = System.currentTimeMillis();
		float alpha = 0.45F + (float) (Math.sin(time / 1000D) * 0.15F);
		double mainOsc = BobMathUtil.sps(time / 1000D) % 1D;

		float a = alpha * alphaMult;
		ResourceManager.battery_redd.renderPartShifted("Plasma", pose, buffers.getBuffer(RenderType.eyes(ResourceManager.fusion_plasma_tex)), LightTexture.FULL_BRIGHT, overlay,
				1.0F * a, 0.25F * a, 0.75F * a, a, 0F, (float) mainOsc);
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityBatteryREDD tile) {
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
				pose.translate(0, -3, 0);
				pose.scale(2.5F, 2.5F, 2.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(-90));
				pose.scale(0.5F, 0.5F, 0.5F);
				VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.battery_redd_tex));
				ResourceManager.battery_redd.renderPart("Base", pose, buffer, light, overlay);
				ResourceManager.battery_redd.renderPart("Wheel", pose, buffer, light, overlay);
				ResourceManager.battery_redd.renderPart("Lights", pose, buffer, LightTexture.FULL_BRIGHT, overlay);
			}
		};
	}
}
