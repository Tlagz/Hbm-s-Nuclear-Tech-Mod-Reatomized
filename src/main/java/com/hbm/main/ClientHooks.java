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

	/**
	 * Upgrade tooltips inside a machine GUI: the machine (the container of the menu's first slot) describes what
	 * the upgrade does for it. Returns false if there is no such machine or it doesn't handle this upgrade.
	 */
	public static boolean provideUpgradeInfo(com.hbm.items.machine.ItemMachineUpgrade.UpgradeType type, int tier, java.util.List<String> info, boolean extended) {
		if(Minecraft.getInstance().screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen) {
			var slots = screen.getMenu().slots;
			if(!slots.isEmpty() && slots.get(0).container instanceof com.hbm.tileentity.IUpgradeInfoProvider provider) {
				if(provider.canProvideInfo(type, tier, extended)) {
					provider.provideInfo(type, tier, info, extended);
					return true;
				}
			}
		}
		return false;
	}

	/** Looping sound for AudioWrapper.getLoopedSound, names are lowercased like all ported sounds */
	public static com.hbm.sound.AudioWrapper createAudio(String sound) {
		return new com.hbm.sound.AudioWrapperClient(net.minecraft.resources.ResourceLocation.parse(sound.toLowerCase(java.util.Locale.US)));
	}
}
