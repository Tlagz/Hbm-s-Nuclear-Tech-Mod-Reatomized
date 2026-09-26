package com.hbm.particle;

import java.awt.Color;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Burning gas: a full bright smoke puff (vanilla smoke sprites) going from yellow over red to dark. The original
 * extended EntitySmokeFX and kept its vertical speed through the smoke update.
 */
public class ParticleGasFlame extends TextureSheetParticle {

	private final SpriteSet sprites;
	private final float colorMod;
	private final float baseSize;

	public ParticleGasFlame(ClientLevel world, double x, double y, double z, double mX, double mY, double mZ, float scale, SpriteSet sprites) {
		super(world, x, y, z);
		this.sprites = sprites;
		this.xd = mX;
		this.yd = mY * 1.5;
		this.zd = mZ;
		this.baseSize = 0.1F * scale;
		this.quadSize = baseSize;
		this.colorMod = 0.8F + random.nextFloat() * 0.2F;
		this.hasPhysics = false;
		this.lifetime = 30 + random.nextInt(13);
		this.setSpriteFromAge(sprites);
		updateColor();
	}

	@Override
	public void tick() {
		double prevMo = this.yd;

		// EntitySmokeFX.onUpdate
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if(this.age++ >= this.lifetime) {
			this.remove();
			return;
		}
		this.setSpriteFromAge(sprites);
		this.yd += 0.004D;
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.96D;
		this.yd *= 0.96D;
		this.zd *= 0.96D;

		updateColor();
		this.yd = prevMo;

		this.xd *= 0.75D;
		this.yd += 0.005D;
		this.zd *= 0.75D;
	}

	protected void updateColor() {
		float time = (float) this.age / (float) this.lifetime;

		Color color = Color.getHSBColor(Math.max((60 - time * 100) / 360F, 0.0F), 1 - time * 0.25F, 1 - time * 0.5F);

		this.rCol = color.getRed() / 255F * colorMod;
		this.gCol = color.getGreen() / 255F * colorMod;
		this.bCol = color.getBlue() / 255F * colorMod;
	}

	/** EntitySmokeFX grew in over the first 32nd of its life */
	@Override
	public float getQuadSize(float partialTicks) {
		return baseSize * Mth.clamp((this.age + partialTicks) / this.lifetime * 32.0F, 0.0F, 1.0F);
	}

	@Override
	protected int getLightColor(float partialTick) {
		return LightTexture.FULL_BRIGHT;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public AABB getRenderBoundingBox(float partialTicks) {
		return super.getRenderBoundingBox(partialTicks).inflate(this.quadSize);
	}
}
