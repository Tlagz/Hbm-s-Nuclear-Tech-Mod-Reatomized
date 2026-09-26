package com.hbm.tileentity.machine.oil;

import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** The fractioning tower separator, only there to be rendered */
public class TileEntitySpacer extends BlockEntity {

	public TileEntitySpacer(BlockPos pos, BlockState state) {
		super(ModTileEntities.SPACER.get(), pos, state);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 1, worldPosition.getZ() + 2);
	}
}
