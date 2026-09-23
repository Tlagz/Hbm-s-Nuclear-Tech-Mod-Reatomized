package com.hbm.util.i18n;

import java.util.IllegalFormatException;

import net.minecraft.locale.Language;

/**
 * Wrapper for translations, same API as the original. Uses the active {@link Language}, which is the
 * client's selected language on the client and the NeoForge-loaded en_us on the server.
 *
 * TODO autoBreak / autoBreakWithParagraphs (GUI text wrapping) when GUIs get ported
 */
public class I18nUtil {

	public static String resolveKey(String s, Object... args) {
		String translated = Language.getInstance().getOrDefault(s);
		if(args.length == 0) return translated;
		try {
			return String.format(translated, args);
		} catch(IllegalFormatException ex) {
			return "Format error: " + translated;
		}
	}

	/** alias */
	public static String format(String s, Object... args) {
		return resolveKey(s, args);
	}

	/** Cuts up the result using NTM's line break character ($) */
	public static String[] resolveKeyArray(String s, Object... args) {
		return resolveKey(s, args).split("\\$");
	}
}
