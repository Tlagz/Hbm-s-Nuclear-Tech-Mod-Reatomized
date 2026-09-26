package com.hbm.tileentity.machine.oil;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.ModMenus;
import com.hbm.inventory.container.ContainerOilProcessor;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.OilProcessingRecipes;
import com.hbm.items.ModItems;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;
import com.hbm.util.Tuple.Triplet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Catalytic reformer: naphtha and the like into reformate, petroleum and hydrogen, needs a catalytic converter.
 * Slots: 0 battery, 1/2 input containers, 3-8 output containers, 9 fluid identifier, 10 catalytic converter.
 */
public class TileEntityMachineCatalyticReformer extends TileEntityOilProcessorBase {

	public TileEntityMachineCatalyticReformer(BlockPos pos, BlockState state) {
		super(ModTileEntities.CATALYTIC_REFORMER.get(), pos, state, 11);
		this.tanks = new FluidTank[4];
		this.tanks[0] = new FluidTank(Fluids.NAPHTHA, 64_000);
		this.tanks[1] = new FluidTank(Fluids.REFORMATE, 24_000);
		this.tanks[2] = new FluidTank(Fluids.PETROLEUM, 24_000);
		this.tanks[3] = new FluidTank(Fluids.HYDROGEN, 24_000);
	}

	@Override
	public String getName() {
		return "container.catalyticReformer";
	}

	@Override
	protected String[] getTankKeys() {
		return new String[] {"input", "o1", "o2", "o3"};
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.chargeAndConnect(this.level.getGameTime() % 20 == 0);
			tanks[0].setType(9, slots);
			tanks[0].loadTank(1, 2, slots);

			reform();

			tanks[1].unloadTank(3, 4, slots);
			tanks[2].unloadTank(5, 6, slots);
			tanks[3].unloadTank(7, 8, slots);

			this.sendOutputs();

			this.networkPackNT(150);
		}
	}

	private void reform() {

		Triplet<FluidStack, FluidStack, FluidStack> out = OilProcessingRecipes.reforming.get(tanks[0].getTankType());
		if(out == null) {
			tanks[1].setTankType(Fluids.NONE);
			tanks[2].setTankType(Fluids.NONE);
			tanks[3].setTankType(Fluids.NONE);
			return;
		}

		tanks[1].setTankType(out.getX().type);
		tanks[2].setTankType(out.getY().type);
		tanks[3].setTankType(out.getZ().type);

		if(power < 20_000) return;
		if(tanks[0].getFill() < 100) return;
		if(!slots.get(10).is(ModItems.catalytic_converter.get())) return;

		if(tanks[1].getFill() + out.getX().fill > tanks[1].getMaxFill()) return;
		if(tanks[2].getFill() + out.getY().fill > tanks[2].getMaxFill()) return;
		if(tanks[3].getFill() + out.getZ().fill > tanks[3].getMaxFill()) return;

		tanks[0].setFill(tanks[0].getFill() - 100);
		tanks[1].setFill(tanks[1].getFill() + out.getX().fill);
		tanks[2].setFill(tanks[2].getFill() + out.getY().fill);
		tanks[3].setFill(tanks[3].getFill() + out.getZ().fill);

		power -= 20_000;
	}

	@Override
	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.relative(dir, 2).relative(rot), dir),
				new DirPos(p.relative(dir, 2).relative(rot, -1), dir),
				new DirPos(p.relative(dir, -2).relative(rot), dir.getOpposite()),
				new DirPos(p.relative(dir, -2).relative(rot, -1), dir.getOpposite()),
				new DirPos(p.relative(rot, 3), rot),
				new DirPos(p.relative(rot, -3), rot.getOpposite())
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 7, worldPosition.getZ() + 3);
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[1], tanks[2], tanks[3]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0]};
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerOilProcessor(ModMenus.CATALYTIC_REFORMER.get(), id, inv, this);
	}
}
