package com.hbm.render.tileentity;

import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.items.ModDataComponents;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;

/** Fluid tank: the frame and the tank shell with the fluid's label texture, the ruptured model after an explosion */
public class RenderFluidTank implements BlockEntityRenderer<TileEntityMachineFluidTank> {

	public RenderFluidTank(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineFluidTank tank, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);

		switch(BlockDummyable.getMeta(tank.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		}

		renderTank(pose, buffers, light, overlay, tank.tank.getTankType(), tank.hasExploded);
		pose.popPose();
	}

	/** The original's getTextureFromType: tinted fluids use the blank label, dangerous ones the warning label */
	public static ResourceLocation getTextureFromType(FluidType type) {
		if(type.renderWithTint) return RefStrings.loc("textures/models/tank/tank_none.png");
		String s = type.getName();
		if(type.isAntimatter() || (type.hasTrait(FT_Corrosive.class) && type.getTrait(FT_Corrosive.class).isHighlyCorrosive())) s = "DANGER";
		return RefStrings.loc("textures/models/tank/tank_" + s.toLowerCase(Locale.US) + ".png");
	}

	private static void renderTank(PoseStack pose, MultiBufferSource buffers, int light, int overlay, FluidType type, boolean exploded) {
		float r = 1F, g = 1F, b = 1F;
		if(type.renderWithTint) {
			int color = type.getTint();
			r = ((color & 0xff0000) >> 16) / 255F;
			g = ((color & 0x00ff00) >> 8) / 255F;
			b = ((color & 0x0000ff) >> 0) / 255F;
		}
		ResourceLocation label = getTextureFromType(type);

		if(!exploded) {
			ResourceManager.fluidtank.renderPart("Frame", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.tank_tex)), light, overlay);
			ResourceManager.fluidtank.renderPart("Tank", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(label)), light, overlay, r, g, b, 1F);
		} else {
			ResourceManager.fluidtank_exploded.renderPart("Frame", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.tank_tex)), light, overlay);
			ResourceManager.fluidtank_exploded.renderPart("TankInner", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceManager.tank_inner_tex)), light, overlay);
			ResourceManager.fluidtank_exploded.renderPart("Tank", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(label)), light, overlay, r, g, b, 1F);
		}
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineFluidTank tile) {
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
				pose.scale(3.5F, 3.5F, 3.5F);
			}

			@Override
			public void renderCommonWithStack(ItemStack item, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.scale(0.75F, 0.75F, 0.75F);

				FluidTank tank = new FluidTank(Fluids.NONE, 0);
				boolean exploded = false;
				CustomData persistent = item.get(ModDataComponents.PERSISTENT.get());
				if(persistent != null) {
					CompoundTag data = persistent.copyTag();
					tank.readFromNBT(data, "tank");
					exploded = data.getBoolean("hasExploded");
				}
				renderTank(pose, buffers, light, overlay, tank.getTankType(), exploded);
			}
		};
	}
}
