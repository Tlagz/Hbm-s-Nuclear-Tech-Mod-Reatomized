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
}
