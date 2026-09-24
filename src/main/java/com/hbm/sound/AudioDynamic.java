package com.hbm.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * Looping positional sound with linear distance falloff over "range" and an optional keep-alive timeout
 * (the sound stops by itself if the owner stops calling keepAlive, e.g. after the chunk was left).
 */
public class AudioDynamic extends AbstractTickableSoundInstance {

	public float maxVolume = 1;
	public float range;
	public int keepAlive;
	public int timeSinceKA;
	public boolean shouldExpire = false;
	// position updates happen automatically and if the parent is the client player, volume is always on max
	public Entity parentEntity = null;

	protected AudioDynamic(ResourceLocation loc) {
		super(SoundEvent.createVariableRangeEvent(loc), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
		this.looping = true;
		this.delay = 0;
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.range = 10;
	}

	public void setPosition(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void attachTo(Entity e) {
		this.parentEntity = e;
	}

	@Override
	public void tick() {

		LocalPlayer player = Minecraft.getInstance().player;

		if(parentEntity != null && player != parentEntity) {
			this.setPosition((float) parentEntity.getX(), (float) parentEntity.getY(), (float) parentEntity.getZ());
		}

		// only adjust volume over distance if the sound isn't attached to this entity
		if(player != null && player != parentEntity) {
			float f = (float) Math.sqrt(Math.pow(x - player.getX(), 2) + Math.pow(y - player.getY(), 2) + Math.pow(z - player.getZ(), 2));
			volume = Math.max(func(f), 0F);
		} else {
			if(player != null && player == parentEntity) this.setPosition((float) parentEntity.getX(), (float) parentEntity.getY() + 10, (float) parentEntity.getZ());
			volume = maxVolume;
		}

		if(this.shouldExpire) {

			if(this.timeSinceKA > this.keepAlive) {
				this.stop();
			}

			this.timeSinceKA++;
		}
	}

	public void start() {
		Minecraft.getInstance().getSoundManager().play(this);
	}

	/** Stops the sound, the sound engine drops stopped tickable sounds */
	public void stopSound() {
		this.stop();
	}

	public void setVolume(float volume) {
		this.maxVolume = volume;
	}

	public void setRange(float range) {
		this.range = range;
	}

	public void setKeepAlive(int keepAlive) {
		this.keepAlive = keepAlive;
		this.shouldExpire = true;
	}

	public void keepAlive() {
		this.timeSinceKA = 0;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float func(float dist) {
		return (dist / range) * -maxVolume + maxVolume;
	}

	public boolean isPlaying() {
		return !this.isStopped() && Minecraft.getInstance().getSoundManager().isActive(this);
	}
}
