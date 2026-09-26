package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.tileentity.machine.TileEntityCondenser;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/** The original's RenderSmallTower and RenderLargeTower, the same static model render for both */
public class RenderCoolingTower<T extends TileEntityCondenser> implements BlockEntityRenderer<T> {

	private final HFRWavefrontObject model;
	private final ResourceLocation texture;

	private RenderCoolingTower(HFRWavefrontObject model, ResourceLocation texture) {
		this.model = model;
		this.texture = texture;
	}

	public static <T extends TileEntityCondenser> BlockEntityRendererProvider<T> small() {
		return context -> new RenderCoolingTower<>(ResourceManager.tower_small, ResourceManager.tower_small_tex);
	}

	public static <T extends TileEntityCondenser> BlockEntityRendererProvider<T> large() {
		return context -> new RenderCoolingTower<>(ResourceManager.tower_large, ResourceManager.tower_large_tex);
	}

	@Override
	public void render(T tower, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		model.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(T tile) {
		if(tile instanceof com.hbm.tileentity.machine.TileEntityTowerSmall small) return small.getRenderBoundingBox();
		if(tile instanceof com.hbm.tileentity.machine.TileEntityTowerLarge large) return large.getRenderBoundingBox();
		return new AABB(tile.getBlockPos()).inflate(5);
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRendererSmall() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -4, 0);
				pose.scale(3F, 3F, 3F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.25F, 0.25F, 0.25F);
				ResourceManager.tower_small.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.tower_small_tex)), light, overlay);
			}
		};
	}

	public static ItemRenderBase itemRendererLarge() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -3, 0);
				pose.scale(4F * 0.95F, 4F * 0.95F, 4F * 0.95F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.25F, 0.25F, 0.25F);
				ResourceManager.tower_large.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.tower_large_tex)), light, overlay);
			}
		};
	}
}
