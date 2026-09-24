package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachineRefinery;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The refinery is rotationally symmetric enough that the original never rotated it */
public class RenderRefinery implements BlockEntityRenderer<TileEntityMachineRefinery> {

	public RenderRefinery(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineRefinery refinery, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(180));

		if(refinery.tilted) {
			pose.translate(0, -0.25, 0);
			pose.mulPose(Axis.ZP.rotationDegrees(10));
			pose.mulPose(Axis.YP.rotationDegrees(5));
		}

		var consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.refinery_tex));
		if(refinery.hasExploded) {
			ResourceManager.refinery_exploded.renderAll(pose, consumer, light, overlay);
		} else {
			ResourceManager.refinery.renderAll(pose, consumer, light, overlay);
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineRefinery tile) {
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
				pose.translate(0, -4, 0);
				pose.scale(3, 3, 3);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(180));
				pose.scale(0.5F, 0.5F, 0.5F);
				ResourceManager.refinery.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.refinery_tex)), light, overlay);
			}
		};
	}
}
