package com.hbm.render.tileentity;

import com.hbm.lib.Library;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineRTG;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** The generator and a connector towards every side a cable (or anything else taking power) is on */
public class RenderRTG implements BlockEntityRenderer<TileEntityMachineRTG> {

	public RenderRTG(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineRTG rtg, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0, 0.5D);
		pose.mulPose(Axis.YP.rotationDegrees(180));

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.rtg_tex));
		ResourceManager.rtg.renderPart("Gen", pose, consumer, light, overlay);

		BlockPos pos = rtg.getBlockPos();
		if(Library.canConnect(rtg.getLevel(), pos.east(), Direction.EAST)) connector(pose, consumer, light, overlay, 0);
		if(Library.canConnect(rtg.getLevel(), pos.west(), Direction.WEST)) connector(pose, consumer, light, overlay, 180);
		if(Library.canConnect(rtg.getLevel(), pos.north(), Direction.NORTH)) connector(pose, consumer, light, overlay, 90);
		if(Library.canConnect(rtg.getLevel(), pos.south(), Direction.SOUTH)) connector(pose, consumer, light, overlay, -90);

		pose.popPose();
	}

	private static void connector(PoseStack pose, VertexConsumer consumer, int light, int overlay, float rot) {
		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(rot));
		ResourceManager.rtg.renderPart("Connector", pose, consumer, light, overlay);
		pose.popPose();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -3.25, 0);
				pose.scale(6.5F, 6.5F, 6.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.rtg.renderPart("Gen", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.rtg_tex)), light, overlay);
			}
		};
	}
}
