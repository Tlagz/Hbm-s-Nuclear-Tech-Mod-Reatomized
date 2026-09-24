package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineChemicalPlant;
import com.hbm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/**
 * Base (and the frame if something is on top), the sliding pump and the spinner while working,
 * and the fluid in the tank tinted with the recipe's fluids and slowly scrolling.
 */
public class RenderChemicalPlant implements BlockEntityRenderer<TileEntityMachineChemicalPlant> {

	public RenderChemicalPlant(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineChemicalPlant chemplant, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(90));

		switch(BlockDummyable.getMeta(chemplant.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		float anim = chemplant.prevAnim + (chemplant.anim - chemplant.prevAnim) * interp;
		GenericRecipe recipe = chemplant.chemplantModule.getRecipe();

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.chemical_plant_tex));
		ResourceManager.chemical_plant.renderPart("Base", pose, consumer, light, overlay);
		if(chemplant.frame) ResourceManager.chemical_plant.renderPart("Frame", pose, consumer, light, overlay);

		pose.pushPose();
		pose.translate(BobMathUtil.sps(anim * 0.125) * 0.375, 0, 0);
		ResourceManager.chemical_plant.renderPart("Slider", pose, consumer, light, overlay);
		pose.popPose();

		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees((float) ((anim * 15) % 360D)));
		pose.translate(-0.5, 0, -0.5);
		ResourceManager.chemical_plant.renderPart("Spinner", pose, consumer, light, overlay);
		pose.popPose();

		if(chemplant.didProcess && recipe != null) {
			int colors = 0;
			int r = 0;
			int g = 0;
			int b = 0;
			if(recipe.outputFluid != null) for(FluidStack stack : recipe.outputFluid) {
				int color = stack.type.getColor();
				r += (color >> 16) & 0xFF;
				g += (color >> 8) & 0xFF;
				b += color & 0xFF;
				colors++;
			}

			if(colors == 0 && recipe.inputFluid != null) for(FluidStack stack : recipe.inputFluid) {
				int color = stack.type.getColor();
				r += (color >> 16) & 0xFF;
				g += (color >> 8) & 0xFF;
				b += color & 0xFF;
				colors++;
			}

			if(colors > 0) {
				// unlit, half transparent, the texture scrolls sideways and bobs up and down
				VertexConsumer fluid = buffers.getBuffer(RenderType.entityTranslucent(ResourceManager.chemical_plant_fluid_tex));
				float du = -anim / 100F;
				float dv = (float) (BobMathUtil.sps(anim * 0.1) * 0.1 - 0.25);
				ResourceManager.chemical_plant.renderPartShifted("Fluid", pose, fluid, LightTexture.FULL_BRIGHT, overlay,
						r / 255F / colors, g / 255F / colors, b / 255F / colors, 0.5F, du, dv);
			}
		}

		pose.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineChemicalPlant tile) {
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
				pose.translate(0, -2.75, 0);
				pose.scale(4.5F, 4.5F, 4.5F);
			}

			@Override
			public void renderCommon(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
				pose.mulPose(Axis.YP.rotationDegrees(90));
				pose.scale(0.75F, 0.75F, 0.75F);
				VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.chemical_plant_tex));
				ResourceManager.chemical_plant.renderPart("Base", pose, consumer, light, overlay);
				ResourceManager.chemical_plant.renderPart("Slider", pose, consumer, light, overlay);
				ResourceManager.chemical_plant.renderPart("Spinner", pose, consumer, light, overlay);
				ResourceManager.chemical_plant.renderPart("Frame", pose, consumer, light, overlay);
			}
		};
	}
}
