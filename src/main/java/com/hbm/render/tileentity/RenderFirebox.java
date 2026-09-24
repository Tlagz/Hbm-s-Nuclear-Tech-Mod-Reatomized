package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityHeaterFirebox;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public class RenderFirebox implements BlockEntityRenderer<TileEntityHeaterFirebox> {

	public RenderFirebox(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityHeaterFirebox firebox, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(firebox.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}
		pose.mulPose(Axis.YP.rotationDegrees(-90));

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.heater_firebox_tex));
		ResourceManager.heater_firebox.renderPart("Main", pose, consumer, light, overlay);

		pose.pushPose();
		float door = firebox.prevDoorAngle + (firebox.doorAngle - firebox.prevDoorAngle) * interp;
		pose.translate(1.375, 0, 0.375);
		pose.mulPose(Axis.YN.rotationDegrees(door));
		pose.translate(-1.375, 0, -0.375);
		ResourceManager.heater_firebox.renderPart("Door", pose, consumer, light, overlay);
		pose.popPose();

		if(firebox.wasOn) {
			// full bright, no culling
			VertexConsumer glow = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.heater_firebox_tex));
			ResourceManager.heater_firebox.renderPart("InnerBurning", pose, glow, LightTexture.FULL_BRIGHT, overlay);
		} else {
			ResourceManager.heater_firebox.renderPart("InnerEmpty", pose, consumer, light, overlay);
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityHeaterFirebox tile) {
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
				pose.scale(3.25F, 3.25F, 3.25F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.heater_firebox_tex));
				ResourceManager.heater_firebox.renderPart("Main", pose, consumer, light, overlay);
				ResourceManager.heater_firebox.renderPart("Door", pose, consumer, light, overlay);
			}
		};
	}
}
