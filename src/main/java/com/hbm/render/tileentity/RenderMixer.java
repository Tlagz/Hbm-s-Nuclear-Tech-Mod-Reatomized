package com.hbm.render.tileentity;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineMixer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Mixer: the stirrer spins while it works, the glass fills up in the output fluid's color */
public class RenderMixer implements BlockEntityRenderer<TileEntityMachineMixer> {

	public RenderMixer(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineMixer mixer, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.mixer_tex));
		ResourceManager.mixer.renderPart("Main", pose, buffer, light, overlay);

		pose.pushPose();
		pose.mulPose(Axis.YN.rotationDegrees(mixer.prevRotation + (mixer.rotation - mixer.prevRotation) * interp));
		ResourceManager.mixer.renderPart("Mixer", pose, buffer, light, overlay);
		pose.popPose();

		int totalFill = 0;
		int totalMax = 0;

		for(FluidTank tank : mixer.tanks) {
			if(tank.getTankType() != Fluids.NONE) {
				totalFill += tank.getFill();
				totalMax += tank.getMaxFill();
			}
		}

		if(totalFill > 0) {
			int color = mixer.tanks[2].getTankType().getColor();
			pose.translate(0, 1, 0);
			pose.scale(1F, (float) ((double) totalFill / (double) totalMax * 0.99), 1F);
			pose.translate(0, -1, 0);
			ResourceManager.mixer.renderPart("Fluid", pose, buffers.getBuffer(RenderType.entityTranslucent(ResourceManager.white_tex)), light, overlay,
					((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, 0.75F);
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineMixer tile) {
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
				pose.translate(0, -5, 0);
				pose.scale(5F, 5F, 5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(180));
				VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.mixer_tex));
				ResourceManager.mixer.renderPart("Main", pose, buffer, light, overlay);
				ResourceManager.mixer.renderPart("Mixer", pose, buffer, light, overlay);
			}
		};
	}
}
