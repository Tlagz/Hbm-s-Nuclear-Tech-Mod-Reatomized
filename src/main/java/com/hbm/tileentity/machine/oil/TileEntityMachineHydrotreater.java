package com.hbm.tileentity.machine.oil;

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
 * Hydrotreater: desulfurizes oils with pressurized hydrogen, needs a catalytic converter.
 * Slots: 0 battery, 1/2 input containers, 3/4 hydrogen containers, 5-8 output containers, 9 fluid identifier,
 * 10 catalytic converter.
 */
public class TileEntityMachineHydrotreater extends TileEntityOilProcessorBase {

	public TileEntityMachineHydrotreater(BlockPos pos, BlockState state) {
		super(ModTileEntities.HYDROTREATER.get(), pos, state, 11);
		this.tanks = new FluidTank[4];
		this.tanks[0] = new FluidTank(Fluids.OIL, 64_000);
		this.tanks[1] = new FluidTank(Fluids.HYDROGEN, 64_000).withPressure(1);
		this.tanks[2] = new FluidTank(Fluids.OIL_DS, 24_000);
		this.tanks[3] = new FluidTank(Fluids.SOURGAS, 24_000);
	}

	@Override
	public String getName() {
		return "container.hydrotreater";
	}

	@Override
	protected String[] getTankKeys() {
		return new String[] {"t0", "t1", "t2", "t3"};
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.chargeAndConnect(this.level.getGameTime() % 20 == 0);
			tanks[0].setType(9, slots);
			tanks[0].loadTank(1, 2, slots);
			tanks[1].loadTank(3, 4, slots);

			if(level.getGameTime() % 2 == 0) reform();

			tanks[2].unloadTank(5, 6, slots);
			tanks[3].unloadTank(7, 8, slots);

			this.sendOutputs();

			this.networkPackNT(25);
		}
	}

	private void reform() {

		Triplet<FluidStack, FluidStack, FluidStack> out = OilProcessingRecipes.hydrotreating.get(tanks[0].getTankType());
		if(out == null) {
			tanks[2].setTankType(Fluids.NONE);
			tanks[3].setTankType(Fluids.NONE);
			return;
		}

		tanks[1].withPressure(out.getX().pressure).setTankType(out.getX().type);
		tanks[2].setTankType(out.getY().type);
		tanks[3].setTankType(out.getZ().type);

		if(power < 20_000) return;
		if(tanks[0].getFill() < 100) return;
		if(tanks[1].getFill() < out.getX().fill) return;
		if(!slots.get(10).is(ModItems.catalytic_converter.get())) return;

		if(tanks[2].getFill() + out.getY().fill > tanks[2].getMaxFill()) return;
		if(tanks[3].getFill() + out.getZ().fill > tanks[3].getMaxFill()) return;

		tanks[0].setFill(tanks[0].getFill() - 100);
		tanks[1].setFill(tanks[1].getFill() - out.getX().fill);
		tanks[2].setFill(tanks[2].getFill() + out.getY().fill);
		tanks[3].setFill(tanks[3].getFill() + out.getZ().fill);

		power -= 20_000;
	}

	@Override
	public DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.offset(2, 0, 1), Direction.EAST),
				new DirPos(p.offset(2, 0, -1), Direction.EAST),
				new DirPos(p.offset(-2, 0, 1), Direction.WEST),
				new DirPos(p.offset(-2, 0, -1), Direction.WEST),
				new DirPos(p.offset(1, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(-1, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(1, 0, -2), Direction.NORTH),
				new DirPos(p.offset(-1, 0, -2), Direction.NORTH)
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 7, worldPosition.getZ() + 2);
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[2], tanks[3]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0], tanks[1]};
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerOilProcessor(ModMenus.HYDROTREATER.get(), id, inv, this);
	}
}
