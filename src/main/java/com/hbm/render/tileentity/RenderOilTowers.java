package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticCracker;
import com.hbm.tileentity.machine.oil.TileEntityMachineFractionTower;
import com.hbm.tileentity.machine.oil.TileEntitySpacer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Static models of the oil towers: fractioning tower, its separator and the catalytic cracker */
public class RenderOilTowers {

	public static class FractionTower implements BlockEntityRenderer<TileEntityMachineFractionTower> {

		public FractionTower(BlockEntityRendererProvider.Context context) { }

		@Override
		public void render(TileEntityMachineFractionTower tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
			pose.pushPose();
			pose.translate(0.5, 0, 0.5);
			ResourceManager.fraction_tower.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.fraction_tower_tex)), light, overlay);
			pose.popPose();
		}

		@Override
		public AABB getRenderBoundingBox(TileEntityMachineFractionTower tile) {
			return tile.getRenderBoundingBox();
		}

		public static ItemRenderBase itemRenderer() {
			return new ItemRenderBase() {
				@Override
				public void renderInventory(PoseStack pose) {
					pose.translate(0, -2.5, 0);
					pose.scale(3.25F, 3.25F, 3.25F);
				}

				@Override
				public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
					ResourceManager.fraction_tower.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.fraction_tower_tex)), light, overlay);
				}
			};
		}
	}

	public static class Spacer implements BlockEntityRenderer<TileEntitySpacer> {

		public Spacer(BlockEntityRendererProvider.Context context) { }

		@Override
		public void render(TileEntitySpacer tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
			pose.pushPose();
			pose.translate(0.5, 0, 0.5);
			ResourceManager.fraction_spacer.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.fraction_spacer_tex)), light, overlay);
			pose.popPose();
		}

		@Override
		public AABB getRenderBoundingBox(TileEntitySpacer tile) {
			return tile.getRenderBoundingBox();
		}

		public static ItemRenderBase itemRenderer() {
			return new ItemRenderBase() {
				@Override
				public void renderInventory(PoseStack pose) {
					pose.scale(3.25F, 3.25F, 3.25F);
				}

				@Override
				public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
					ResourceManager.fraction_spacer.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.fraction_spacer_tex)), light, overlay);
				}
			};
		}
	}

	public static class Cracker implements BlockEntityRenderer<TileEntityMachineCatalyticCracker> {

		public Cracker(BlockEntityRendererProvider.Context context) { }

		@Override
		public void render(TileEntityMachineCatalyticCracker tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
			pose.pushPose();
			pose.translate(0.5, 0, 0.5);

			switch(BlockDummyable.getMeta(tile.getBlockState()) - BlockDummyable.offset) {
			case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
			case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
			case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
			case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
			}

			ResourceManager.cracking_tower.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.cracking_tower_tex)), light, overlay);
			pose.popPose();
		}

		@Override
		public AABB getRenderBoundingBox(TileEntityMachineCatalyticCracker tile) {
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
					pose.scale(1.8F, 1.8F, 1.8F);
				}

				@Override
				public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
					pose.scale(0.5F, 0.5F, 0.5F);
					ResourceManager.cracking_tower.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.cracking_tower_tex)), light, overlay);
				}
			};
		}
	}
}
