package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.machine.MachinePump;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachinePumpBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Ground water pump: the rotor turns, the arms and piston move up and down with it */
public class RenderPump implements BlockEntityRenderer<TileEntityMachinePumpBase> {

	public RenderPump(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachinePumpBase pump, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(pump.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		float angle = pump.lastRotor + (pump.rotor - pump.lastRotor) * interp;
		int type = pump.getBlockState().getBlock() instanceof MachinePump block ? block.type : 0;
		renderCommon(pose, buffers, light, overlay, angle, type);

		pose.popPose();
	}

	private static void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay, double rot, int type) {

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(type == 0 ? ResourceManager.pump_steam_tex : ResourceManager.pump_electric_tex));
		ResourceManager.pump.renderPart("Base", pose, buffer, light, overlay);

		pose.pushPose();
		pose.translate(0, 2.25, 0);
		pose.mulPose(Axis.ZP.rotationDegrees((float) (rot - 90)));
		pose.translate(0, -2.25, 0);
		ResourceManager.pump.renderPart("Rotor", pose, buffer, light, overlay);
		pose.popPose();

		double sin = Math.sin(rot * Math.PI / 180D) * 0.5D - 0.5D;
		double cos = Math.cos(rot * Math.PI / 180D) * 0.5D;
		double ang = Math.acos(cos / 2D);
		double cath = Math.sqrt(1 + (cos * cos) / 2);

		pose.pushPose();
		pose.translate(0, 1 - cath + sin, 0);
		pose.translate(0, 4.75, 0);
		pose.mulPose(Axis.ZN.rotationDegrees((float) (ang * 180D / Math.PI - 90D)));
		pose.translate(0, -4.75, 0);
		ResourceManager.pump.renderPart("Arms", pose, buffer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(0, 1 - cath + sin, 0);
		ResourceManager.pump.renderPart("Piston", pose, buffer, light, overlay);
		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachinePumpBase tile) {
		return tile.getRenderBoundingBox();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public static ItemRenderBase itemRenderer(int type) {
		return new ItemRenderBase() {
			@Override
			public void renderInventory(PoseStack pose) {
				pose.translate(0, -3, 0);
				pose.scale(2.5F, 2.5F, 2.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				RenderPump.renderCommon(pose, buffers, light, overlay, System.currentTimeMillis() % 3600 * 0.1F, type);
			}
		};
	}
}
