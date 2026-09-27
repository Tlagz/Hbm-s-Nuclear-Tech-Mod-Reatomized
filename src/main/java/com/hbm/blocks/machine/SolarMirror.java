package com.hbm.blocks.machine;

import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Heliostat mirror, turned by RenderSolarMirror towards the boiler it's aimed at */
public class SolarMirror extends BlockMachineTile {

	public SolarMirror(Properties properties) {
		super(properties.noOcclusion(), ModTileEntities.SOLAR_MIRROR);
	}

	/** Sky light passes like through the original's non-opaque block, the mirror reads it at its own position */
	@Override
	protected boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter world, net.minecraft.core.BlockPos pos) {
		return true;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	/** Ticks on both sides, the client reports the mirror to the boiler for the light beams */
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return type == ModTileEntities.SOLAR_MIRROR.get() ? TileEntityLoadedBase.ticker() : null;
	}
}
