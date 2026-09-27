package com.hbm.blocks.machine;

import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Siren, plays its cassette while powered */
public class MachineSiren extends BlockMachineTile {

	public MachineSiren(Properties properties) {
		super(properties, ModTileEntities.SIREN);
	}

	/** Ticks on both sides, the client plays the sound */
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return type == ModTileEntities.SIREN.get() ? TileEntityLoadedBase.ticker() : null;
	}
}
