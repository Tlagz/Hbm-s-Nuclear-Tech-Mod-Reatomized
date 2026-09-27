package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineChemicalFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Base (and the frame if something is on top) and the two fans spinning while any field is working */
public class RenderChemicalFactory implements BlockEntityRenderer<TileEntityMachineChemicalFactory> {

	public RenderChemicalFactory(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineChemicalFactory chemplant, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(90));

		switch(BlockDummyable.getMeta(chemplant.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		float anim = chemplant.prevAnim + (chemplant.anim - chemplant.prevAnim) * interp;

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.chemical_factory_tex));
		ResourceManager.chemical_factory.renderPart("Base", pose, consumer, light, overlay);
		if(chemplant.frame) ResourceManager.chemical_factory.renderPart("Frame", pose, consumer, light, overlay);

		pose.pushPose();
		pose.translate(1, 0, 0);
		pose.mulPose(Axis.YP.rotationDegrees((float) (-anim * 45 % 360D)));
		pose.translate(-1, 0, 0);
		ResourceManager.chemical_factory.renderPart("Fan1", pose, consumer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(-1, 0, 0);
		pose.mulPose(Axis.YP.rotationDegrees((float) (-anim * 45 % 360D)));
		pose.translate(1, 0, 0);
		ResourceManager.chemical_factory.renderPart("Fan2", pose, consumer, light, overlay);
		pose.popPose();

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineChemicalFactory tile) {
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
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.scale(0.75F, 0.75F, 0.75F);
				VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.chemical_factory_tex));
				ResourceManager.chemical_factory.renderPart("Base", pose, consumer, light, overlay);
				ResourceManager.chemical_factory.renderPart("Frame", pose, consumer, light, overlay);
				ResourceManager.chemical_factory.renderPart("Fan1", pose, consumer, light, overlay);
				ResourceManager.chemical_factory.renderPart("Fan2", pose, consumer, light, overlay);
			}
		};
	}
}
