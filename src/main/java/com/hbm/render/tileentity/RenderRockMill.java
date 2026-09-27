package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineRockMill;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Base (and the frame if something is on top) and the spinning wheel */
public class RenderRockMill implements BlockEntityRenderer<TileEntityMachineRockMill> {

	public RenderRockMill(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineRockMill mill, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(90));

		switch(BlockDummyable.getMeta(mill.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.rock_mill_tex));
		ResourceManager.rock_mill.renderPart("Base", pose, consumer, light, overlay);
		if(mill.frame) ResourceManager.rock_mill.renderPart("Frame", pose, consumer, light, overlay);

		float rot = mill.prevRotation + (mill.rotation - mill.prevRotation) * interp;
		pose.mulPose(Axis.YN.rotationDegrees(rot));
		ResourceManager.rock_mill.renderPart("Wheel", pose, consumer, light, overlay);

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineRockMill tile) {
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
				pose.scale(0.75F, 0.75F, 0.75F);
				ResourceManager.rock_mill.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.rock_mill_tex)), light, overlay);
			}
		};
	}
}
