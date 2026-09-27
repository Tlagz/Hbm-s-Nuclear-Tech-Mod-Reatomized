package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineRadGen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Radiation-powered engine: spinning rotor while running, a fullbright status light and tinted glass */
public class RenderRadGen implements BlockEntityRenderer<TileEntityMachineRadGen> {

	public RenderRadGen(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineRadGen radgen, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0D, 0.5D);

		switch(BlockDummyable.getRotation(radgen.getBlockState())) {
		case NORTH: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case WEST: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case SOUTH: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		default: break;
		}

		renderCommon(pose, buffers, light, overlay, radgen.isOn);
		pose.popPose();
	}

	public static void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay, boolean isOn) {
		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.radgen_tex));
		ResourceManager.radgen.renderPart("Base", pose, consumer, light, overlay);

		pose.pushPose();
		if(isOn) {
			pose.translate(0, 1.5, 0);
			pose.mulPose(Axis.XP.rotationDegrees((System.currentTimeMillis() % 3600) * -0.1F));
			pose.translate(0, -1.5, 0);
		}
		ResourceManager.radgen.renderPart("Rotor", pose, consumer, light, overlay);
		pose.popPose();

		// the textured frame of the glass
		ResourceManager.radgen.renderPart("Glass", pose, consumer, light, overlay);

		VertexConsumer lamp = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.white_tex));
		ResourceManager.radgen.renderPart("Light", pose, lamp, LightTexture.FULL_BRIGHT, overlay, 0F, isOn ? 1F : 0.1F, 0F, 1F);

		VertexConsumer glass = buffers.getBuffer(RenderType.entityTranslucent(ResourceManager.white_tex));
		ResourceManager.radgen.renderPart("Glass", pose, glass, light, overlay, 0.5F, 0.75F, 1F, 0.3F);
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineRadGen tile) {
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
				pose.translate(0, -1, 0);
				pose.scale(4.5F, 4.5F, 4.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				pose.translate(0.5, 0, 0);
				RenderRadGen.renderCommon(pose, buffers, light, overlay, false);
			}
		};
	}
}
