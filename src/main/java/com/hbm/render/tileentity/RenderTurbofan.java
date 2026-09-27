package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Turbofan: spinning blades, the exhaust glows with the afterburner on */
public class RenderTurbofan implements BlockEntityRenderer<TileEntityMachineTurbofan> {

	public RenderTurbofan(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineTurbofan turbo, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0D, 0.5D);

		switch(BlockDummyable.getRotation(turbo.getBlockState())) {
		case NORTH: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case WEST: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case SOUTH: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		default: break;
		}

		float spin = turbo.lastSpin + (turbo.spin - turbo.lastSpin) * interp;

		ResourceManager.turbofan.renderPart("Body", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.turbofan_tex)), light, overlay);

		pose.pushPose();
		pose.translate(0, 1.5, 0);
		pose.mulPose(Axis.ZN.rotationDegrees(spin));
		pose.translate(0, -1.5, 0);
		ResourceManager.turbofan.renderPart("Blades", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.turbofan_tex)), light, overlay);
		pose.popPose();

		ResourceManager.turbofan.renderPart("Afterburner", pose, buffers.getBuffer(RenderType.entityCutout(turbo.afterburner == 0 ? ResourceManager.turbofan_back_tex : ResourceManager.turbofan_afterburner_tex)), light, overlay);

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineTurbofan tile) {
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
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.scale(2.25F, 2.25F, 2.25F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.turbofan.renderPart("Body", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.turbofan_tex)), light, overlay);
				ResourceManager.turbofan.renderPart("Blades", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.turbofan_tex)), light, overlay);
				ResourceManager.turbofan.renderPart("Afterburner", pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.turbofan_back_tex)), light, overlay);
			}
		};
	}
}
