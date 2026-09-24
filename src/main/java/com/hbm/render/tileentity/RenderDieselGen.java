package com.hbm.render.tileentity;

import com.hbm.blocks.machine.MachineDiesel;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class RenderDieselGen implements BlockEntityRenderer<TileEntityMachineDiesel> {

	public RenderDieselGen(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineDiesel tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(tile.getBlockState().getValue(MachineDiesel.FACING)) {
		case SOUTH: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case EAST: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case NORTH: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case WEST: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		default: break;
		}

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.dieselgen_tex));
		ResourceManager.dieselgen.renderPart("Generator", pose, consumer, light, overlay);

		// the engine shakes while running
		if(tile.isOn && tile.hasAcceptableFuel() && tile.tank.getFill() > 0) {
			double swingSide = Math.sin(System.currentTimeMillis() / 50D) * 0.005;
			double swingFront = Math.sin(System.currentTimeMillis() / 25D) * 0.005;
			pose.translate(swingFront, 0, swingSide);
		}

		ResourceManager.dieselgen.renderPart("Engine", pose, consumer, light, overlay);

		pose.popPose();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -2.5, 0);
				pose.scale(5, 5, 5);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(2, 2, 2);
				ResourceManager.dieselgen.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.dieselgen_tex)), light, overlay);
			}
		};
	}
}
