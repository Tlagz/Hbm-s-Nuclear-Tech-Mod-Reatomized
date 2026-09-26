package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** The compact compressor: the powered condenser's body with two fans, ports on the second layer */
public class TileEntityMachineCompressorCompact extends TileEntityMachineCompressorBase {

	public float fanSpin;
	public float prevFanSpin;

	public TileEntityMachineCompressorCompact(BlockPos pos, BlockState state) {
		super(ModTileEntities.COMPRESSOR_COMPACT.get(), pos, state);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(!isServer()) {
			this.prevFanSpin = this.fanSpin;

			if(this.isOn) {
				this.fanSpin += 45;

				if(this.fanSpin >= 360) {
					this.prevFanSpin -= 360;
					this.fanSpin -= 360;
				}
			}
		}
	}

	@Override
	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition.above();
		return new DirPos[] {
				new DirPos(p.relative(rot, 4), rot),
				new DirPos(p.relative(rot, -4), rot.getOpposite()),
				new DirPos(p.relative(dir, 2).relative(rot, -1), dir),
				new DirPos(p.relative(dir, 2).relative(rot), dir),
				new DirPos(p.relative(dir, -2).relative(rot, -1), dir.getOpposite()),
				new DirPos(p.relative(dir, -2).relative(rot), dir.getOpposite())
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 3, worldPosition.getY(), worldPosition.getZ() - 3, worldPosition.getX() + 4, worldPosition.getY() + 3, worldPosition.getZ() + 4);
	}
}
