package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityAshpit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The heating oven model with the ashpit texture, the door slides open while the GUI is open, ash inside when full */
public class RenderAshpit implements BlockEntityRenderer<TileEntityAshpit> {

	public RenderAshpit(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityAshpit ashpit, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(ashpit.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}
		pose.mulPose(Axis.YP.rotationDegrees(-90));

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.ashpit_tex));
		ResourceManager.heater_oven.renderPart("Main", pose, buffer, light, overlay);

		pose.pushPose();
		float door = ashpit.prevDoorAngle + (ashpit.doorAngle - ashpit.prevDoorAngle) * interp;
		pose.translate(0, 0, door * 0.75D / 135D);
		ResourceManager.heater_oven.renderPart("Door", pose, buffer, light, overlay);
		pose.popPose();

		ResourceManager.heater_oven.renderPart(ashpit.isFull ? "InnerBurning" : "Inner", pose, buffer, light, overlay);

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityAshpit tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -1, 0);
				pose.scale(3.25F, 3.25F, 3.25F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.ashpit_tex));
				ResourceManager.heater_oven.renderPart("Main", pose, buffer, light, overlay);
				ResourceManager.heater_oven.renderPart("Door", pose, buffer, light, overlay);
			}
		};
	}
}
