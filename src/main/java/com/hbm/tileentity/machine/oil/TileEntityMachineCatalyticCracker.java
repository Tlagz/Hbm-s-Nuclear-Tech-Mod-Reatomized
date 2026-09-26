package com.hbm.tileentity.machine.oil;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.CrackingRecipes;
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
 * Catalytic cracker: 100mB of the input and 200mB steam into two products (twice every 5 ticks), 2mB spent steam.
 * TODO copy tool
 */
public class TileEntityMachineCatalyticCracker extends TileEntityLoadedBase implements IFluidStandardTransceiverMK2 {

	public FluidTank[] tanks;

	public TileEntityMachineCatalyticCracker(BlockPos pos, BlockState state) {
		super(ModTileEntities.CATALYTIC_CRACKER.get(), pos, state);
		tanks = new FluidTank[5];
		tanks[0] = new FluidTank(Fluids.BITUMEN, 4000);
		tanks[1] = new FluidTank(Fluids.STEAM, 8000);
		tanks[2] = new FluidTank(Fluids.OIL, 4000);
		tanks[3] = new FluidTank(Fluids.PETROLEUM, 4000);
		tanks[4] = new FluidTank(Fluids.SPENTSTEAM, 800);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			setupTanks();
			updateConnections();

			if(level.getGameTime() % 5 == 0)
				crack();

			if(level.getGameTime() % 10 == 0) {
				for(DirPos pos : getConPos()) {
					for(int i = 2; i <= 4; i++) {
						if(tanks[i].getFill() > 0) this.tryProvide(tanks[i], level, pos, pos.getDir());
					}
				}
			}

			networkPackNT(25);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		for(FluidTank tank : tanks)
			tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		for(FluidTank tank : tanks)
			tank.deserialize(buf);
	}

	private void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(tanks[0].getTankType(), level, pos, pos.getDir());
			this.trySubscribe(tanks[1].getTankType(), level, pos, pos.getDir());
		}
	}

	private void crack() {

		Pair<FluidStack, FluidStack> quart = CrackingRecipes.getCracking(tanks[0].getTankType());

		if(quart != null) {

			int left = quart.getKey().fill;
			int right = quart.getValue().fill;

			for(int i = 0; i < 2; i++) {
				if(tanks[0].getFill() >= 100 && tanks[1].getFill() >= 200 && hasSpace(left, right)) {
					tanks[0].setFill(tanks[0].getFill() - 100);
					tanks[1].setFill(tanks[1].getFill() - 200);
					tanks[2].setFill(tanks[2].getFill() + left);
					tanks[3].setFill(tanks[3].getFill() + right);
					tanks[4].setFill(tanks[4].getFill() + 2); //LPS has the density of WATER not STEAM (1%!)
				}
			}
		}
	}

	private boolean hasSpace(int left, int right) {
		return tanks[2].getFill() + left <= tanks[2].getMaxFill() && tanks[3].getFill() + right <= tanks[3].getMaxFill() && tanks[4].getFill() + 2 <= tanks[4].getMaxFill();
	}

	private void setupTanks() {

		Pair<FluidStack, FluidStack> quart = CrackingRecipes.getCracking(tanks[0].getTankType());

		if(quart != null) {
			tanks[1].setTankType(Fluids.STEAM);
			tanks[2].setTankType(quart.getKey().type);
			tanks[3].setTankType(quart.getValue().type);
			tanks[4].setTankType(Fluids.SPENTSTEAM);
		} else {
			tanks[2].setTankType(Fluids.NONE);
			tanks[3].setTankType(Fluids.NONE);
			tanks[4].setTankType(Fluids.NONE);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		for(int i = 0; i < 5; i++)
			tanks[i].readFromNBT(nbt, "tank" + i);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		for(int i = 0; i < 5; i++)
			tanks[i].writeToNBT(nbt, "tank" + i);
	}

	protected DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.relative(dir, 4).relative(rot, 1), dir),
				new DirPos(p.relative(dir, 4).relative(rot, -2), dir),
				new DirPos(p.relative(dir, -4).relative(rot, 1), dir.getOpposite()),
				new DirPos(p.relative(dir, -4).relative(rot, -2), dir.getOpposite()),
				new DirPos(p.relative(dir, 2).relative(rot, 3), rot),
				new DirPos(p.relative(dir, 2).relative(rot, -4), rot),
				new DirPos(p.relative(dir, -2).relative(rot, 3), rot.getOpposite()),
				new DirPos(p.relative(dir, -2).relative(rot, -4), rot.getOpposite())
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 3, worldPosition.getY(), worldPosition.getZ() - 3, worldPosition.getX() + 4, worldPosition.getY() + 16, worldPosition.getZ() + 4);
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[2], tanks[3], tanks[4]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0], tanks[1]};
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}
}
