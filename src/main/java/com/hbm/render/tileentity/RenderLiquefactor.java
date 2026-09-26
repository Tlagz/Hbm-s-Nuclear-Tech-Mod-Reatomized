package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor;
import com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/** Liquefactor and solidifier: the tank's fluid rises in the glass column (the solidifier also turns with the block) */
public class RenderLiquefactor<T extends BlockEntity> implements BlockEntityRenderer<T> {

	private final HFRWavefrontObject model;
	private final ResourceLocation texture;
	private final boolean rotates;
	private final double fluidBase;

	private RenderLiquefactor(HFRWavefrontObject model, ResourceLocation texture, boolean rotates, double fluidBase) {
		this.model = model;
		this.texture = texture;
		this.rotates = rotates;
		this.fluidBase = fluidBase;
	}

	public static BlockEntityRendererProvider<TileEntityMachineLiquefactor> liquefactor() {
		return context -> new RenderLiquefactor<>(ResourceManager.liquefactor, ResourceManager.liquefactor_tex, false, 1D);
	}

	public static BlockEntityRendererProvider<TileEntityMachineSolidifier> solidifier() {
		return context -> new RenderLiquefactor<>(ResourceManager.solidifier, ResourceManager.solidifier_tex, true, 1.25D);
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

		model.renderPart("Main", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay);

		FluidTank tank = tile instanceof TileEntityMachineLiquefactor liq ? liq.tank : ((TileEntityMachineSolidifier) tile).tank;

		if(tank.getFill() > 0) {
			int color = tank.getTankType().getColor();
			double height = (double) tank.getFill() / (double) tank.getMaxFill();
			pose.pushPose();
			pose.translate(0, fluidBase, 0);
			pose.scale(1F, (float) height, 1F);
			pose.translate(0, -fluidBase, 0);
			model.renderPart("Fluid", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.white_tex)), light, overlay,
					((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, 1F);
			pose.popPose();
		}

		model.renderPart("Glass", pose, buffers.getBuffer(RenderType.entityTranslucent(ResourceManager.white_tex)), light, overlay, 0.75F, 1F, 1F, 0.15F);

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(T tile) {
		return new AABB(tile.getBlockPos()).inflate(1).expandTowards(0, 3, 0);
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRenderer(HFRWavefrontObject model, ResourceLocation texture) {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -2.5, 0);
				pose.scale(3F, 3F, 3F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				model.renderPart("Main", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay);
			}
		};
	}
}
