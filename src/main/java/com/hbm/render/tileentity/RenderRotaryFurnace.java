package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace;
import com.hbm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Rotary furnace, the piston pumps faster with better fuel */
public class RenderRotaryFurnace implements BlockEntityRenderer<TileEntityMachineRotaryFurnace> {

	public RenderRotaryFurnace(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineRotaryFurnace furnace, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(furnace.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.rotary_furnace_tex));
		ResourceManager.rotary_furnace.renderPart("Furnace", pose, buffer, light, overlay);

		float anim = furnace.lastAnim + (furnace.anim - furnace.lastAnim) * interp;

		pose.pushPose();
		pose.translate(0, BobMathUtil.sps((anim * 0.75) * 0.125) * 0.5 - 0.5, 0);
		ResourceManager.rotary_furnace.renderPart("Piston", pose, buffer, light, overlay);
		pose.popPose();

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineRotaryFurnace tile) {
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
				pose.translate(0, -2, 0);
				pose.scale(3.5F, 3.5F, 3.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.625F, 0.625F, 0.625F);
				pose.mulPose(Axis.YP.rotationDegrees(90));
				ResourceManager.rotary_furnace.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.rotary_furnace_tex)), light, overlay);
			}
		};
	}
}
