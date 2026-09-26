package com.hbm.tileentity.machine.oil;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.FractionRecipes;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.DirPos;
import com.hbm.util.Tuple.Pair;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Fractioning tower segment: 100mB of the input into two fractions every 10 ticks. Stacked segments (3 blocks apart)
 * share the work: the oil moves up, the fractions come down to the bottom segment.
 *
 * TODO copy tool
 */
public class TileEntityMachineFractionTower extends TileEntityLoadedBase implements IFluidStandardTransceiverMK2 {

	public FluidTank[] tanks;

	public TileEntityMachineFractionTower(BlockPos pos, BlockState state) {
		super(ModTileEntities.FRACTION_TOWER.get(), pos, state);
		tanks = new FluidTank[3];
		tanks[0] = new FluidTank(Fluids.HEAVYOIL, 4000);
		tanks[1] = new FluidTank(Fluids.BITUMEN, 4000);
		tanks[2] = new FluidTank(Fluids.SMEAR, 4000);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(level.getBlockEntity(worldPosition.above(3)) instanceof TileEntityMachineFractionTower frac) {

				//make types equal
				for(int i = 0; i < 3; i++) {
					frac.tanks[i].setTankType(tanks[i].getTankType());
				}

				//calculate transfer
				int oil = Math.min(tanks[0].getFill(), frac.tanks[0].getMaxFill() - frac.tanks[0].getFill());
				int left = Math.min(frac.tanks[1].getFill(), tanks[1].getMaxFill() - tanks[1].getFill());
				int right = Math.min(frac.tanks[2].getFill(), tanks[2].getMaxFill() - tanks[2].getFill());

				//move oil up, pull fractions down
				tanks[0].setFill(tanks[0].getFill() - oil);
				tanks[1].setFill(tanks[1].getFill() + left);
				tanks[2].setFill(tanks[2].getFill() + right);
				frac.tanks[0].setFill(frac.tanks[0].getFill() + oil);
				frac.tanks[1].setFill(frac.tanks[1].getFill() - left);
				frac.tanks[2].setFill(frac.tanks[2].getFill() - right);
			}

			setupTanks();
			this.updateConnections();

			if(level.getGameTime() % 10 == 0)
				fractionate();

			this.sendFluid();

			networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		for(int i = 0; i < 3; i++)
			tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		for(int i = 0; i < 3; i++)
			tanks[i].deserialize(buf);
	}

	private void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(tanks[0].getTankType(), level, pos, pos.getDir());
		}
	}

	private void sendFluid() {
		for(DirPos pos : getConPos()) {
			this.tryProvide(tanks[1], level, pos, pos.getDir());
			this.tryProvide(tanks[2], level, pos, pos.getDir());
		}
	}

	private DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(worldPosition.east(2), Direction.EAST),
				new DirPos(worldPosition.west(2), Direction.WEST),
				new DirPos(worldPosition.south(2), Direction.SOUTH),
				new DirPos(worldPosition.north(2), Direction.NORTH)
		};
	}

	private void setupTanks() {

		Pair<FluidStack, FluidStack> quart = FractionRecipes.getFractions(tanks[0].getTankType());

		if(quart != null) {
			tanks[1].setTankType(quart.getKey().type);
			tanks[2].setTankType(quart.getValue().type);
		} else {
			tanks[0].setTankType(Fluids.NONE);
			tanks[1].setTankType(Fluids.NONE);
			tanks[2].setTankType(Fluids.NONE);
		}
	}

	private void fractionate() {

		Pair<FluidStack, FluidStack> quart = FractionRecipes.getFractions(tanks[0].getTankType());

		if(quart != null) {

			int left = quart.getKey().fill;
			int right = quart.getValue().fill;

			if(tanks[0].getFill() >= 100 && hasSpace(left, right)) {
				tanks[0].setFill(tanks[0].getFill() - 100);
				tanks[1].setFill(tanks[1].getFill() + left);
				tanks[2].setFill(tanks[2].getFill() + right);
			}
		}
	}

	private boolean hasSpace(int left, int right) {
		return tanks[1].getFill() + left <= tanks[1].getMaxFill() && tanks[2].getFill() + right <= tanks[2].getMaxFill();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		for(int i = 0; i < 3; i++)
			tanks[i].readFromNBT(nbt, "tank" + i);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		for(int i = 0; i < 3; i++)
			tanks[i].writeToNBT(nbt, "tank" + i);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 3, worldPosition.getZ() + 2);
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tanks[1], tanks[2] };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tanks[0] };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}
}
