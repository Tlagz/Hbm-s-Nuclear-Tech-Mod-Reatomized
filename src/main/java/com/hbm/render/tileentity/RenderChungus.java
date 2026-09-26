package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityChungus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Leviathan turbine: body, the steam grade lever and the big fan */
public class RenderChungus implements BlockEntityRenderer<TileEntityChungus> {

	public RenderChungus(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityChungus turbine, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		pose.mulPose(Axis.YP.rotationDegrees(90));

		switch(BlockDummyable.getMeta(turbine.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		pose.translate(0, 0, -3);

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.chungus_tex));
		ResourceManager.chungus.renderPart("Body", pose, buffer, light, overlay);

		pose.pushPose();
		pose.translate(0, 0, 4.5);
		pose.mulPose(Axis.XP.rotationDegrees(15 - (turbine.tanks[0].getTankType().ordinal() - 2) * 10));
		pose.translate(0, 0, -4.5);
		ResourceManager.chungus.renderPart("Lever", pose, buffer, light, overlay);
		pose.popPose();

		pose.translate(0, 2.5, 0);
		pose.mulPose(Axis.ZN.rotationDegrees(turbine.lastRotor + (turbine.rotor - turbine.lastRotor) * interp));
		pose.translate(0, -2.5, 0);
		ResourceManager.chungus.renderPart("Blades", pose, buffer, light, overlay);

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityChungus tile) {
		return tile.getRenderBoundingBox();
	}

	@Override
	public boolean shouldRenderOffScreen(TileEntityChungus tile) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0.5, 0, 0);
				pose.scale(2.5F, 2.5F, 2.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				pose.mulPose(Axis.YP.rotationDegrees(90));
				VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.chungus_tex));
				ResourceManager.chungus.renderPart("Body", pose, buffer, light, overlay);
				ResourceManager.chungus.renderPart("Lever", pose, buffer, light, overlay);
				ResourceManager.chungus.renderPart("Blades", pose, buffer, light, overlay);
			}
		};
	}
}
