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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tile entity of multiblock dummies that forwards energy, fluids and (through the capability) items to the core.
 * The flags are set by the block when it creates the proxy, based on the dummy's metadata.
 *
 * TODO crucible, redstone-over-radio and OpenComputers forwarding
 */
public class TileEntityProxyCombo extends TileEntityLoadedBase implements IEnergyReceiverMK2, IFluidReceiverMK2, api.hbm.tile.IHeatSource {

	private BlockEntity tile;
	public boolean inventory;
	public boolean power;
	public boolean conductor;
	public boolean fluid;
	public boolean heat;

	public TileEntityProxyCombo(BlockPos pos, BlockState state) {
		super(ModTileEntities.PROXY_COMBO.get(), pos, state);
	}

	public TileEntityProxyCombo inventory() { this.inventory = true; return this; }
	public TileEntityProxyCombo power() { this.power = true; return this; }
	public TileEntityProxyCombo conductor() { this.conductor = true; return this; }
	public TileEntityProxyCombo fluid() { this.fluid = true; return this; }
	public TileEntityProxyCombo heatSource() { this.heat = true; return this; }

	/** The flags have to be saved: on chunk load the tile is recreated by its type's factory, which doesn't know them */
	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.inventory = nbt.getBoolean("inv");
		this.power = nbt.getBoolean("power");
		this.conductor = nbt.getBoolean("conductor");
		this.fluid = nbt.getBoolean("fluid");
		this.heat = nbt.getBoolean("heat");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("inv", inventory);
		nbt.putBoolean("power", power);
		nbt.putBoolean("conductor", conductor);
		nbt.putBoolean("fluid", fluid);
		nbt.putBoolean("heat", heat);
	}

	/** The core tile entity, cached */
	public BlockEntity getTile() {
		if(tile == null || tile.isRemoved() || (tile instanceof TileEntityLoadedBase loaded && !loaded.isLoaded)) {
			tile = findCoreTile();
		}
		return tile;
	}

	/** What energy, fluid and heat calls go to: the core, or the delegate the core hands out for this very dummy */
	public Object getCoreObject() {
		BlockEntity core = getTile();
		if(core instanceof IProxyDelegateProvider provider) {
			Object delegate = provider.getDelegateForPosition(worldPosition);
			if(delegate != null) return delegate;
		}
		return core;
	}

	private BlockEntity findCoreTile() {
		if(level == null) return null;
		if(getBlockState().getBlock() instanceof com.hbm.blocks.IProxyController controller) return controller.getCore(level, worldPosition);
		if(!(getBlockState().getBlock() instanceof BlockDummyable dummy)) return null;
		BlockPos core = dummy.findCore(level, worldPosition);
		return core == null ? null : level.getBlockEntity(core);
	}

	/// ENERGY ///

	@Override
	public void setPower(long i) {
		if(power && getCoreObject() instanceof IEnergyReceiverMK2 rec) rec.setPower(i);
	}

	@Override
	public long getPower() {
		return power && getCoreObject() instanceof IEnergyReceiverMK2 rec ? rec.getPower() : 0;
	}

	@Override
	public long getMaxPower() {
		return power && getCoreObject() instanceof IEnergyReceiverMK2 rec ? rec.getMaxPower() : 0;
	}

	@Override
	public long transferPower(long amount) {
		if(power && getCoreObject() instanceof IEnergyReceiverMK2 rec) return rec.transferPower(amount);
		return amount;
	}

	@Override
	public long getReceiverSpeed() {
		return power && getCoreObject() instanceof IEnergyReceiverMK2 rec ? rec.getReceiverSpeed() : 0;
	}

	@Override
	public boolean canConnect(Direction dir) {
		if(power && getCoreObject() instanceof IEnergyConnectorMK2 con) return con.canConnect(dir);
		if(conductor && getCoreObject() instanceof IEnergyConductorMK2 con) return con.canConnect(dir);
		return false;
	}

	@Override
	public boolean allowDirectProvision() {
		if(!power) return false;
		if(getCoreObject() instanceof IEnergyReceiverMK2 rec) return rec.allowDirectProvision();
		return true;
	}

	@Override
	public ConnectionPriority getPriority() {
		return power && getCoreObject() instanceof IEnergyReceiverMK2 rec ? rec.getPriority() : ConnectionPriority.NORMAL;
	}

	/// HEAT ///

	@Override
	public int getHeatStored() {
		return heat && getCoreObject() instanceof api.hbm.tile.IHeatSource source ? source.getHeatStored() : 0;
	}

	@Override
	public void useUpHeat(int heat) {
		if(this.heat && getCoreObject() instanceof api.hbm.tile.IHeatSource source) source.useUpHeat(heat);
	}

	/// FLUIDS ///

	@Override
	public long transferFluid(FluidType type, int pressure, long amount) {
		if(fluid && getCoreObject() instanceof IFluidReceiverMK2 rec) return rec.transferFluid(type, pressure, amount);
		return amount;
	}

	@Override
	public long getDemand(FluidType type, int pressure) {
		return fluid && getCoreObject() instanceof IFluidReceiverMK2 rec ? rec.getDemand(type, pressure) : 0;
	}

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		return fluid && getCoreObject() instanceof IFluidConnectorMK2 con && con.canConnect(type, dir);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return fluid && getCoreObject() instanceof IFluidReceiverMK2 rec ? rec.getAllTanks() : new FluidTank[0];
	}

	@Override
	public int[] getReceivingPressureRange(FluidType type) {
		return fluid && getCoreObject() instanceof IFluidReceiverMK2 rec ? rec.getReceivingPressureRange(type) : DEFAULT_PRESSURE_RANGE;
	}
}
