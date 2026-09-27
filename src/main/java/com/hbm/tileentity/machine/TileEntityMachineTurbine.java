package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerMachineTurbine;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Coolable.CoolingType;
import com.hbm.lib.Library;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Steam turbine: the small one-block turbine, turns steam (of any density) into power and spent steam at 85%
 * efficiency, at most 6000mB per tick. The buffer leaks 5% per tick.
 * Slots: 0/1 fluid identifier in/out, 2/3 input fluid containers in/out, 4 battery, 5/6 output fluid containers in/out.
 *
 * TODO config (IConfigurableMachine), OpenComputers
 */
public class TileEntityMachineTurbine extends TileEntityMachineBase implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, MenuProvider {

	public long power;
	public FluidTank[] tanks;

	private static final int[] slots_top = new int[] {4};
	private static final int[] slots_bottom = new int[] {6};
	private static final int[] slots_side = new int[] {4};

	protected double[] info = new double[3];

	public static long maxPower = 1_000_000;
	public static int inputTankSize = 64_000;
	public static int outputTankSize = 128_000;
	public static int maxSteamPerTick = 6_000;
	public static double efficiency = 0.85D;

	public TileEntityMachineTurbine(BlockPos pos, BlockState state) {
		super(ModTileEntities.TURBINE.get(), pos, state, 7);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.STEAM, inputTankSize);
		tanks[1] = new FluidTank(Fluids.SPENTSTEAM, outputTankSize);
	}

	@Override
	public String getName() {
		return "container.machineTurbine";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return i == 4 && stack.getItem() instanceof IBatteryItem;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return side == Direction.DOWN ? slots_bottom : (side == Direction.UP ? slots_top : slots_side);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return false;
	}

	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.info = new double[3];

			for(Direction dir : Direction.values()) {
				this.trySubscribe(tanks[0].getTankType(), level, worldPosition.relative(dir), dir);
				this.tryProvide(level, worldPosition.relative(dir), dir);
			}

			tanks[0].setType(0, 1, slots);
			tanks[0].loadTank(2, 3, slots);
			power = Library.chargeItemsFromTE(slots, 4, power, maxPower);

			this.power *= 0.95;

			FluidType in = tanks[0].getTankType();
			boolean valid = false;
			if(in.hasTrait(FT_Coolable.class)) {
				FT_Coolable trait = in.getTrait(FT_Coolable.class);
				double eff = trait.getEfficiency(CoolingType.TURBINE) * efficiency; //small turbine is only 85% efficient by default
				if(eff > 0) {
					tanks[1].setTankType(trait.coolsTo);
					int inputOps = tanks[0].getFill() / trait.amountReq;
					int outputOps = (tanks[1].getMaxFill() - tanks[1].getFill()) / trait.amountProduced;
					int cap = maxSteamPerTick / trait.amountReq;
					int ops = Math.min(inputOps, Math.min(outputOps, cap));
					tanks[0].setFill(tanks[0].getFill() - ops * trait.amountReq);
					tanks[1].setFill(tanks[1].getFill() + ops * trait.amountProduced);
					this.power += (long) (ops * trait.heatEnergy * eff);
					info[0] = ops * trait.amountReq;
					info[1] = ops * trait.amountProduced;
					info[2] = ops * trait.heatEnergy * eff;
					valid = true;
				}
			}
			if(!valid) tanks[1].setTankType(Fluids.NONE);
			if(power > maxPower) power = maxPower;

			if(tanks[1].getFill() > 0) for(Direction dir : Direction.values()) this.tryProvide(tanks[1], level, worldPosition.relative(dir), dir);

			tanks[1].unloadTank(5, 6, slots);

			this.networkPackNT(25);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "steam");
		power = nbt.getLong("power");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "steam");
		nbt.putLong("power", power);
	}

	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }
	@Override public void setPower(long i) { this.power = i; }

	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tanks[1] }; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tanks[0] }; }
	@Override public FluidTank[] getAllTanks() { return tanks; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineTurbine(id, inv, this);
	}
}
