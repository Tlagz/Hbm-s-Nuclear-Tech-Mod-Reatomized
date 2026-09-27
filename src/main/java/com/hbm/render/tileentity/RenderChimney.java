package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.tileentity.machine.TileEntityChimneyBase;
import com.hbm.tileentity.machine.TileEntityChimneyBrick;
import com.hbm.tileentity.machine.TileEntityChimneyIndustrial;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/** Both smokestacks, the original's RenderChimneyBrick and RenderChimneyIndustrial */
public class RenderChimney<T extends TileEntityChimneyBase> implements BlockEntityRenderer<T> {

	private final HFRWavefrontObject model;
	private final ResourceLocation texture;

	private RenderChimney(HFRWavefrontObject model, ResourceLocation texture) {
		this.model = model;
		this.texture = texture;
	}

	public static BlockEntityRendererProvider<TileEntityChimneyBrick> brick() {
		return ctx -> new RenderChimney<>(ResourceManager.chimney_brick, ResourceManager.chimney_brick_tex);
	}

	public static BlockEntityRendererProvider<TileEntityChimneyIndustrial> industrial() {
		return ctx -> new RenderChimney<>(ResourceManager.chimney_industrial, ResourceManager.chimney_industrial_tex);
	}

	@Override
	public void render(T tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0D, 0.5D);
		pose.mulPose(Axis.YP.rotationDegrees(180));
		model.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(T tile) {
		if(tile instanceof TileEntityChimneyIndustrial ind) return ind.getRenderBoundingBox();
		if(tile instanceof TileEntityChimneyBrick brick) return brick.getRenderBoundingBox();
		return BlockEntityRenderer.super.getRenderBoundingBox(tile);
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRendererBrick() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -5, 0);
				pose.scale(2.25F, 2.25F, 2.25F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				ResourceManager.chimney_brick.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.chimney_brick_tex)), light, overlay);
			}
		};
	}

	public static ItemRenderBase itemRendererIndustrial() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -5, 0);
				pose.scale(2.75F, 2.75F, 2.75F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.25F, 0.25F, 0.25F);
				ResourceManager.chimney_industrial.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.chimney_industrial_tex)), light, overlay);
			}
		};
	}
}
