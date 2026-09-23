package com.hbm.hazard;

import java.util.ArrayList;
import java.util.List;

import com.hbm.hazard.type.HazardTypeBase;

public class HazardData {

	/*
	 * Purges all previously loaded data when read, useful for when specific items should fully override tag data.
	 */
	boolean doesOverride = false;
	/*
	 * MUTEX, even more precise to make only specific entries mutually exclusive, for example tag aliases such as plutonium238 and pu238.
	 * Does the opposite of overrides, if a previous entry collides with this one, this one will yield.
	 *
	 * RESERVED BITS (please keep this up to date)
	 * -1: tags ("ingots/x")
	 */
	int mutexBits = 0;

	List<HazardEntry> entries = new ArrayList<>();

	public HazardData addEntry(HazardTypeBase hazard) {
		return this.addEntry(hazard, 1F, false);
	}

	public HazardData addEntry(HazardTypeBase hazard, float level) {
		return this.addEntry(hazard, level, false);
	}

	public HazardData addEntry(HazardTypeBase hazard, float level, boolean override) {
		this.entries.add(new HazardEntry(hazard, level));
		this.doesOverride = override;
		return this;
	}

	public HazardData addEntry(HazardEntry entry) {
		this.entries.add(entry);
		return this;
	}

	public HazardData setMutex(int mutex) {
		this.mutexBits = mutex;
		return this;
	}

	public int getMutex() {
		return mutexBits;
	}
}
