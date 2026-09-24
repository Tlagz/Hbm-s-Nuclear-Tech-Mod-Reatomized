package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine;
import com.hbm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Base (and the frame if something is on top), the spinning ring with the two striker arms, the recipe's item on the bed */
public class RenderAssemblyMachine implements BlockEntityRenderer<TileEntityMachineAssemblyMachine> {

	public RenderAssemblyMachine(BlockEntityRendererProvider.Context context) { }

	@Override
	public void render(TileEntityMachineAssemblyMachine assembler, float interp, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(90));

		switch(BlockDummyable.getMeta(assembler.getBlockState()) - BlockDummyable.offset) {
		case 2: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 3: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(ResourceManager.assembly_machine_tex));
		ResourceManager.assembly_machine.renderPart("Base", pose, consumer, light, overlay);
		if(assembler.frame) ResourceManager.assembly_machine.renderPart("Frame", pose, consumer, light, overlay);

		pose.pushPose();

		double spin = BobMathUtil.interp(assembler.prevRing, assembler.ring, interp);
		double[] arm1 = assembler.arms[0].getPositions(interp);
		double[] arm2 = assembler.arms[1].getPositions(interp);

		pose.mulPose(Axis.YP.rotationDegrees((float) spin));
		ResourceManager.assembly_machine.renderPart("Ring", pose, consumer, light, overlay);

		pose.pushPose();
		rotateAround(pose, 1.625, 0.9375, arm1[0]);
		ResourceManager.assembly_machine.renderPart("ArmLower1", pose, consumer, light, overlay);
		rotateAround(pose, 2.375, 0.9375, arm1[1]);
		ResourceManager.assembly_machine.renderPart("ArmUpper1", pose, consumer, light, overlay);
		rotateAround(pose, 2.375, 0.4375, arm1[2]);
		ResourceManager.assembly_machine.renderPart("Head1", pose, consumer, light, overlay);
		pose.translate(0, arm1[3], 0);
		ResourceManager.assembly_machine.renderPart("Spike1", pose, consumer, light, overlay);
		pose.popPose();

		pose.pushPose();
		rotateAround(pose, 1.625, -0.9375, -arm2[0]);
		ResourceManager.assembly_machine.renderPart("ArmLower2", pose, consumer, light, overlay);
		rotateAround(pose, 2.375, -0.9375, -arm2[1]);
		ResourceManager.assembly_machine.renderPart("ArmUpper2", pose, consumer, light, overlay);
		rotateAround(pose, 2.375, -0.4375, -arm2[2]);
		ResourceManager.assembly_machine.renderPart("Head2", pose, consumer, light, overlay);
		pose.translate(0, arm2[3], 0);
		ResourceManager.assembly_machine.renderPart("Spike2", pose, consumer, light, overlay);
		pose.popPose();

		pose.popPose();

		GenericRecipe recipe = assembler.assemblerModule.getRecipe();
		var player = Minecraft.getInstance().player;
		if(recipe != null && player != null && player.distanceToSqr(assembler.getBlockPos().getX() + 0.5, assembler.getBlockPos().getY() + 1, assembler.getBlockPos().getZ() + 0.5) < 35 * 35) {

			pose.mulPose(Axis.YP.rotationDegrees(90));
			pose.translate(0, 1.0625, 0);

			ItemStack stack = recipe.getIcon().copyWithCount(1);

			if(stack.getItem() instanceof BlockItem) {
				// a block standing on the bed
				pose.translate(0, 0.25, 0);
			} else {
				// flat items lie on the bed
				pose.translate(0, 0.02, 0);
				pose.mulPose(Axis.XP.rotationDegrees(90));
			}
			pose.scale(0.625F, 0.625F, 0.625F);

			Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, assembler.getLevel(), 0);
		}

		pose.popPose();
	}

	/** glTranslated(0, y, z); glRotated(angle, 1, 0, 0); glTranslated(0, -y, -z) */
	private static void rotateAround(PoseStack pose, double y, double z, double angle) {
		pose.translate(0, y, z);
		pose.mulPose(Axis.XP.rotationDegrees((float) angle));
		pose.translate(0, -y, -z);
	}

	@Override
	public AABB getRenderBoundingBox(TileEntityMachineAssemblyMachine tile) {
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
				ResourceManager.assembly_machine.renderAll(pose, buffers.getBuffer(RenderType.entityCutout(ResourceManager.assembly_machine_tex)), light, overlay);
			}
		};
	}
}
