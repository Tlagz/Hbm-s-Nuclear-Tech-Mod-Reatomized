package com.hbm.tileentity.machine;

import java.util.HashSet;
import java.util.Set;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.DirPos;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Ground water pump base: needs a solid layer below and mostly dirt/sand down to {@link #groundDepth}, only works up
 * to {@link #groundHeight}. Unlike the original the tanks are saved.
 *
 * TODO config (IConfigurableMachine), copy tool, waste_earth as valid ground
 */
public abstract class TileEntityMachinePumpBase extends TileEntityLoadedBase implements IFluidStandardTransceiverMK2 {

	private static Set<Block> validBlocks;

	public static Set<Block> getValidBlocks() {
		if(validBlocks == null) {
			validBlocks = new HashSet<>();
			validBlocks.add(Blocks.GRASS_BLOCK);
			validBlocks.add(Blocks.DIRT);
			validBlocks.add(Blocks.SAND);
			validBlocks.add(Blocks.MYCELIUM);
			validBlocks.add(ModBlocks.dirt_dead.get());
			validBlocks.add(ModBlocks.dirt_oily.get());
			validBlocks.add(ModBlocks.sand_dirty.get());
			validBlocks.add(ModBlocks.sand_dirty_red.get());
		}
		return validBlocks;
	}

	public FluidTank water;
	public boolean isOn = false;
	public float rotor;
	public float lastRotor;
	public boolean onGround = false;
	public int groundCheckDelay = 0;

	public static int groundHeight = 70;
	public static int groundDepth = 4;
	public static int steamSpeed = 1_000;
	public static int electricSpeed = 10_000;

	public TileEntityMachinePumpBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			for(DirPos pos : getConPos()) {
				if(water.getFill() > 0) this.tryProvide(water, level, pos, pos.getDir());
			}

			if(groundCheckDelay > 0) {
				groundCheckDelay--;
			} else {
				onGround = this.checkGround();
			}

			this.isOn = false;
			if(this.canOperate() && worldPosition.getY() <= groundHeight && onGround) {
				this.isOn = true;
				this.operate();
			}

			networkPackNT(150);
		} else {

			this.lastRotor = this.rotor;
			if(this.isOn) this.rotor += 10F;

			if(this.rotor >= 360F) {
				this.rotor -= 360F;
				this.lastRotor -= 360F;

				double x = worldPosition.getX(), y = worldPosition.getY(), z = worldPosition.getZ();
				level.playLocalSound(x, y, z, ModSounds.get("block.steamEngineOperate"), SoundSource.BLOCKS, 0.5F, 0.75F, false);
				level.playLocalSound(x, y, z, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1F, 0.5F, false);
			}
		}
	}

	protected boolean checkGround() {

		if(!level.dimensionType().hasSkyLight()) return false;

		int valid = 0;
		int invalid = 0;

		for(int x = -1; x <= 1; x++) {
			for(int y = -1; y >= -groundDepth; y--) {
				for(int z = -1; z <= 1; z++) {
					BlockPos pos = worldPosition.offset(x, y, z);
					BlockState state = level.getBlockState(pos);

					if(y == -1 && !state.isRedstoneConductor(level, pos)) return false; // first layer has to be full solid

					if(getValidBlocks().contains(state.getBlock())) valid++;
					else invalid++;
				}
			}
		}

		return valid >= invalid; // valid block count has to be at least 50%
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(this.isOn);
		buf.writeBoolean(this.onGround);
		water.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.isOn = buf.readBoolean();
		this.onGround = buf.readBoolean();
		water.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		water.readFromNBT(nbt, "water");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		water.writeToNBT(nbt, "water");
	}

	protected abstract boolean canOperate();
	protected abstract void operate();

	protected DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(worldPosition.east(2), Direction.EAST),
				new DirPos(worldPosition.west(2), Direction.WEST),
				new DirPos(worldPosition.south(2), Direction.SOUTH),
				new DirPos(worldPosition.north(2), Direction.NORTH)
		};
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {water};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {water};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[0];
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 5, worldPosition.getZ() + 2);
	}
}
