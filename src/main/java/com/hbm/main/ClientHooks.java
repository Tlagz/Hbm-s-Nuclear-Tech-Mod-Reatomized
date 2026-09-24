package com.hbm.main;

import com.hbm.inventory.gui.GUIScreenFluid;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Client only actions triggered from common code (items opening screens), the original's
 * player.openGui(...) with IGUIProvider. Only call these when level.isClientSide.
 */
public class ClientHooks {

	public static void openFluidIdentifierScreen(Player player) {
		Minecraft.getInstance().setScreen(new GUIScreenFluid(player));
	}

	/** Looping sound for AudioWrapper.getLoopedSound, names are lowercased like all ported sounds */
	public static com.hbm.sound.AudioWrapper createAudio(String sound) {
		return new com.hbm.sound.AudioWrapperClient(net.minecraft.resources.ResourceLocation.parse(sound.toLowerCase(java.util.Locale.US)));
	}
}
