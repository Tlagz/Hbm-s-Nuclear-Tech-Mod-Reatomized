package com.hbm.render.tileentity;

import org.joml.Matrix4f;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Base (and the frame if something is on top), the two carriages with a striker and a saw arm each, the recipe items
 * on the four pedestals and the sparks flying off the saw blades while they cut.
 */
public class RenderAssemblyFactory implements BlockEntityRenderer<TileEntityMachineAssemblyFactory> {

	public RenderAssemblyFactory(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineAssemblyFactory assemfac, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(90));

		switch(BlockDummyable.getMeta(assemfac.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.assembly_factory_tex));
		ResourceManager.assembly_factory.renderPart("Base", pose, consumer, light, overlay);
		if(assemfac.frame) ResourceManager.assembly_factory.renderPart("Frame", pose, consumer, light, overlay);

		double slide1 = assemfac.animations[0].getSlider(interp);
		double slide2 = assemfac.animations[1].getSlider(interp);
		double[] arm1 = assemfac.animations[0].striker.getPositions(interp);
		double[] arm2 = assemfac.animations[0].saw.getPositions(interp);
		double[] arm3 = assemfac.animations[1].striker.getPositions(interp);
		double[] arm4 = assemfac.animations[1].saw.getPositions(interp);

		renderArm(pose, consumer, light, overlay, 1, 0.5 - slide1, -1, arm1, false);
		renderArm(pose, consumer, light, overlay, 2, -0.5 + slide1, 1, arm2, true);
		renderArm(pose, consumer, light, overlay, 3, -0.5 + slide2, 1, arm3, false);
		renderArm(pose, consumer, light, overlay, 4, 0.5 - slide2, -1, arm4, true);

		var player = Minecraft.getInstance().player;
		if(player != null && player.distanceToSqr(assemfac.getBlockPos().getX() + 0.5, assemfac.getBlockPos().getY() + 1, assemfac.getBlockPos().getZ() + 0.5) < 35 * 35) {

			for(int i = 0; i < 4; i++) {
				GenericRecipe recipe = assemfac.assemblerModule[i].getRecipe();
				if(recipe == null) continue;

				pose.pushPose();
				pose.translate(1.5 - i, 0, 0);
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.translate(0, 1.0625, 0);

				ItemStack stack = recipe.getIcon().copyWithCount(1);

				if(stack.getItem() instanceof BlockItem) {
					// a block standing on the pedestal
					pose.translate(0, 0.25, 0);
				} else {
					// flat items lie on the pedestal
					pose.translate(0, 0.02, 0);
					pose.mulPose(Axis.XP.rotationDegrees(90));
				}
				pose.scale(0.625F, 0.625F, 0.625F);

				Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, assemfac.getLevel(), 0);
				pose.popPose();
			}

			// two layers of sparks, one with regular UV and one mirrored with +0.5 offset, left and right of the blade
			VertexConsumer sparks = buffers.getBuffer(RenderType.entityTranslucent(ResourceManager.assembly_factory_sparks_tex));
			float uMin = (float) ((assemfac.getLevel().getGameTime() / 10D + interp) % 10);

			if(arm2[3] <= -0.375D) {
				pose.pushPose();
				pose.translate(0.5 + slide1, 1.0625D, -arm2[2] / 45D); // arm angle/45 is a seemingly good enough approximation
				renderSparks(sparks, pose, uMin, 1.25F);
				pose.popPose();
			}

			if(arm4[3] <= -0.375D) {
				pose.pushPose();
				pose.translate(-0.5 - slide2, 1.0625D, arm4[2] / 45D);
				renderSparks(sparks, pose, uMin, -1.25F);
				pose.popPose();
			}
		}

		pose.popPose();
	}

	/**
	 * One arm on its slider. side is -1 for the arms on the -Z half and 1 for the +Z half, which flips the pivots and
	 * the rotation direction.
	 */
	private static void renderArm(PoseStack pose, VertexConsumer consumer, int light, int overlay, int n, double slide, int side, double[] arm, boolean saw) {
		pose.pushPose();
		pose.translate(slide, 0, 0);
		ResourceManager.assembly_factory.renderPart("Slider" + n, pose, consumer, light, overlay);

		rotateAround(pose, 1.625, side * 0.9375, side * arm[0]);
		ResourceManager.assembly_factory.renderPart("ArmLower" + n, pose, consumer, light, overlay);

		rotateAround(pose, 2.375, side * 0.9375, side * arm[1]);
		ResourceManager.assembly_factory.renderPart("ArmUpper" + n, pose, consumer, light, overlay);

		rotateAround(pose, 2.375, side * 0.4375, side * arm[2]);
		ResourceManager.assembly_factory.renderPart("Head" + n, pose, consumer, light, overlay);

		pose.translate(0, arm[3], 0);
		ResourceManager.assembly_factory.renderPart("Striker" + n, pose, consumer, light, overlay);

		if(saw) {
			rotateAround(pose, 1.625, side * 0.3125, -side * arm[4]);
			ResourceManager.assembly_factory.renderPart("Blade" + n, pose, consumer, light, overlay);
		}

		pose.popPose();
	}

	/** glTranslated(0, y, z); glRotated(angle, 1, 0, 0); glTranslated(0, -y, -z) */
	private static void rotateAround(PoseStack pose, double y, double z, double angle) {
		pose.translate(0, y, z);
		pose.mulPose(Axis.XP.rotationDegrees((float) angle));
		pose.translate(0, -y, -z);
	}

	private static void renderSparks(VertexConsumer consumer, PoseStack pose, float uMin, float length) {
		float wide = 0.1875F;
		float narrow = 0F;
		float uMax = uMin + 1;
		float epsilon = 0.01F;

		Matrix4f matrix = pose.last().pose();
		vertex(consumer, matrix, pose, -epsilon, -wide, length, uMin + 0.5F, 0, 0F);
		vertex(consumer, matrix, pose, -epsilon, wide, length, uMin + 0.5F, 1, 0F);
		vertex(consumer, matrix, pose, -epsilon, narrow, 0, uMax + 0.5F, 1, 1F);
		vertex(consumer, matrix, pose, -epsilon, -narrow, 0, uMax + 0.5F, 0, 1F);

		vertex(consumer, matrix, pose, epsilon, -wide, length, uMin, 1, 0F);
		vertex(consumer, matrix, pose, epsilon, wide, length, uMin, 0, 0F);
		vertex(consumer, matrix, pose, epsilon, narrow, 0, uMax, 0, 1F);
		vertex(consumer, matrix, pose, epsilon, -narrow, 0, uMax, 1, 1F);
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrix, PoseStack pose, float x, float y, float z, float u, float v, float alpha) {
		consumer.addVertex(matrix, x, y, z).setColor(1F, 1F, 1F, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 1, 0, 0);
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineAssemblyFactory tile) {
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
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.scale(0.75F, 0.75F, 0.75F);
				ResourceManager.assembly_factory.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.assembly_factory_tex)), light, overlay);
			}
		};
	}
}
