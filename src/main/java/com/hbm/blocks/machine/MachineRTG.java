package com.hbm.blocks.machine;

import com.hbm.tileentity.ModTileEntities;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/** RT generator, drawn by its tile renderer (the generator and a connector towards every cable) */
public class MachineRTG extends BlockMachineTile {

	public MachineRTG(Properties properties) {
		super(properties.noOcclusion(), ModTileEntities.RTG);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
}
