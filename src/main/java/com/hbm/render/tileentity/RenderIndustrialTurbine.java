package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineIndustrialTurbine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Industrial turbine with the steam grade gauge and the spinning flywheel */
public class RenderIndustrialTurbine implements BlockEntityRenderer<TileEntityMachineIndustrialTurbine> {

	public RenderIndustrialTurbine(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineIndustrialTurbine turbine, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(turbine.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		}

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.industrial_turbine_tex));
		ResourceManager.industrial_turbine.renderPart("Turbine", pose, buffer, light, overlay);

		pose.pushPose();
		pose.translate(0, 1.5, 0);
		pose.mulPose(Axis.ZP.rotationDegrees(135 - (turbine.tanks[0].getTankType().getID() - Fluids.STEAM.getID()) * 90));
		pose.translate(0, -1.5, 0);
		ResourceManager.industrial_turbine.renderPart("Gauge", pose, buffer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(0, 1.5, 0);
		pose.mulPose(Axis.ZN.rotationDegrees(turbine.lastRotor + (turbine.rotor - turbine.lastRotor) * interp));
		pose.translate(0, -1.5, 0);
		ResourceManager.industrial_turbine.renderPart("Flywheel", pose, buffer, light, overlay);
		pose.popPose();

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineIndustrialTurbine tile) {
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
				pose.translate(1, 0, 0);
				pose.scale(3F, 3F, 3F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.scale(0.75F, 0.75F, 0.75F);
				pose.translate(0.5, 0, 0);
				VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.industrial_turbine_tex));

				ResourceManager.industrial_turbine.renderPart("Turbine", pose, buffer, light, overlay);

				pose.translate(0, 1.5, 0);
				pose.mulPose(Axis.ZP.rotationDegrees(135));
				pose.translate(0, -1.5, 0);
				ResourceManager.industrial_turbine.renderPart("Gauge", pose, buffer, light, overlay);

				double rot = (System.currentTimeMillis() / 5) % 336D;
				pose.translate(0, 1.5, 0);
				pose.mulPose(Axis.ZN.rotationDegrees((float) rot));
				pose.translate(0, -1.5, 0);
				ResourceManager.industrial_turbine.renderPart("Flywheel", pose, buffer, light, overlay);
			}
		};
	}
}
