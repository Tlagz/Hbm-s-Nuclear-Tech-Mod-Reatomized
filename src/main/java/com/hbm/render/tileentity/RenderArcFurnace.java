package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** Electric arc furnace: the lid lifts to load, hot electrodes and molten contents glow, the cables sway while it runs */
public class RenderArcFurnace implements BlockEntityRenderer<TileEntityMachineArcFurnaceLarge> {

	public RenderArcFurnace(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineArcFurnaceLarge arc, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(arc.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		float lift = arc.prevLid + (arc.lid - arc.prevLid) * interp;
		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.arc_furnace_tex));
		int bright = LightTexture.FULL_BRIGHT;

		ResourceManager.arc_furnace.renderPart("Furnace", pose, buffer, light, overlay);

		if(!arc.liquids.isEmpty()) {
			pose.pushPose();
			pose.translate(0, -1.75 + TileEntityMachineArcFurnaceLarge.getStackAmount(arc.liquids) * 1.75 / TileEntityMachineArcFurnaceLarge.maxLiquid, 0);
			ResourceManager.arc_furnace.renderPart("ContentsHot", pose, buffer, bright, overlay);
			pose.popPose();
		} else if(arc.hasMaterial) {
			ResourceManager.arc_furnace.renderPart("ContentsCold", pose, buffer, light, overlay);
		}

		long time = arc.getLevel() != null ? arc.getLevel().getGameTime() : 0;

		pose.translate(0, 2 * lift, 0);
		if(arc.isProgressing) pose.translate(0, 0, Math.sin(time + interp) * 0.005);
		ResourceManager.arc_furnace.renderPart("Lid", pose, buffer, light, overlay);
		for(int i = 0; i < 3; i++) {
			byte electrode = arc.electrodes[i];
			if(electrode != TileEntityMachineArcFurnaceLarge.ELECTRODE_NONE) ResourceManager.arc_furnace.renderPart("Ring" + (i + 1), pose, buffer, light, overlay);
			if(electrode == TileEntityMachineArcFurnaceLarge.ELECTRODE_FRESH) ResourceManager.arc_furnace.renderPart("Electrode" + (i + 1), pose, buffer, light, overlay);
			if(electrode == TileEntityMachineArcFurnaceLarge.ELECTRODE_USED) ResourceManager.arc_furnace.renderPart("Electrode" + (i + 1) + "Hot", pose, buffer, bright, overlay);
			if(electrode == TileEntityMachineArcFurnaceLarge.ELECTRODE_DEPLETED) ResourceManager.arc_furnace.renderPart("Electrode" + (i + 1) + "Short", pose, buffer, bright, overlay);
		}

		// the cables hang from 5.5 blocks up, one per electrode at z 0.5, 0 and -0.5
		for(int i = 0; i < 3; i++) {
			if(arc.electrodes[i] == TileEntityMachineArcFurnaceLarge.ELECTRODE_NONE) continue;
			double z = 0.5 - i * 0.5;
			pose.pushPose();
			pose.translate(0, 5.5, z);
			if(arc.isProgressing) pose.mulPose(Axis.XP.rotationDegrees((float) (Math.sin((time + interp) / 2) * 30)));
			pose.translate(0, -5.5, -z);
			ResourceManager.arc_furnace.renderPart("Cable" + (i + 1), pose, buffer, light, overlay);
			pose.popPose();
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineArcFurnaceLarge tile) {
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
				pose.translate(0, -3, 0);
				pose.scale(3.5F, 3.5F, 3.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.scale(0.5F, 0.5F, 0.5F);
				VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.arc_furnace_tex));
				for(String part : new String[] { "Furnace", "Lid", "Ring1", "Ring2", "Ring3", "Electrode1", "Electrode2", "Electrode3", "Cable1", "Cable2", "Cable3" }) {
					ResourceManager.arc_furnace.renderPart(part, pose, buffer, light, overlay);
				}
			}
		};
	}
}
