package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** The big compressor: a 9 block tall tower with a pumping piston and a fan */
public class TileEntityMachineCompressor extends TileEntityMachineCompressorBase {

	public float fanSpin;
	public float prevFanSpin;
	public float piston;
	public float prevPiston;
	public boolean pistonDir;

	private float randSpeed = 0.1F;

	public TileEntityMachineCompressor(BlockPos pos, BlockState state) {
		super(ModTileEntities.COMPRESSOR.get(), pos, state);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(!isServer()) {

			this.prevFanSpin = this.fanSpin;
			this.prevPiston = this.piston;

			if(this.isOn) {
				this.fanSpin += 15;

				if(this.fanSpin >= 360) {
					this.prevFanSpin -= 360;
					this.fanSpin -= 360;
				}

				if(this.pistonDir) {
					this.piston -= randSpeed;
					if(this.piston <= 0) {
						level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), ModSounds.get("item.boltgun"), SoundSource.BLOCKS, this.getVolume(0.5F), 0.75F, false);
						this.pistonDir = !this.pistonDir;
					}
				} else {
					this.piston += 0.05F;
					if(this.piston >= 1) {
						this.randSpeed = 0.085F + level.random.nextFloat() * 0.03F;
						this.pistonDir = !this.pistonDir;
					}
				}

				this.piston = Mth.clamp(this.piston, 0F, 1F);
			}
		}
	}

	@Override
	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		return new DirPos[] {
				new DirPos(worldPosition.relative(rot, 2), rot),
				new DirPos(worldPosition.relative(rot, -2), rot.getOpposite()),
				new DirPos(worldPosition.relative(dir, -2), dir.getOpposite()),
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 9, worldPosition.getZ() + 3);
	}
}
