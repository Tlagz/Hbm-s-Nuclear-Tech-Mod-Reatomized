package com.hbm.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Non-multiblock blocks with a proxy tile that forwards to some other tile, e.g. the alloy furnace extension */
public interface IProxyController {

	public BlockEntity getCore(Level world, BlockPos pos);
}
