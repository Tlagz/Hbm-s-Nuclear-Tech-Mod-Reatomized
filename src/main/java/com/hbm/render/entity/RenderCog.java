package com.hbm.render.entity;

import com.hbm.entity.projectile.EntityCog;
import com.hbm.main.ResourceManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** The Stirling engine's gear flying around, spinning until it's stuck */
public class RenderCog extends EntityRenderer<EntityCog> {

	public RenderCog(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(EntityCog cog, float yaw, float interp, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();

		int orientation = cog.getOrientation();
		switch(orientation % 6) {
		case 3: pose.mulPose(Axis.YP.rotationDegrees(0)); break;
		case 5: pose.mulPose(Axis.YP.rotationDegrees(90)); break;
		case 2: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
		case 4: pose.mulPose(Axis.YP.rotationDegrees(270)); break;
		}

		pose.translate(0, 0, -1);

		if(orientation < 6) {
			pose.mulPose(Axis.ZN.rotationDegrees((float) (System.currentTimeMillis() % (360 * 3) / 3D)));
		}

		pose.translate(0, -1.375, 0);

		ResourceManager.stirling.renderPart("Cog", pose, buffers.getBuffer(RenderType.entityCutout(getTextureLocation(cog))), light, OverlayTexture.NO_OVERLAY);

		pose.popPose();
		super.render(cog, yaw, interp, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityCog cog) {
		return switch(cog.getMeta()) {
		case 1 -> ResourceManager.stirling_steel_tex;
		case 2 -> ResourceManager.stirling_creative_tex;
		default -> ResourceManager.stirling_tex;
		};
	}
}
