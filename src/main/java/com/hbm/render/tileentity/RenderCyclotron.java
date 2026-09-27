package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * Cyclotron: the ring plus the four plug sockets, filled or empty. With all four plugs in, a ring of Standard
 * Galactic letters circles above it.
 */
public class RenderCyclotron implements BlockEntityRenderer<TileEntityMachineCyclotron> {

	private static final Style GALACTIC = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"));

	public RenderCyclotron(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineCyclotron cyc, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5D, 0D, 0.5D);

		ResourceManager.cyclotron.renderPart("Body", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.cyclotron_tex)), light, overlay);

		boolean plugged = true;
		ResourceLocation[][] plugs = {
				{ResourceManager.cyclotron_ashes, ResourceManager.cyclotron_ashes_filled},
				{ResourceManager.cyclotron_book, ResourceManager.cyclotron_book_filled},
				{ResourceManager.cyclotron_gavel, ResourceManager.cyclotron_gavel_filled},
				{ResourceManager.cyclotron_coin, ResourceManager.cyclotron_coin_filled}};

		for(int i = 0; i < 4; i++) {
			boolean filled = cyc.getPlug(i);
			if(!filled) plugged = false;
			ResourceManager.cyclotron.renderPart("B" + (i + 1), pose, buffers.getBuffer(RenderType.entityCutoutNoCull(plugs[i][filled ? 1 : 0])), light, overlay);
		}

		if(plugged) {
			pose.pushPose();
			pose.mulPose(Axis.YP.rotationDegrees((float) (System.currentTimeMillis() * 0.025 % 360)));

			String msg = "plures necat crapula quam gladius";
			Font font = Minecraft.getInstance().font;

			pose.translate(0, 2, 0);
			pose.mulPose(Axis.XP.rotationDegrees(180));

			float rot = 0F;

			//looks dumb but we'll use this technology for the cyclotron
			for(char c : msg.toCharArray()) {
				pose.pushPose();
				pose.mulPose(Axis.YP.rotationDegrees(rot));
				rot -= font.width(String.valueOf(c)) * 2F;
				pose.translate(2.75, 0, 0);
				pose.mulPose(Axis.YP.rotationDegrees(-90));
				pose.scale(0.1F, 0.1F, 0.1F);
				font.drawInBatch(Component.literal(String.valueOf(c)).withStyle(GALACTIC), 0, 0, 0x600060, false, pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
				pose.popPose();
			}

			pose.popPose();
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineCyclotron tile) {
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
				ResourceManager.cyclotron.renderPart("Body", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.cyclotron_tex)), light, overlay);
				ResourceManager.cyclotron.renderPart("B1", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.cyclotron_ashes)), light, overlay);
				ResourceManager.cyclotron.renderPart("B2", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.cyclotron_book)), light, overlay);
				ResourceManager.cyclotron.renderPart("B3", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.cyclotron_gavel)), light, overlay);
				ResourceManager.cyclotron.renderPart("B4", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.cyclotron_coin)), light, overlay);
			}
		};
	}
}
