package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.items.machine.ItemBatterySC;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Battery socket: battery packs and capacitors show as their own model with the type's texture, schrabidium cells
 * as a battery, anything else floats above the socket.
 *
 * TODO the creative battery's horse and the discharge beams (BeamPronter, HorsePronter)
 */
public class RenderBatterySocket implements BlockEntityRenderer<TileEntityBatterySocket> {

	public RenderBatterySocket(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityBatterySocket socket, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(socket.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		}

		pose.translate(-0.5, 0, 0.5);

		var buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.battery_socket_tex));
		ResourceManager.battery_socket.renderPart("Socket", pose, buffer, light, overlay);

		if(socket.frame) {
			ResourceManager.battery_socket.renderPart("Supports", pose, buffer, light, overlay);
		}

		ItemStack render = socket.syncStack;

		if(!render.isEmpty()) {

			if(render.getItem() instanceof ItemBatteryPack pack) {
				ResourceManager.battery_socket.renderPart(pack.pack.isCapacitor() ? "Capacitor" : "Battery", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(pack.pack.texture)), light, overlay);
			} else if(render.getItem() instanceof ItemBatterySC) {
				ResourceManager.battery_socket.renderPart("Battery", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.battery_sc_tex)), light, overlay);
			} else {
				pose.pushPose();
				pose.translate(0, 0.5, 0);
				pose.scale(1.5F, 1.5F, 1.5F);
				pose.mulPose(Axis.YN.rotationDegrees((float) ((socket.getLevel().getGameTime() % 360 + interp) * 2.5D)));
				pose.translate(0, 0.25, 0);
				Minecraft.getInstance().getItemRenderer().renderStatic(render, ItemDisplayContext.GROUND, light, overlay, pose, buffers, socket.getLevel(), 0);
				pose.popPose();
			}
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityBatterySocket tile) {
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
				pose.translate(0, -2, 0);
				pose.scale(5F, 5F, 5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				ResourceManager.battery_socket.renderPart("Socket", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.battery_socket_tex)), light, overlay);
			}
		};
	}
}
