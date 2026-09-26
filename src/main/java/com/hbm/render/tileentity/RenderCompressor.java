package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineCompressor;
import com.hbm.tileentity.machine.TileEntityMachineCompressorCompact;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The compressor tower (pumping piston and fan) and the compact compressor (the condenser body with two fans) */
public class RenderCompressor {

	private static void rotate(PoseStack pose, int meta, int north, int west, int south, int east) {
		switch(meta - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(north)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(west)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(south)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(east)); break;
		}
	}

	public static class Tower implements BlockEntityRenderer<TileEntityMachineCompressor> {

		public Tower(BlockEntityRendererProvider.Context context) { }

		@Override
		public void render(TileEntityMachineCompressor compressor, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
			pose.pushPose();
			pose.translate(0.5, 0, 0.5);
			rotate(pose, BlockDummyable.getMeta(compressor.getBlockState()), 90, 180, 270, 0);

			VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.compressor_tex));
			ResourceManager.compressor.renderPart("Compressor", pose, buffer, light, overlay);

			float lift = compressor.prevPiston + (compressor.piston - compressor.prevPiston) * interp;
			float fan = compressor.prevFanSpin + (compressor.fanSpin - compressor.prevFanSpin) * interp;

			pose.pushPose();
			pose.translate(0, lift * 3 - 3, 0);
			ResourceManager.compressor.renderPart("Pump", pose, buffer, light, overlay);
			pose.popPose();

			pose.pushPose();
			pose.translate(0, 1.5, 0);
			pose.mulPose(Axis.XP.rotationDegrees(fan));
			pose.translate(0, -1.5, 0);
			ResourceManager.compressor.renderPart("Fan", pose, buffer, light, overlay);
			pose.popPose();

			pose.popPose();
		}

		@Override
		public AABB getRenderBoundingBox(TileEntityMachineCompressor tile) {
			return tile.getRenderBoundingBox();
		}

		@Override
		public int getViewDistance() {
			return 256;
		}
	}

	public static class Compact implements BlockEntityRenderer<TileEntityMachineCompressorCompact> {

		public Compact(BlockEntityRendererProvider.Context context) { }

		@Override
		public void render(TileEntityMachineCompressorCompact compressor, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
			pose.pushPose();
			pose.translate(0.5, 0, 0.5);
			rotate(pose, BlockDummyable.getMeta(compressor.getBlockState()), 90, 180, 270, 0);

			VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.compressor_compact_tex));
			ResourceManager.condenser.renderPart("Condenser", pose, buffer, light, overlay);

			float rot = compressor.prevFanSpin + (compressor.fanSpin - compressor.prevFanSpin) * interp;

			pose.pushPose();
			pose.translate(0, 1.5, 0);
			pose.mulPose(Axis.XP.rotationDegrees(rot));
			pose.translate(0, -1.5, 0);
			ResourceManager.condenser.renderPart("Fan1", pose, buffer, light, overlay);
			pose.popPose();

			pose.pushPose();
			pose.translate(0, 1.5, 0);
			pose.mulPose(Axis.XN.rotationDegrees(rot));
			pose.translate(0, -1.5, 0);
			ResourceManager.condenser.renderPart("Fan2", pose, buffer, light, overlay);
			pose.popPose();

			pose.popPose();
		}

		@Override
		public AABB getRenderBoundingBox(TileEntityMachineCompressorCompact tile) {
			return tile.getRenderBoundingBox();
		}

		@Override
		public int getViewDistance() {
			return 256;
		}
	}

	public static ItemRenderBase towerItem() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -4, 0);
				pose.scale(3F, 3F, 3F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.compressor_tex));
				ResourceManager.compressor.renderPart("Compressor", pose, buffer, light, overlay);

				double lift = (System.currentTimeMillis() * 0.005) % 9;
				if(lift > 3) lift = 3 - (lift - 3) / 2D;
				pose.pushPose();
				pose.translate(0, -lift, 0);
				ResourceManager.compressor.renderPart("Pump", pose, buffer, light, overlay);
				pose.popPose();

				pose.pushPose();
				pose.translate(0, 1.5, 0);
				pose.mulPose(Axis.XP.rotationDegrees((float) ((System.currentTimeMillis() * 0.25) % 360D)));
				pose.translate(0, -1.5, 0);
				ResourceManager.compressor.renderPart("Fan", pose, buffer, light, overlay);
				pose.popPose();
			}
		};
	}

	public static ItemRenderBase compactItem() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(-1, -1, 0);
				pose.scale(2.75F, 2.75F, 2.75F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.75F, 0.75F, 0.75F);
				pose.translate(0.5, 0, 0);
				ResourceManager.condenser.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.compressor_compact_tex)), light, overlay);
			}
		};
	}
}
