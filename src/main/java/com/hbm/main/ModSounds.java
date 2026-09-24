package com.hbm.main;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.lib.RefStrings;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers every sound event listed in assets/hbm/sounds.json (names lowercased by the asset port script),
 * so code can play them by their original name, e.g. {@code ModSounds.get("player.cough")}.
 */
public class ModSounds {

	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, RefStrings.MODID);
	private static final Map<String, DeferredHolder<SoundEvent, SoundEvent>> BY_NAME = new HashMap<>();

	static {
		try(InputStream in = ModSounds.class.getResourceAsStream("/assets/hbm/sounds.json")) {
			JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for(String name : json.keySet()) {
				BY_NAME.put(name, SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(RefStrings.loc(name))));
			}
		} catch(Exception ex) {
			throw new RuntimeException("Could not read hbm sounds.json", ex);
		}
	}

	/** The registry holder of a sound, for things that need a supplier (e.g. DeferredSoundType) */
	public static DeferredHolder<SoundEvent, SoundEvent> holder(String name) {
		DeferredHolder<SoundEvent, SoundEvent> holder = BY_NAME.get(name.toLowerCase());
		if(holder == null) throw new IllegalArgumentException("Unknown hbm sound " + name);
		return holder;
	}

	/** Looks up a sound by its original (case insensitive) name, e.g. "block.crateBreak" */
	public static SoundEvent get(String name) {
		DeferredHolder<SoundEvent, SoundEvent> holder = BY_NAME.get(name.toLowerCase());
		if(holder == null) throw new IllegalArgumentException("Unknown hbm sound " + name);
		return holder.get();
	}
}
