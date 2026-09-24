package com.hbm.sound;

import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Handle for a looping sound (machine hums, engines). On the server (or without a sound) everything is a
 * no-op, the client version is AudioWrapperClient. Machines create it with {@link #getLoopedSound}, call
 * keepAlive() every tick while running and stopSound() when they stop or unload.
 */
public class AudioWrapper {

	public void setKeepAlive(int keepAlive) { }
	public void keepAlive() { }

	public void updatePosition(float x, float y, float z) { }
	public void attachTo(Entity e) { }

	public void updateVolume(float volume) { }
	public void updateRange(float range) { }

	public void updatePitch(float pitch) { }

	public float getVolume() { return 0F; }
	public float getRange() { return 0F; }

	public float getPitch() { return 0F; }

	public void setDoesRepeat(boolean repeats) { }

	public void startSound() { }

	public void stopSound() { }

	public boolean isPlaying() { return false; }

	/** The original's MainRegistry.proxy.getLoopedSound, sound like "hbm:block.engine" */
	public static AudioWrapper getLoopedSound(String sound, float x, float y, float z, float volume, float range, float pitch) {
		if(FMLEnvironment.dist != Dist.CLIENT) return new AudioWrapper();
		AudioWrapper audio = com.hbm.main.ClientHooks.createAudio(sound);
		audio.updatePosition(x, y, z);
		audio.updateVolume(volume);
		audio.updateRange(range);
		audio.updatePitch(pitch);
		return audio;
	}

	public static AudioWrapper getLoopedSound(String sound, float x, float y, float z, float volume, float range, float pitch, int keepAlive) {
		AudioWrapper audio = getLoopedSound(sound, x, y, z, volume, range, pitch);
		audio.setKeepAlive(keepAlive);
		return audio;
	}
}
