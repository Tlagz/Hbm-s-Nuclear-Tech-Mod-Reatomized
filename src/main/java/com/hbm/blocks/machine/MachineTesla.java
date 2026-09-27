package com.hbm.blocks.machine;

import com.hbm.tileentity.ModTileEntities;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/** Tesla coil, rendered with its lightning by RenderTesla */
public class MachineTesla extends BlockMachineTile {

	public MachineTesla(Properties properties) {
		super(properties.noOcclusion(), ModTileEntities.TESLA);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
}
