package com.hbm.tileentity.machine;

import com.hbm.inventory.material.NTMMaterial;
import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Half block mold for small molds, filled from channels or by pouring */
public class TileEntityFoundryMold extends TileEntityFoundryCastingBase implements IRenderFoundry {

	public TileEntityFoundryMold(BlockPos pos, BlockState state) {
		super(ModTileEntities.FOUNDRY_MOLD.get(), pos, state);
	}

	@Override
	public int getMoldSize() {
		return 0;
	}

	@Override
	public boolean shouldRender() {
		return this.type != null && this.amount > 0;
	}

	@Override
	public double getFluidLevel() {
		return 0.125 + this.amount * 0.25D / Math.max(this.getCapacity(), 1);
	}

	@Override
	public NTMMaterial getMat() {
		return this.type;
	}

	@Override public double minX() { return 0.125D; }
	@Override public double maxX() { return 0.875D; }
	@Override public double minZ() { return 0.125D; }
	@Override public double maxZ() { return 0.875D; }
	@Override public double moldHeight() { return 0.13D; }
	@Override public double outHeight() { return 0.25D; }
}
