package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineEPress;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;

/** Body, the head moving down with the press progress and the input item on the bed */
public class RenderEPress implements BlockEntityRenderer<TileEntityMachineEPress> {

	public RenderEPress(BlockEntityRendererProvider.Context context) { }

	private static void rotate(PoseStack pose, TileEntityMachineEPress press) {
		pose.mulPose(Axis.YP.rotationDegrees(180));
		switch(BlockDummyable.getMeta(press.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		}
	}

	@Override
	public void render(TileEntityMachineEPress press, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {

		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		rotate(pose, press);
		ResourceManager.epress_body.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.epress_body_tex)), light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(0.5, 1, 0.5);
		rotate(pose, press);
		double p = (press.lastPress + (press.renderPress - press.lastPress) * interp) / (double) TileEntityMachineEPress.maxPress;
		pose.translate(0, Mth.clamp(1D - p, 0D, 1D) * 0.875D, 0);
		ResourceManager.epress_head.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.epress_head_tex)), light, overlay);
		pose.popPose();

		if(!press.syncStack.isEmpty()) {
			pose.pushPose();
			// flat on the press bed, one block up
			pose.translate(0.5, 1 + 0.0625 * 0.35, 0.5);
			rotate(pose, press);
			pose.mulPose(Axis.XP.rotationDegrees(90));
			pose.scale(0.5F, 0.5F, 0.5F);
			Minecraft.getInstance().getItemRenderer().renderStatic(press.syncStack.copyWithCount(1), ItemDisplayContext.FIXED, light, overlay, pose, buffers, press.getLevel(), 0);
			pose.popPose();
		}
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineEPress tile) {
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
				pose.translate(0, -3.5, 0);
				pose.scale(4.5F, 4.5F, 4.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				ResourceManager.epress_body.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.epress_body_tex)), light, overlay);
				pose.translate(0, 1, 0);
				ResourceManager.epress_head.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.epress_head_tex)), light, overlay);
			}
		};
	}
}
