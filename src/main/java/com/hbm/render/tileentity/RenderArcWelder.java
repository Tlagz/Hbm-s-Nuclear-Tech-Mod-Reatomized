package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineArcWelder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;

/** The welder with the current recipe's output lying on the welding bed */
public class RenderArcWelder implements BlockEntityRenderer<TileEntityMachineArcWelder> {

	public RenderArcWelder(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineArcWelder welder, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(welder.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		pose.translate(-0.5, 0, 0);

		ResourceManager.arc_welder.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.arc_welder_tex)), light, overlay);

		if(!welder.display.isEmpty()) {
			pose.pushPose();
			pose.translate(0.0625D * 2.5D, 1.125D, 0D);
			pose.mulPose(Axis.YP.rotationDegrees(90));
			pose.mulPose(Axis.XP.rotationDegrees(-90));
			pose.scale(0.75F, 0.75F, 0.75F);
			Minecraft.getInstance().getItemRenderer().renderStatic(welder.display, ItemDisplayContext.FIXED, light, overlay, pose, buffers, welder.getLevel(), 0);
			pose.popPose();
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineArcWelder tile) {
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
				pose.scale(4, 4, 4);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.arc_welder.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.arc_welder_tex)), light, overlay);
			}
		};
	}
}
