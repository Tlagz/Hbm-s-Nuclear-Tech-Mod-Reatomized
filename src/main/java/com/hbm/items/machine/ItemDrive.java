package com.hbm.items.machine;

/** Flash drives and disks, one item per type (ModItems.drive) */
public class ItemDrive {

	public static enum EnumDriveType {
		FLASH_EMPTY,
		DISK_EMPTY,
		FLASH_BROKEN,
		DISK_BROKEN,
		FLASH_FLIGHTSIM,			// precalc for spaceflight
		FLASH_PARTICLESIM,			// precalc for fusion
		DISK_FLIGHTDATA,			// raw data from satellite
		DISK_FLIGHTDATA_PROCESSED,	// processed data from satellite
		DISK_ORBITDATA,				// raw sensor relay data
		DISK_ORBITDATA_PROCESSED,	// processed data from sensor relay
		KLAUS,						// kkklanker
	}
}
