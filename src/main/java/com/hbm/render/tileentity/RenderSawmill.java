package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.items.ModDataComponents;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntitySawmill;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** The frame, the spinning blade (if it still has one) and the two gears */
public class RenderSawmill implements BlockEntityRenderer<TileEntitySawmill> {

	public RenderSawmill(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntitySawmill sawmill, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0, 0.5D);

		switch(BlockDummyable.getMeta(sawmill.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		float rot = sawmill.lastSpin + (sawmill.spin - sawmill.lastSpin) * interp;
		renderCommon(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.sawmill_tex)), light, overlay, rot, sawmill.hasBlade);

		pose.popPose();
	}

	private static void renderCommon(PoseStack pose, VertexConsumer consumer, int light, int overlay, float rot, boolean hasBlade) {
		ResourceManager.sawmill.renderPart("Main", pose, consumer, light, overlay);

		if(hasBlade) {
			pose.pushPose();
			pose.translate(0, 1.375, 0);
			pose.mulPose(Axis.ZP.rotationDegrees(-rot * 2));
			pose.translate(0, -1.375, 0);
			ResourceManager.sawmill.renderPart("Blade", pose, consumer, light, overlay);
			pose.popPose();
		}

		pose.pushPose();
		pose.translate(0.5625, 1.375, 0);
		pose.mulPose(Axis.ZP.rotationDegrees(rot));
		pose.translate(-0.5625, -1.375, 0);
		ResourceManager.sawmill.renderPart("GearLeft", pose, consumer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(-0.5625, 1.375, 0);
		pose.mulPose(Axis.ZP.rotationDegrees(-rot));
		pose.translate(0.5625, -1.375, 0);
		ResourceManager.sawmill.renderPart("GearRight", pose, consumer, light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntitySawmill tile) {
		return tile.getRenderBoundingBox();
	}

	public static ItemRenderBase itemRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -1.5, 0);
				pose.scale(3.25F, 3.25F, 3.25F);
			}

			@Override
			public void renderCommonWithStack(ItemStack stack, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				boolean blade = !stack.getOrDefault(ModDataComponents.NO_COG.get(), false);
				RenderSawmill.renderCommon(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.sawmill_tex)), light, overlay, blade ? System.currentTimeMillis() % 3600 * 0.1F : 0, blade);
			}
		};
	}
}
