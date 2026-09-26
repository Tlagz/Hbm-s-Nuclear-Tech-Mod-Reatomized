package com.hbm.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.phys.AABB;

/** Big slow steam puff that rises, grows and drifts with the wind, from cooling towers and the like */
public class ParticleCoolingTower extends TextureSheetParticle {

	private float baseScale = 1.0F;
	private float maxScale = 1.0F;
	private float lift = 0.3F;
	private float strafe = 0.075F;
	private boolean windDir = true;
	private float alphaMod = 0.25F;

	public ParticleCoolingTower(ClientLevel world, double x, double y, double z, SpriteSet sprites) {
		super(world, x, y, z);
		this.setSprite(sprites.get(0, 1));
		this.rCol = this.gCol = this.bCol = 0.9F + world.random.nextFloat() * 0.05F;
		this.hasPhysics = false;
	}

	public void setBaseScale(float f) { this.baseScale = f; }
	public void setMaxScale(float f) { this.maxScale = f; }
	public void setLift(float f) { this.lift = f; }
	public void setLife(int i) { this.lifetime = i; }
	public void setStrafe(float f) { this.strafe = f; }
	public void noWind() { this.windDir = false; }
	public void alphaMod(float mod) { this.alphaMod = mod; }

	@Override
	public void tick() {

		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		float ageScale = (float) this.age / (float) this.lifetime;

		this.alpha = alphaMod - ageScale * alphaMod;
		this.quadSize = baseScale + (float) Math.pow((maxScale * ageScale - baseScale), 2);

		this.age++;

		if(lift > 0 && this.yd < this.lift) {
			this.yd += 0.01F;
		}
		if(lift < 0 && this.yd > this.lift) {
			this.yd -= 0.01F;
		}

		this.xd += random.nextGaussian() * strafe * ageScale;
		this.zd += random.nextGaussian() * strafe * ageScale;

		if(windDir) {
			this.xd += 0.02 * ageScale;
			this.zd -= 0.01 * ageScale;
		}

		if(this.age >= this.lifetime) {
			this.remove();
		}

		this.move(this.xd, this.yd, this.zd);

		this.xd *= 0.925;
		this.yd *= 0.925;
		this.zd *= 0.925;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	/** The puffs grow to several blocks, the tiny collision box would get them culled while still on screen */
	@Override
	public AABB getRenderBoundingBox(float partialTicks) {
		return super.getRenderBoundingBox(partialTicks).inflate(this.quadSize);
	}
}
