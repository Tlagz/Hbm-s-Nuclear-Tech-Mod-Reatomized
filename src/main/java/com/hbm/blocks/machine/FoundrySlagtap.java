package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityFoundrySlagtap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** An outlet that dumps its contents on the ground as slag, textured by datagen with the foundry_slagtap_ set */
public class FoundrySlagtap extends FoundryOutlet {

	public FoundrySlagtap(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityFoundrySlagtap(pos, state);
	}
}
