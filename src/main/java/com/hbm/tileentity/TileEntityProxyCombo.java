package com.hbm.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;

import api.hbm.energymk2.IEnergyConductorMK2;
import api.hbm.energymk2.IEnergyConnectorMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidConnectorMK2;
import api.hbm.fluidmk2.IFluidReceiverMK2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tile entity of multiblock dummies that forwards energy, fluids and (through the capability) items to the core.
 * The flags are set by the block when it creates the proxy, based on the dummy's metadata.
 *
 * TODO heat source, crucible, redstone-over-radio and OpenComputers forwarding
 */
public class TileEntityProxyCombo extends TileEntityLoadedBase implements IEnergyReceiverMK2, IFluidReceiverMK2 {

	private BlockEntity tile;
	public boolean inventory;
	public boolean power;
	public boolean conductor;
	public boolean fluid;

	public TileEntityProxyCombo(BlockPos pos, BlockState state) {
		super(ModTileEntities.PROXY_COMBO.get(), pos, state);
	}

	public TileEntityProxyCombo inventory() { this.inventory = true; return this; }
	public TileEntityProxyCombo power() { this.power = true; return this; }
	public TileEntityProxyCombo conductor() { this.conductor = true; return this; }
	public TileEntityProxyCombo fluid() { this.fluid = true; return this; }

	/** The core tile entity, cached */
	public BlockEntity getTile() {
		if(tile == null || tile.isRemoved() || (tile instanceof TileEntityLoadedBase loaded && !loaded.isLoaded)) {
			tile = findCoreTile();
		}
		return tile;
	}

	private BlockEntity findCoreTile() {
		if(level == null || !(getBlockState().getBlock() instanceof BlockDummyable dummy)) return null;
		BlockPos core = dummy.findCore(level, worldPosition);
		return core == null ? null : level.getBlockEntity(core);
	}

	/// ENERGY ///

	@Override
	public void setPower(long i) {
		if(power && getTile() instanceof IEnergyReceiverMK2 rec) rec.setPower(i);
	}

	@Override
	public long getPower() {
		return power && getTile() instanceof IEnergyReceiverMK2 rec ? rec.getPower() : 0;
	}

	@Override
	public long getMaxPower() {
		return power && getTile() instanceof IEnergyReceiverMK2 rec ? rec.getMaxPower() : 0;
	}

	@Override
	public long transferPower(long amount) {
		if(power && getTile() instanceof IEnergyReceiverMK2 rec) return rec.transferPower(amount);
		return amount;
	}

	@Override
	public long getReceiverSpeed() {
		return power && getTile() instanceof IEnergyReceiverMK2 rec ? rec.getReceiverSpeed() : 0;
	}

	@Override
	public boolean canConnect(Direction dir) {
		if(power && getTile() instanceof IEnergyConnectorMK2 con) return con.canConnect(dir);
		if(conductor && getTile() instanceof IEnergyConductorMK2 con) return con.canConnect(dir);
		return false;
	}

	@Override
	public boolean allowDirectProvision() {
		if(!power) return false;
		if(getTile() instanceof IEnergyReceiverMK2 rec) return rec.allowDirectProvision();
		return true;
	}

	@Override
	public ConnectionPriority getPriority() {
		return power && getTile() instanceof IEnergyReceiverMK2 rec ? rec.getPriority() : ConnectionPriority.NORMAL;
	}

	/// FLUIDS ///

	@Override
	public long transferFluid(FluidType type, int pressure, long amount) {
		if(fluid && getTile() instanceof IFluidReceiverMK2 rec) return rec.transferFluid(type, pressure, amount);
		return amount;
	}

	@Override
	public long getDemand(FluidType type, int pressure) {
		return fluid && getTile() instanceof IFluidReceiverMK2 rec ? rec.getDemand(type, pressure) : 0;
	}

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		return fluid && getTile() instanceof IFluidConnectorMK2 con && con.canConnect(type, dir);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return fluid && getTile() instanceof IFluidReceiverMK2 rec ? rec.getAllTanks() : new FluidTank[0];
	}

	@Override
	public int[] getReceivingPressureRange(FluidType type) {
		return fluid && getTile() instanceof IFluidReceiverMK2 rec ? rec.getReceivingPressureRange(type) : DEFAULT_PRESSURE_RANGE;
	}
}
