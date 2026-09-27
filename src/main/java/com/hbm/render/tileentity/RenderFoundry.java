package com.hbm.render.tileentity;

import com.hbm.blocks.machine.FoundryChannel;
import com.hbm.blocks.machine.FoundryTank;
import com.hbm.tileentity.machine.TileEntityFoundryTank;
import net.minecraft.core.Direction;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.IRenderFoundry;
import com.hbm.tileentity.machine.TileEntityFoundryBase;
import com.hbm.tileentity.machine.TileEntityFoundryCastingBase;
import com.hbm.tileentity.machine.TileEntityFoundryChannel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

/**
 * Molten material in molds, basins and channels (glowing, in the material's molten color), plus the installed mold
 * and the cast item lying in molds and basins. The original drew the channel contents in its block renderer.
 */
public class RenderFoundry implements BlockEntityRenderer<TileEntityFoundryBase> {

	public static final ResourceLocation lava = RefStrings.loc("textures/models/machines/lava_gray.png");

	public RenderFoundry(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityFoundryBase tile, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {

		if(tile instanceof TileEntityFoundryCastingBase cast && tile instanceof IRenderFoundry foundry) {
			ItemStack mold = cast.slots.get(0);
			if(!mold.isEmpty()) drawItem(tile, pose, buffers, mold, foundry.moldHeight(), light);

			ItemStack out = cast.slots.get(1);
			if(!out.isEmpty()) {
				if(out.getItem() instanceof BlockItem) drawItem(tile, pose, buffers, out, foundry.outHeight() - 0.0625, light);
				else drawItem(tile, pose, buffers, out, foundry.outHeight(), light);
			}

			if(foundry.shouldRender()) {
				VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(lava));
				int color = brighter(foundry.getMat().moltenColor);
				top(pose, buffer, color, foundry.minX(), foundry.maxX(), foundry.minZ(), foundry.maxZ(), foundry.getFluidLevel());
			}
		}

		if(tile instanceof TileEntityFoundryChannel channel && channel.amount > 0 && channel.type != null) {
			VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(lava));
			int color = brighter(channel.type.moltenColor);
			double level = 0.125D + channel.amount * 0.25D / channel.getCapacity();
			BlockState state = channel.getBlockState();

			top(pose, buffer, color, 0.375, 0.625, 0.375, 0.625, level);
			if(state.getBlock() instanceof FoundryChannel) {
				if(state.getValue(FoundryChannel.EAST)) top(pose, buffer, color, 0.625, 1, 0.3125, 0.6875, level);
				if(state.getValue(FoundryChannel.WEST)) top(pose, buffer, color, 0, 0.375, 0.3125, 0.6875, level);
				if(state.getValue(FoundryChannel.SOUTH)) top(pose, buffer, color, 0.3125, 0.6875, 0.625, 1, level);
				if(state.getValue(FoundryChannel.NORTH)) top(pose, buffer, color, 0.3125, 0.6875, 0, 0.375, level);
			}
		}

		// tanks: the surface plus the sides towards connected tanks, the floor is gone when there's a tank below
		if(tile instanceof TileEntityFoundryTank tank && tank.amount > 0 && tank.type != null && tank.getBlockState().getBlock() instanceof FoundryTank) {
			BlockState state = tank.getBlockState();
			boolean conNegY = state.getValue(FoundryTank.DOWN);
			boolean conPosY = state.getValue(FoundryTank.UP);
			double max = 0.75D + (conNegY ? 0.125D : 0) + (conPosY ? 0.125D : 0);
			double height = conNegY ? 0D : 0.125D;
			double top = height + tank.amount * max / tank.getCapacity();

			int c = brighter(tank.type.moltenColor);
			int r = (int) (255D - (255D - ((c >> 16) & 0xFF)) * 0.7D);
			int g = (int) (255D - (255D - ((c >> 8) & 0xFF)) * 0.7D);
			int b = (int) (255D - (255D - (c & 0xFF)) * 0.7D);
			int color = (r << 16) | (g << 8) | b;

			VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(lava));
			top(pose, buffer, color, 0, 1, 0, 1, top);
			for(Direction dir : Direction.Plane.HORIZONTAL) {
				if(state.getValue(FoundryTank.connection(dir))) side(pose, buffer, color, dir, height, top);
			}
		}
	}

	/** A fullbright vertical quad of the molten texture on the given side of the block */
	private static void side(PoseStack pose, VertexConsumer buffer, int color, Direction dir, double y0, double y1) {
		Matrix4f m = pose.last().pose();
		int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
		double x0, z0, x1, z1;
		switch(dir) {
		case NORTH: x0 = 0; z0 = 0; x1 = 1; z1 = 0; break;
		case SOUTH: x0 = 0; z0 = 1; x1 = 1; z1 = 1; break;
		case WEST: x0 = 0; z0 = 0; x1 = 0; z1 = 1; break;
		default: x0 = 1; z0 = 0; x1 = 1; z1 = 1; break;
		}
		float nx = dir.getStepX(), nz = dir.getStepZ();
		float v0 = (float) (1 - y1), v1 = (float) (1 - y0);
		buffer.addVertex(m, (float) x0, (float) y0, (float) z0).setColor(r, g, b, 255).setUv(0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), nx, 0, nz);
		buffer.addVertex(m, (float) x1, (float) y0, (float) z1).setColor(r, g, b, 255).setUv(1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), nx, 0, nz);
		buffer.addVertex(m, (float) x1, (float) y1, (float) z1).setColor(r, g, b, 255).setUv(1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), nx, 0, nz);
		buffer.addVertex(m, (float) x0, (float) y1, (float) z0).setColor(r, g, b, 255).setUv(0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), nx, 0, nz);
	}

	/** The original's Color.brighter() */
	private static int brighter(int hex) {
		int r = (hex >> 16) & 0xFF, g = (hex >> 8) & 0xFF, b = hex & 0xFF;
		int i = 3;
		if(r == 0 && g == 0 && b == 0) return 0x030303;
		if(r > 0 && r < i) r = i;
		if(g > 0 && g < i) g = i;
		if(b > 0 && b < i) b = i;
		return (Math.min((int) (r / 0.7), 255) << 16) | (Math.min((int) (g / 0.7), 255) << 8) | Math.min((int) (b / 0.7), 255);
	}

	/** A fullbright horizontal quad of the molten texture, UVs from the position like the original */
	private static void top(PoseStack pose, VertexConsumer buffer, int color, double minX, double maxX, double minZ, double maxZ, double y) {
		Matrix4f m = pose.last().pose();
		int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
		vertex(buffer, m, pose, minX, y, minZ, (float) minZ, (float) maxX, r, g, b);
		vertex(buffer, m, pose, minX, y, maxZ, (float) maxZ, (float) maxX, r, g, b);
		vertex(buffer, m, pose, maxX, y, maxZ, (float) maxZ, (float) minX, r, g, b);
		vertex(buffer, m, pose, maxX, y, minZ, (float) minZ, (float) minX, r, g, b);
	}

	private static void vertex(VertexConsumer buffer, Matrix4f m, PoseStack pose, double x, double y, double z, float u, float v, int r, int g, int b) {
		buffer.addVertex(m, (float) x, (float) y, (float) z).setColor(r, g, b, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 1, 0);
	}

	/** An item lying flat in the mold at the given height */
	private static void drawItem(TileEntityFoundryBase tile, PoseStack pose, MultiBufferSource buffers, ItemStack stack, double height, int light) {
		pose.pushPose();
		pose.translate(0.5, height + 0.01, 0.5);
		pose.mulPose(Axis.XP.rotationDegrees(90));
		pose.scale(0.75F, 0.75F, 0.75F);
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, tile.getLevel(), 0);
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen(TileEntityFoundryBase tile) {
		return false;
	}
}
