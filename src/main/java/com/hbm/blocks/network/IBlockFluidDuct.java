package com.hbm.blocks.network;

import com.hbm.inventory.fluid.FluidType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IBlockFluidDuct {

	public void changeTypeRecursively(Level world, BlockPos pos, FluidType prevType, FluidType type, int loopsRemaining);
}
