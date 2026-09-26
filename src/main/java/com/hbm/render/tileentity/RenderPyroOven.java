package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven;
import com.hbm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Pyrolysis oven: the slider moves back and forth and the fan spins while it works */
public class RenderPyroOven implements BlockEntityRenderer<TileEntityMachinePyroOven> {

	public RenderPyroOven(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachinePyroOven pyro, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(pyro.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		}

		float anim = pyro.prevAnim + (pyro.anim - pyro.prevAnim) * interp;

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.pyrooven_tex));
		ResourceManager.pyrooven.renderPart("Oven", pose, buffer, light, overlay);

		pose.pushPose();
		pose.translate(BobMathUtil.sps(anim * 0.125) / 2 - 0.5, 0, 0);
		ResourceManager.pyrooven.renderPart("Slider", pose, buffer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(1.5, 0, 1.5);
		pose.mulPose(Axis.YP.rotationDegrees((float) (anim * 45D % 360D)));
		pose.translate(-1.5, 0, -1.5);
		ResourceManager.pyrooven.renderPart("Fan", pose, buffer, light, overlay);
		pose.popPose();

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachinePyroOven tile) {
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
				pose.scale(3.5F, 3.5F, 3.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				pose.mulPose(Axis.YP.rotationDegrees(90));
				ResourceManager.pyrooven.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.pyrooven_tex)), light, overlay);
			}
		};
	}
}
