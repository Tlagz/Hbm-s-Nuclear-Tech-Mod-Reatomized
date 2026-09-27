package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Amat;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous;
import com.hbm.particle.ParticleEffectsNT;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Drainage pipe: takes any fluid (at low priority) and dumps it out of its end, with all the consequences of spilling
 * that fluid. Antimatter blows it up.
 *
 * TODO the oil spill puddles for viscous flammable liquids (the block isn't ported yet)
 */
public class TileEntityMachineDrain extends TileEntityLoadedBase implements IFluidStandardReceiverMK2 {

	public FluidTank tank;

	public TileEntityMachineDrain(BlockPos pos, BlockState state) {
		super(ModTileEntities.DRAIN.get(), pos, state);
		this.tank = new FluidTank(Fluids.NONE, 2_000);
	}

	@Override
	public void updateEntity() {

		if(!level.isClientSide) {

			if(level.getGameTime() % 20 == 0) {
				for(DirPos pos : getConPos()) this.trySubscribe(tank.getTankType(), level, pos);
			}

			networkPackNT(50);

			if(tank.getFill() > 0) {

				if(tank.getTankType().hasTrait(FT_Amat.class)) {
					level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 10F, true, Level.ExplosionInteraction.BLOCK);
					return;
				}

				int toSpill = Math.max(tank.getFill() / 2, 1);
				tank.setFill(tank.getFill() - toSpill);
				FluidTrait.onRelease(level, worldPosition, tank.getTankType(), tank, FluidReleaseType.SPILL, toSpill);
			}

		} else {

			// gases rise from the end of the pipe, liquids splash out
			if(tank.getFill() > 0) {
				Direction dir = BlockDummyable.getRotation(getBlockState());
				CompoundTag data = new CompoundTag();

				if(tank.getTankType().hasTrait(FT_Gaseous.class)) {
					data.putString("type", "tower");
					data.putFloat("lift", 0.5F);
					data.putFloat("base", 0.375F);
					data.putFloat("max", 3F);
					data.putInt("life", 100 + level.random.nextInt(50));
				} else {
					data.putString("type", "splash");
				}

				data.putInt("color", tank.getTankType().getColor());
				data.putDouble("posX", worldPosition.getX() + 0.5 - dir.getStepX() * 2.5);
				data.putDouble("posZ", worldPosition.getZ() + 0.5 - dir.getStepZ() * 2.5);
				data.putDouble("posY", worldPosition.getY() + 0.5);
				ParticleEffectsNT.effectNT(data);
			}
		}
	}

	/** The back and the two sides of the pipe's base */
	public DirPos[] getConPos() {
		Direction dir0 = BlockDummyable.getRotation(getBlockState());
		Direction dir1 = dir0.getClockWise();
		Direction dir2 = dir0.getCounterClockWise();

		return new DirPos[] {
				new DirPos(worldPosition.relative(dir0), dir0),
				new DirPos(worldPosition.relative(dir1), dir1),
				new DirPos(worldPosition.relative(dir2), dir2)
		};
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tank.readFromNBT(nbt, "t");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tank.writeToNBT(nbt, "t");
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		tank.deserialize(buf);
	}

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tank}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tank}; }
	@Override public ConnectionPriority getFluidPriority() { return ConnectionPriority.LOW; }

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		return dir.getAxis().isHorizontal();
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 1, worldPosition.getZ() + 3);
	}
}
