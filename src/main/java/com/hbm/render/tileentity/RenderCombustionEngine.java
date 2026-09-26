package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids.CD_Canister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineCombustionEngine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Combustion engine: the fuel canister in the fuel's color, the hatch swings open while the GUI is open */
public class RenderCombustionEngine implements BlockEntityRenderer<TileEntityMachineCombustionEngine> {

	public RenderCombustionEngine(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineCombustionEngine engine, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(engine.getBlockState()) - BlockDummyable.offset) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		}

		pose.translate(-0.5, 0, 3);

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.combustion_engine_tex));
		ResourceManager.combustion_engine.renderPart("Engine", pose, buffer, light, overlay);

		float r = 1F, g = 1F, b = 1F;
		CD_Canister canister = engine.tank.getTankType().getContainer(CD_Canister.class);
		if(canister != null) {
			int color = canister.color;
			r = ((color & 0xff0000) >> 16) / 256F;
			g = ((color & 0x00ff00) >> 8) / 256F;
			b = ((color & 0x0000ff) >> 0) / 256F;
		}
		ResourceManager.combustion_engine.renderPart("Canister", pose, buffer, light, overlay, r, g, b, 1F);

		pose.translate(1, 0, -2.6875);
		pose.mulPose(Axis.YN.rotationDegrees(engine.prevDoorAngle + (engine.doorAngle - engine.prevDoorAngle) * interp));
		pose.translate(-1, 0, 2.6875);
		ResourceManager.combustion_engine.renderPart("Hatch", pose, buffer, light, overlay);

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineCombustionEngine tile) {
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
				pose.translate(0, -1, 0);
				pose.scale(2.75F, 2.75F, 2.75F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.translate(0, 0, 2.75);
				ResourceManager.combustion_engine.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.combustion_engine_tex)), light, overlay);
			}
		};
	}
}
