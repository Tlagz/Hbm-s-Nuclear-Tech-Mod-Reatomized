package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.machine.MachineStirling;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityStirling;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/** Stirling engine: the big gear, the small gear and the piston turning with the power output */
public class RenderStirling implements BlockEntityRenderer<TileEntityStirling> {

	public RenderStirling(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityStirling stirling, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(stirling.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		float rot = stirling.lastSpin + (stirling.spin - stirling.lastSpin) * interp;
		renderCommon(pose, buffers, light, overlay, rot, stirling.hasCog, stirling.getGeatMeta());

		pose.popPose();
	}

	public static ResourceLocation texture(int type) {
		return type == 0 ? ResourceManager.stirling_tex : type == 2 ? ResourceManager.stirling_creative_tex : ResourceManager.stirling_steel_tex;
	}

	private static void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay, float rot, boolean hasCog, int type) {
		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(texture(type)));

		ResourceManager.stirling.renderPart("Base", pose, buffer, light, overlay);

		if(hasCog) {
			pose.pushPose();
			pose.translate(0, 1.375, 0);
			pose.mulPose(Axis.ZP.rotationDegrees(-rot));
			pose.translate(0, -1.375, 0);
			ResourceManager.stirling.renderPart("Cog", pose, buffer, light, overlay);
			pose.popPose();
		}

		pose.pushPose();
		pose.translate(0, 1.375, 0.25);
		pose.mulPose(Axis.XP.rotationDegrees(rot * 2 + 3));
		pose.translate(0, -1.375, -0.25);
		ResourceManager.stirling.renderPart("CogSmall", pose, buffer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(Math.sin(rot * Math.PI / 90D) * 0.25 + 0.125, 0, 0);
		ResourceManager.stirling.renderPart("Piston", pose, buffer, light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityStirling tile) {
		return tile.getRenderBoundingBox();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRenderer(MachineStirling block) {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -1.5, 0);
				pose.scale(3.25F, 3.25F, 3.25F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				RenderStirling.renderCommon(pose, buffers, light, overlay, 0, true, block.type);
			}
		};
	}
}
