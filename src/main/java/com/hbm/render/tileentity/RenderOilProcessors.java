package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.tileentity.machine.oil.TileEntityOilProcessorBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * Static models of the vacuum distiller, hydrotreater and catalytic reformer (the only one that turns with the block).
 * TODO the reformer's polaroid easter egg
 */
public class RenderOilProcessors<T extends TileEntityOilProcessorBase> implements BlockEntityRenderer<T> {

	private final HFRWavefrontObject model;
	private final ResourceLocation texture;
	private final boolean rotates;

	private RenderOilProcessors(HFRWavefrontObject model, ResourceLocation texture, boolean rotates) {
		this.model = model;
		this.texture = texture;
		this.rotates = rotates;
	}

	public static <T extends TileEntityOilProcessorBase> BlockEntityRendererProvider<T> vacuumDistill() {
		return context -> new RenderOilProcessors<>(ResourceManager.vacuum_distill, ResourceManager.vacuum_distill_tex, false);
	}

	public static <T extends TileEntityOilProcessorBase> BlockEntityRendererProvider<T> hydrotreater() {
		return context -> new RenderOilProcessors<>(ResourceManager.hydrotreater, ResourceManager.hydrotreater_tex, false);
	}

	public static <T extends TileEntityOilProcessorBase> BlockEntityRendererProvider<T> catalyticReformer() {
		return context -> new RenderOilProcessors<>(ResourceManager.catalytic_reformer, ResourceManager.catalytic_reformer_tex, true);
	}

	@Override
	public void render(T tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		if(rotates) {
			switch(BlockDummyable.getMeta(tile.getBlockState()) - BlockDummyable.offset) {
			case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
			case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
			case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
			case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
			}
		}

		model.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(T tile) {
		return new AABB(tile.getBlockPos()).inflate(3).expandTowards(0, 7, 0);
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRenderer(HFRWavefrontObject model, ResourceLocation texture, double down, float scale) {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -down, 0);
				pose.scale(scale, scale, scale);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				model.renderAll(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay);
			}
		};
	}
}
