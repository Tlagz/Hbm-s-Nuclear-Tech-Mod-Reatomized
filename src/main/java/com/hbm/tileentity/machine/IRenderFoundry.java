package com.hbm.tileentity.machine;

import com.hbm.inventory.material.NTMMaterial;

/** Molds and basins: where RenderFoundry draws the molten metal layer, the mold and the cast item */
public interface IRenderFoundry {

	/** Returns whether a molten metal layer should be rendered in the TESR */
	public boolean shouldRender();
	/** Returns the Y-offset of the molten metal layer */
	public double getFluidLevel();
	/** Returns the NTM Mat used, mainly for the color */
	public NTMMaterial getMat();

	/* Return size constraints for the rectangle */
	public double minX();
	public double maxX();
	public double minZ();
	public double maxZ();
	public double moldHeight();
	public double outHeight();
}
