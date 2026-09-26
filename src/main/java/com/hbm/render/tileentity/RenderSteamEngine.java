package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntitySteamEngine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Steam engine: flywheel, shaft, connecting rod and piston moving with the rotor angle */
public class RenderSteamEngine implements BlockEntityRenderer<TileEntitySteamEngine> {

	public RenderSteamEngine(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntitySteamEngine engine, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(engine.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		float angle = engine.lastRotor + (engine.rotor - engine.lastRotor) * interp;
		pose.translate(2, 0, 0);
		renderCommon(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.steam_engine_tex)), light, overlay, angle);

		pose.popPose();
	}

	private static void renderCommon(PoseStack pose, VertexConsumer buffer, int light, int overlay, double rot) {

		ResourceManager.steam_engine.renderPart("Base", pose, buffer, light, overlay);

		pose.pushPose();
		pose.translate(2, 1.375, 0);
		pose.mulPose(Axis.ZN.rotationDegrees((float) rot));
		pose.translate(-2, -1.375, 0);
		ResourceManager.steam_engine.renderPart("Flywheel", pose, buffer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(0, 1.375, -0.5);
		pose.mulPose(Axis.XP.rotationDegrees((float) (rot * 2D)));
		pose.translate(0, -1.375, 0.5);
		ResourceManager.steam_engine.renderPart("Shaft", pose, buffer, light, overlay);
		pose.popPose();

		double sin = Math.sin(rot * Math.PI / 180D) * 0.25D - 0.25D;
		double cos = Math.cos(rot * Math.PI / 180D) * 0.25D;

		pose.pushPose();
		double ang = Math.acos(cos / 1.875D);
		pose.translate(sin, cos, 0);
		pose.translate(2.25, 1.375, 0);
		pose.mulPose(Axis.ZN.rotationDegrees((float) (ang * 180D / Math.PI - 90D)));
		pose.translate(-2.25, -1.375, 0);
		ResourceManager.steam_engine.renderPart("Transmission", pose, buffer, light, overlay);
		pose.popPose();

		pose.pushPose();
		double cath = Math.sqrt(3.515625D - (cos * cos) / 2);
		pose.translate(1.875 - cath + sin, 0, 0); //the difference that "1.875 - cath" makes is minuscule but very much noticeable
		ResourceManager.steam_engine.renderPart("Piston", pose, buffer, light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntitySteamEngine tile) {
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
				pose.mulPose(Axis.YN.rotationDegrees(90));
				pose.translate(0, -1.5, 0);
				pose.scale(2F, 2F, 2F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				RenderSteamEngine.renderCommon(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.steam_engine_tex)), light, overlay, 0);
			}
		};
	}
}
