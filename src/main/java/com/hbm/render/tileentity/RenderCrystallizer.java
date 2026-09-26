package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineCrystallizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Ore acidizer: the spinner turns while it works, the acid shows in the tank */
public class RenderCrystallizer implements BlockEntityRenderer<TileEntityMachineCrystallizer> {

	public RenderCrystallizer(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineCrystallizer crys, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(crys.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.crystallizer_tex));
		ResourceManager.crystallizer.renderPart("Body", pose, buffer, light, overlay);

		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(crys.prevAngle + (crys.angle - crys.prevAngle) * interp));
		ResourceManager.crystallizer.renderPart("Spinner", pose, buffer, light, overlay);
		pose.popPose();

		if(crys.prevAngle != crys.angle && crys.tank.getTankType().getTexture() != null) {
			VertexConsumer fluid = buffers.getBuffer(RenderType.entityTranslucent(crys.tank.getTankType().getTexture()));
			ResourceManager.crystallizer.renderPart("Fluid", pose, fluid, light, overlay);
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineCrystallizer tile) {
		return tile.getRenderBoundingBox();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderNonInv(PoseStack pose) {
				pose.scale(0.5F, 0.5F, 0.5F);
			}

			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -4, 0);
				pose.scale(2F, 2F, 2F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.crystallizer_tex));
				ResourceManager.crystallizer.renderPart("Body", pose, buffer, light, overlay);
				ResourceManager.crystallizer.renderPart("Spinner", pose, buffer, light, overlay);
			}
		};
	}
}
