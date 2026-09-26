package com.hbm.tileentity.machine.storage;

import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Big-Ass Tank 9000, a 2,048,000mB barrel with ports next to its eight side pillars */
public class TileEntityMachineBAT9000 extends TileEntityBarrel {

	public TileEntityMachineBAT9000(BlockPos pos, BlockState state) {
		super(ModTileEntities.BAT9000.get(), pos, state, 2048000);
	}

	@Override
	public String getName() {
		return "container.bat9000";
	}

	@Override
	public void checkFluidInteraction() {
		if(tank.getTankType().isAntimatter()) {
			level.destroyBlock(worldPosition, false);
			level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 10, true, Level.ExplosionInteraction.BLOCK);
		}
	}

	@Override
	protected DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.offset(1, 0, 3), Direction.SOUTH),
				new DirPos(p.offset(-1, 0, 3), Direction.SOUTH),
				new DirPos(p.offset(1, 0, -3), Direction.NORTH),
				new DirPos(p.offset(-1, 0, -3), Direction.NORTH),
				new DirPos(p.offset(3, 0, 1), Direction.EAST),
				new DirPos(p.offset(-3, 0, 1), Direction.WEST),
				new DirPos(p.offset(3, 0, -1), Direction.EAST),
				new DirPos(p.offset(-3, 0, -1), Direction.WEST)
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 5, worldPosition.getZ() + 3);
	}
}
