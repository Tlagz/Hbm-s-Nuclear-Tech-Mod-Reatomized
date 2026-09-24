package com.hbm.lib;

import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.ItemBatteryCreative;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyConnectorBlock;
import api.hbm.energymk2.IEnergyConnectorMK2;
import api.hbm.fluidmk2.IFluidConnectorBlockMK2;
import api.hbm.fluidmk2.IFluidConnectorMK2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class Library {

	/** Moves power from the tile into the battery in the given slot, returns the tile's new power */
	public static long chargeItemsFromTE(List<ItemStack> slots, int index, long power, long maxPower) {

		if(power < 0) return 0;
		if(power > maxPower) return maxPower;

		ItemStack stack = slots.get(index);
		if(stack.getItem() instanceof ItemBatteryCreative) return 0;

		if(stack.getItem() instanceof IBatteryItem battery) {

			long batMax = battery.getMaxCharge(stack);
			long batCharge = battery.getCharge(stack);
			long batRate = battery.getChargeRate(stack);
			long toCharge = Math.min(Math.min(power, batRate), batMax - batCharge);

			power -= toCharge;

			battery.chargeBattery(stack, toCharge);
		}

		return power;
	}

	/** Moves power from the battery in the given slot into the tile, returns the tile's new power */
	public static long chargeTEFromItems(List<ItemStack> slots, int index, long power, long maxPower) {

		ItemStack stack = slots.get(index);
		if(stack.getItem() instanceof ItemBatteryCreative) return maxPower;

		if(stack.getItem() instanceof IBatteryItem battery) {

			long batCharge = battery.getCharge(stack);
			long batRate = battery.getDischargeRate(stack);
			long toDischarge = Math.min(Math.min((maxPower - power), batRate), batCharge);

			battery.dischargeBattery(stack, toDischarge);
			power += toDischarge;
		}

		return power;
	}

	/**
	 * Whether a pipe of this fluid type can connect to the block at pos
	 * @param dir the direction from the pipe towards pos
	 */
	public static boolean canConnectFluid(BlockGetter world, BlockPos pos, Direction dir, FluidType type) {

		if(world instanceof LevelReader reader && reader.isOutsideBuildHeight(pos))
			return false;

		BlockState state = world.getBlockState(pos);

		if(state.getBlock() instanceof IFluidConnectorBlockMK2 con) {
			if(con.canConnect(type, world, pos, dir.getOpposite() /* machine's connecting side */))
				return true;
		}

		BlockEntity te = world.getBlockEntity(pos);

		if(te instanceof IFluidConnectorMK2 con) {
			if(con.canConnect(type, dir.getOpposite() /* machine's connecting side */))
				return true;
		}

		return false;
	}

	/**
	 * Whether a cable can visually connect to the block at pos
	 * @param dir the direction from the cable towards pos
	 */
	public static boolean canConnect(BlockGetter world, BlockPos pos, Direction dir) {

		if(world instanceof LevelReader reader && reader.isOutsideBuildHeight(pos))
			return false;

		BlockState state = world.getBlockState(pos);

		if(state.getBlock() instanceof IEnergyConnectorBlock con) {
			if(con.canConnect(world, pos, dir.getOpposite() /* machine's connecting side */))
				return true;
		}

		BlockEntity te = world.getBlockEntity(pos);

		if(te instanceof IEnergyConnectorMK2 con) {
			if(con.canConnect(dir.getOpposite() /* machine's connecting side */))
				return true;
		}

		return false;
	}
}
