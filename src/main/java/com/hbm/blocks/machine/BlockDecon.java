package com.hbm.blocks.machine;

import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Decontamination shower, stand on it to get rid of radiation and contamination */
public class BlockDecon extends BlockMachineTile {

	public BlockDecon(Properties properties) {
		super(properties, ModTileEntities.DECON);
	}

	/** Ticks on both sides for the particles */
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return type == ModTileEntities.DECON.get() ? TileEntityLoadedBase.ticker() : null;
	}
}
