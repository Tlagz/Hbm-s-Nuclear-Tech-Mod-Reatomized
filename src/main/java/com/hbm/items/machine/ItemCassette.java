package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemEnumMulti;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Siren cassettes, one item per track (the original's metadata, where 0 was the empty NULL track; "no track" is
 * null here). The overlay is tinted with the track's color.
 */
public class ItemCassette extends ItemEnumMulti {

	public enum TrackType {

		HATCH(				"Hatch Siren", 				"hbm:alarm.hatch",			SoundType.LOOP,		3358839,	250),
		ATUOPILOT(			"Autopilot Disconnected", 	"hbm:alarm.autopilot",		SoundType.LOOP,		11908533,	50),
		AMS_SIREN(			"AMS Siren", 				"hbm:alarm.amssiren",		SoundType.LOOP,		15055698,	50),
		BLAST_DOOR(			"Blast Door Alarm", 		"hbm:alarm.blastdooralarm",	SoundType.LOOP,		11665408,	50),
		APC_LOOP(			"APC Siren", 				"hbm:alarm.apcloop",		SoundType.LOOP,		3565216,	50),
		KLAXON(				"Klaxon", 					"hbm:alarm.klaxon",			SoundType.LOOP,		8421504,	50),
		KLAXON_A(			"Vault Door Alarm",			"hbm:alarm.foklaxona",		SoundType.LOOP,		0x8c810b,	50),
		KLAXON_B(			"Security Alert", 			"hbm:alarm.foklaxonb",		SoundType.LOOP,		0x76818e,	50),
		SIREN(				"Standard Siren", 			"hbm:alarm.regularsiren",	SoundType.LOOP,		6684672,	100),
		CLASSIC(			"Classic Siren", 			"hbm:alarm.classic",		SoundType.LOOP,		0xc0cfe8,	100),
		BANK_ALARM(			"Bank Alarm", 				"hbm:alarm.bankalarm",		SoundType.LOOP,		3572962,	100),
		BEEP_SIREN(			"Beep Siren", 				"hbm:alarm.beepsiren",		SoundType.LOOP,		13882323,	100),
		CONTAINER_ALARM(	"Container Alarm", 			"hbm:alarm.containeralarm",	SoundType.LOOP,		14727839,	100),
		SWEEP_SIREN(		"Sweep Siren", 				"hbm:alarm.sweepsiren",		SoundType.LOOP,		15592026,	500),
		STRIDER_SIREN(		"Missile Silo Siren", 		"hbm:alarm.stridersiren",	SoundType.LOOP,		11250586,	500),
		AIR_RAID(			"Air Raid Siren", 			"hbm:alarm.airraid",		SoundType.LOOP,		0xDF3795,	500),
		NOSTROMO_SIREN(		"Nostromo Self Destruct",	"hbm:alarm.nostromosiren",	SoundType.LOOP,		0x5dd800,	100),
		EAS_ALARM(			"EAS Alarm Screech",		"hbm:alarm.easalarm",		SoundType.LOOP,		0xb3a8c1,	50),
		APC_PASS(			"APC Pass", 				"hbm:alarm.apcpass",		SoundType.PASS,		3422163,	50),
		RAZORTRAIN(			"Razortrain Horn", 			"hbm:alarm.razortrainhorn",	SoundType.SOUND,	7819501,	250);

		//Name of the track shown in GUI
		private final String title;
		//Location of the sound
		private final String location;
		//Sound type, whether the sound should be repeated or not
		private final SoundType type;
		//Color of the cassette
		private final int color;
		//Range where the sound can be heard
		private final int volume;

		private TrackType(String name, String loc, SoundType sound, int msa, int intensity) {
			title = name;
			location = loc;
			type = sound;
			color = msa;
			volume = intensity;
		}

		public String getTrackTitle() { return title; }
		public String getSoundLocation() { return location; }
		public SoundType getType() { return type; }
		public int getColor() { return color; }
		public int getVolume() { return volume; }
	}

	public enum SoundType {
		LOOP,
		PASS,
		SOUND;
	}

	private final TrackType track;

	public ItemCassette(Properties properties, String descriptionId, TrackType track) {
		super(properties.stacksTo(1), descriptionId);
		this.track = track;
	}

	public TrackType getTrack() {
		return track;
	}

	/** The track on the cassette, null for anything else */
	public static TrackType getType(ItemStack stack) {
		return stack.getItem() instanceof ItemCassette cassette ? cassette.track : null;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal("Siren sound cassette:"));
		list.add(Component.literal("   Name: " + track.getTrackTitle()));
		list.add(Component.literal("   Type: " + track.getType().name()));
		list.add(Component.literal("   Volume: " + track.getVolume()));
	}
}
