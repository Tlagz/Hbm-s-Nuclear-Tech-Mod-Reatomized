package com.hbm.tileentity.machine;

import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Full block basin for the large molds, only filled by pouring from above */
public class TileEntityFoundryBasin extends TileEntityFoundryCastingBase implements IRenderFoundry {

	public TileEntityFoundryBasin(BlockPos pos, BlockState state) {
		super(ModTileEntities.FOUNDRY_BASIN.get(), pos, state);
	}

	@Override
	public int getMoldSize() {
		return 1;
	}

	/* Basin can't accept sideways flowing */
	@Override public boolean canAcceptPartialFlow(Level world, BlockPos pos, Direction side, MaterialStack stack) { return false; }
	@Override public MaterialStack flow(Level world, BlockPos pos, Direction side, MaterialStack stack) { return stack; }

	@Override
	public boolean shouldRender() {
		return this.type != null && this.amount > 0;
	}

	@Override
	public double getFluidLevel() {
		return 0.125 + this.amount * 0.75D / Math.max(this.getCapacity(), 1);
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
	@Override public double outHeight() { return 0.875D; }
}
